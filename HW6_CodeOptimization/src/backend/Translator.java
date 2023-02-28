package backend;

import backend.MipsCode.*;
import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;
import midend.MidCode.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;

import static backend.MipsCode.ITypeInstr.IOpCode.addiu;
import static backend.MipsCode.RTypeInstr.ROpCode.*;

public class Translator {
    private static final Translator TRANSLATOR = new Translator();
    private static final MidCodeTable midCodeTable = MidCodeTable.getInstance();
    private static final LabelTable labelTable = LabelTable.getInstance();
    private static final LinkedList<MipsCode> macroCodeList = new LinkedList<>();
    private static final LinkedList<MipsCode> mipsCodeList = new LinkedList<>();
    private static final StringJoiner mipsCode = new StringJoiner("\n");
    private static final RegScheduler scheduler = RegScheduler.getInstance();
    private static final LinkedList<Reg> synchronizedReg = new LinkedList<>();
    private static final HashMap<Value, Address> value2address = new HashMap<>();
    private final LinkedList<Value> valueFromArg = new LinkedList<>();
    private BasicBlock curBasicBlock;
    private MidCode curMidCode;
    private int frameSize;
    private int pushCount = 0;
    private int strCount = 0;
    private static int tempLabel = 0;

    public static Translator getInstance() {
        return TRANSLATOR;
    }

    public LinkedList<MipsCode> getMipsCodeList() {
        return mipsCodeList;
    }

    public LinkedList<Reg> getSynchronizedReg() {
        return synchronizedReg;
    }

    public HashMap<Value, Address> getValue2address() {
        return value2address;
    }

    public BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }

    public MidCode getCurMidCode() {
        return curMidCode;
    }

    public void translate() {
        for (MidCode midCode : midCodeTable.getMacroList()) {
            if (!(midCode instanceof Declare)) {
                continue;
            }
            Declare declare = (Declare) midCode;
            Value value = declare.getValue();
            LinkedList<Value> initValues = declare.getInitValues();
            LinkedList<Integer> intValues = new LinkedList<>();
            initValues.forEach(item -> intValues.add(((Imm) item).getValue()));
            macroCodeList.add(new WordMacro(value.getIdent(), declare.getSize(), intValues));
            value2address.put(value, new AbsoluteAddress(value.getIdent()));
        }
        for (FuncBlock funcBlock : midCodeTable.getFuncBlockList()) {
            scheduler.clearAll();
            //mipsCodeList.add(new Comment("****************              function                ****************\n\n"));
            scheduler.setCurFuncBlock(funcBlock);
            HashMap<Value, Reg> val2Reg = funcBlock.getVal2Reg();
            val2Reg.forEach((value, reg) -> System.out.println(value + " " + reg));
            translateFuncBlock(funcBlock);
            //mipsCodeList.add(new Comment("****************              function                ****************"));
        }
    }

    public void translateFuncBlock(FuncBlock funcBlock) {
        for (BasicBlock basicBlock : funcBlock.getBasicBlockList()) {
            scheduler.clear();
            //mipsCodeList.add(new Comment("****************              basic block                ****************\n\n"));
            curBasicBlock = basicBlock;
            translateBasicBlock();
            //mipsCodeList.add(new Comment("****************              basic block                ****************"));
        }
    }

    public void translateBasicBlock() {
        if (labelTable.getLabelList(curBasicBlock.getHead()).size() != 0) {
            mipsCodeList.addAll(labelTable.getLabelList(curBasicBlock.getHead()));
        }
        for (MidCode midCode = curBasicBlock.getHead(); midCode != curBasicBlock.getTail(); midCode = midCode.getNext()) {
            synchronizedReg.clear();
            //mipsCodeList.add(new Comment(midCode.toString()));
            curMidCode = midCode;
            translateMidCode();
        }
        synchronizedReg.clear();
        //mipsCodeList.add(new Comment(curBasicBlock.getTail().toString()));
        curMidCode = curBasicBlock.getTail();
        if (curBasicBlock.getTail() instanceof Branch || curBasicBlock.getTail() instanceof Return || curBasicBlock.getTail() instanceof Jump) {
            scheduler.flush(curBasicBlock.getTail() instanceof Return);
            translateMidCode();
        } else {
            translateMidCode();
            scheduler.flush(false);
        }
    }

    public void translateMidCode() {
        switch (curMidCode.getClass().toString()) {
            case "class midend.MidCode.ArgPush":
                assert curMidCode instanceof ArgPush;
                translateArgPush((ArgPush) curMidCode);
                break;
            case "class midend.MidCode.Assign":
                assert curMidCode instanceof Assign;
                translateAssign((Assign) curMidCode);
                break;
            case "class midend.MidCode.Branch":
                assert curMidCode instanceof Branch;
                translateBranch((Branch) curMidCode);
                break;
            case "class midend.MidCode.Declare":
                assert curMidCode instanceof Declare;
                translateDeclare((Declare) curMidCode);
                break;
            case "class midend.MidCode.Exit":
                assert curMidCode instanceof Exit;
                translateExit();
                break;
            case "class midend.MidCode.FuncCall":
                assert curMidCode instanceof FuncCall;
                translateFuncCall((FuncCall) curMidCode);
                break;
            case "class midend.MidCode.FuncEntry":
                assert curMidCode instanceof FuncEntry;
                translateFuncEntry((FuncEntry) curMidCode);
                break;
            case "class midend.MidCode.IntGet":
                assert curMidCode instanceof IntGet;
                translateIntGet();
                break;
            case "class midend.MidCode.Jump":
                assert curMidCode instanceof Jump;
                translateJump((Jump) curMidCode);
                break;
            case "class midend.MidCode.Load":
                assert curMidCode instanceof Load;
                translateLoad((Load) curMidCode);
                break;
            case "class midend.MidCode.Move":
                assert curMidCode instanceof Move;
                translateMove((Move) curMidCode);
                break;
            case "class midend.MidCode.ParaGet":
                assert curMidCode instanceof ParaGet;
                translateParaGet((ParaGet) curMidCode);
                break;
            case "class midend.MidCode.Print":
                assert curMidCode instanceof Print;
                translatePrint((Print) curMidCode);
                break;
            case "class midend.MidCode.Return":
                assert curMidCode instanceof Return;
                translateReturn((Return) curMidCode);
                break;
            case "class midend.MidCode.Store":
                assert curMidCode instanceof Store;
                translateStore((Store) curMidCode);
                break;
        }
        if (curMidCode instanceof UseVal) {
            for (Value useVal : ((UseVal) curMidCode).getUseVal()) {
                if (curBasicBlock.usedUp(useVal, curMidCode) && !curBasicBlock.getLiveOutSet().contains(useVal)) {
                    scheduler.unMap(useVal);
                }
            }
        }
    }

    public ValueMeta getValueMeta(Value value, boolean load, boolean lw) {
        if (value instanceof Imm) {
            return (Imm) value;
        } else if (value instanceof Word && value.getIdent().equals("?")) {
            return Reg.RV;
        } else {
            Reg reg;
            if ((reg = scheduler.find(value)) == null) {
                if ((reg = scheduler.alloc(value)) == null) {
                    reg = scheduler.preempt(value);
                }
                if (load) {
                    Address address = value2address.get(value);
                    if (value instanceof Word) {
                        mipsCodeList.add(new ITypeInstr.lw(reg, address));
                    } else if (address instanceof AbsoluteAddress) {
                        mipsCodeList.add(new ITypeInstr.la(reg, address));
                    } else if (lw || valueFromArg.contains(value) || ((Addr) value).isTemp()) {
                        mipsCodeList.add(new ITypeInstr.lw(reg, address));
                    } else {
                        mipsCodeList.add(new ITypeInstr.la(reg, address));
                    }
                }
            }
            synchronizedReg.add(reg);
            return reg;
        }
    }

    public void translateArgPush(ArgPush argPush) {
        pushCount++;
        ValueMeta valueMeta = getValueMeta(argPush.getValue(), true, false);
        if (valueMeta instanceof Reg) {
            mipsCodeList.add(new ITypeInstr.sw((Reg) valueMeta, new RelativeAddress(Reg.SP, -pushCount * 4)));
        } else {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) valueMeta));
            mipsCodeList.add(new ITypeInstr.sw(Reg.TR, new RelativeAddress(Reg.SP, -pushCount * 4)));
        }
    }

    public void translateAssign(Assign assign) {
        if (assign.getRightValue() instanceof BinaryOperate) {
            BinaryOperate.BinaryOp binaryOp = ((BinaryOperate) assign.getRightValue()).getBinaryOp();
            Value leftValue = ((BinaryOperate) assign.getRightValue()).getLeftValue();
            Value rightValue = ((BinaryOperate) assign.getRightValue()).getRightValue();
            ValueMeta leftMeta = getValueMeta(leftValue, true, false);
            ValueMeta rightMeta = getValueMeta(rightValue, true, false);
            Reg valMeta = (Reg) getValueMeta(assign.getLeftValue(), false, false);
            if (leftValue instanceof Addr) {
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(((Imm) rightMeta).getValue() * 4)));
                    operateValMeta(binaryOp, valMeta, leftMeta, Reg.TR);
                } else {
                    mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.TR, (Reg) rightMeta, new Imm(2)));
                    operateValMeta(binaryOp, valMeta, leftMeta, Reg.TR);
                }
            } else if (rightValue instanceof Addr) {
                if (leftMeta instanceof Imm) {
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(((Imm) leftMeta).getValue() * 4)));
                    operateValMeta(binaryOp, valMeta, Reg.TR, rightMeta);
                } else {
                    mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.TR, (Reg) leftMeta, new Imm(2)));
                    operateValMeta(binaryOp, valMeta, Reg.TR, rightMeta);
                }
            } else {
                operateValMeta(binaryOp, valMeta, leftMeta, rightMeta);
            }
        } else {
            UnaryOperate.UnaryOp unaryOp = ((UnaryOperate) assign.getRightValue()).getUnaryOp();
            Reg valMeta = (Reg) getValueMeta(assign.getLeftValue(), false, false);
            ValueMeta rightMeta = getValueMeta(((UnaryOperate) assign.getRightValue()).getValue(), true, false);
            operateUnary(unaryOp, valMeta, rightMeta);
        }
    }

    public void operateValMeta(BinaryOperate.BinaryOp binaryOp, Reg valMeta, ValueMeta leftMeta, ValueMeta rightMeta) {
        if (leftMeta instanceof Imm && rightMeta instanceof Imm) {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) leftMeta));
            mipsCodeList.add(new ITypeInstr.li(valMeta, (Imm) rightMeta));
            operateReg(valMeta, binaryOp, Reg.TR, valMeta);
        } else if (leftMeta instanceof Imm) {
            if (binaryOp == BinaryOperate.BinaryOp.MUL) {
                operateValMeta(binaryOp, valMeta, rightMeta, leftMeta);
            } else {
                mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) leftMeta));
                operateReg(valMeta, binaryOp, Reg.TR, (Reg) rightMeta);
            }
        } else if (rightMeta instanceof Imm) {
            int imm = ((Imm) rightMeta).getValue();
            if (binaryOp == BinaryOperate.BinaryOp.MUL) {
                if (imm > 0) {
                    if (Integer.bitCount(imm) == 1) {
                        int bit = Integer.numberOfTrailingZeros(imm);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, valMeta, (Reg) leftMeta, new Imm(bit)));
                    } else if (Integer.bitCount(imm + 1) == 1) {
                        int bit = Integer.numberOfTrailingZeros(imm + 1);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.RV, (Reg) leftMeta, new Imm(bit)));
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.RV, (Reg) leftMeta));
                    } else if (Integer.bitCount(imm - 1) == 1) {
                        int bit = Integer.numberOfTrailingZeros(imm - 1);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.RV, (Reg) leftMeta, new Imm(bit)));
                        mipsCodeList.add(new RTypeInstr.RR3(addu, valMeta, Reg.RV, (Reg) leftMeta));
                    } else {
                        mipsCodeList.add(new ITypeInstr.li(Reg.RV, (Imm) rightMeta));
                        operateReg(valMeta, binaryOp, (Reg) leftMeta, Reg.RV);
                    }
                } else if (imm < 0) {
                    if (Integer.bitCount(-imm) == 1) {
                        int bit = Integer.numberOfTrailingZeros(-imm);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, valMeta, (Reg) leftMeta, new Imm(bit)));
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.ZERO, valMeta));
                    } else if (Integer.bitCount(-imm + 1) == 1) {
                        int bit = Integer.numberOfTrailingZeros(-imm + 1);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.RV, (Reg) leftMeta, new Imm(bit)));
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, (Reg) leftMeta, Reg.RV));
                    } else if (Integer.bitCount(-imm - 1) == 1) {
                        int bit = Integer.numberOfTrailingZeros(-imm - 1);
                        mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.RV, (Reg) leftMeta, new Imm(bit)));
                        mipsCodeList.add(new RTypeInstr.RR3(addu, valMeta, (Reg) leftMeta, Reg.RV));
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.ZERO, valMeta));
                    } else {
                        mipsCodeList.add(new ITypeInstr.li(Reg.RV, (Imm) rightMeta));
                        operateReg(valMeta, binaryOp, (Reg) leftMeta, Reg.RV);
                    }
                }
            } else if (binaryOp == BinaryOperate.BinaryOp.DIV) {
                if (imm == 2) {
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, valMeta, (Reg) leftMeta, new Imm(1)));
                } else if (Integer.bitCount(Math.abs(imm)) == 1) {
                    int bit = Integer.numberOfTrailingZeros(Math.abs(imm));
                    mipsCodeList.add(new ITypeInstr.bgez((Reg) leftMeta, "Temp" + tempLabel));
                    mipsCodeList.add(new ITypeInstr.IR2I1(addiu, (Reg) leftMeta, (Reg) leftMeta, new Imm(Math.abs(imm) - 1)));
                    mipsCodeList.add(new Label("Temp" + tempLabel++));
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, valMeta, (Reg) leftMeta, new Imm(bit)));
                    if (imm < 0) {
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.ZERO, valMeta));
                    }
                } else {
                    int l = Math.max(((int) Math.ceil(Math.log(Math.abs(imm)) / Math.log(2))), 1);
                    BigInteger m = BigInteger.ONE.add(BigInteger.valueOf(2).pow(31 + l)
                            .divide(BigInteger.valueOf(Math.abs(imm))));
                    BigInteger m1 = m.subtract(BigInteger.valueOf(2).pow(32));
                    int sh = l - 1;
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(m1.intValue())));
                    mipsCodeList.add(new mult((Reg) leftMeta, Reg.TR));
                    mipsCodeList.add(new mfhi(Reg.TR));
                    mipsCodeList.add(new RTypeInstr.RR3(addu, Reg.TR, Reg.TR, (Reg) leftMeta));
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, Reg.TR, Reg.TR, new Imm(sh)));
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, Reg.RV, (Reg) leftMeta, new Imm(31)));
                    if (imm > 0) {
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.TR, Reg.RV));
                    } else {
                        mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.RV, Reg.TR));
                    }
                }
            } else if (binaryOp == BinaryOperate.BinaryOp.MOD) {
                if (imm == 2) {
                    mipsCodeList.add(new RTypeInstr.RR2I1(and, valMeta, (Reg) leftMeta, new Imm(1)));
                } else if (Integer.bitCount(Math.abs(imm)) == 1) {
                    int bit = -2147483648 + Math.abs(imm) - 1;
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(bit)));
                    mipsCodeList.add(new RTypeInstr.RR3(and, valMeta, Reg.TR, (Reg) leftMeta));
                    mipsCodeList.add(new ITypeInstr.bgez(valMeta, "Temp" + tempLabel));
                    mipsCodeList.add(new ITypeInstr.IR2I1(addiu, valMeta, valMeta, new Imm(-1)));
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(-Math.abs(imm))));
                    mipsCodeList.add(new RTypeInstr.RR3(or, valMeta, valMeta, Reg.TR));
                    mipsCodeList.add(new ITypeInstr.IR2I1(addiu, valMeta, valMeta, new Imm(1)));
                    mipsCodeList.add(new Label("Temp" + tempLabel++));
                } else {
                    int l = Math.max(((int) Math.ceil(Math.log(Math.abs(imm)) / Math.log(2))), 1);
                    BigInteger m = BigInteger.ONE.add(BigInteger.valueOf(2).pow(31 + l)
                            .divide(BigInteger.valueOf(Math.abs(imm))));
                    BigInteger m1 = m.subtract(BigInteger.valueOf(2).pow(32));
                    int sh = l - 1;
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(m1.intValue())));
                    mipsCodeList.add(new mult((Reg) leftMeta, Reg.TR));
                    mipsCodeList.add(new mfhi(Reg.TR));
                    mipsCodeList.add(new RTypeInstr.RR3(addu, Reg.TR, Reg.TR, (Reg) leftMeta));
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, Reg.TR, Reg.TR, new Imm(sh)));
                    mipsCodeList.add(new RTypeInstr.RR2I1(sra, Reg.RV, (Reg) leftMeta, new Imm(31)));
                    if (imm > 0) {
                        mipsCodeList.add(new RTypeInstr.RR3(subu, Reg.TR, Reg.TR, Reg.RV));
                    } else {
                        mipsCodeList.add(new RTypeInstr.RR3(subu, Reg.TR, Reg.RV, Reg.TR));
                    }
                    operateValMeta(BinaryOperate.BinaryOp.MUL, Reg.TR, Reg.TR, new Imm(imm));
                    mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, (Reg) leftMeta, Reg.TR));
                }
            } else {
                mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
                operateReg(valMeta, binaryOp, (Reg) leftMeta, Reg.TR);
            }
        } else {
            operateReg(valMeta, binaryOp, (Reg) leftMeta, (Reg) rightMeta);
        }
    }

    private void operateReg(Reg valMeta, BinaryOperate.BinaryOp binaryOp, Reg leftReg, Reg rightReg) {
        switch (binaryOp) {
            case ADD:
                mipsCodeList.add(new RTypeInstr.RR3(addu, valMeta, leftReg, rightReg));
                break;
            case SUB:
                mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, leftReg, rightReg));
                break;
            case MUL:
                mipsCodeList.add(new mult(leftReg, rightReg));
                mipsCodeList.add(new mflo(valMeta));
                break;
            case DIV:
                mipsCodeList.add(new div(leftReg, rightReg));
                mipsCodeList.add(new mflo(valMeta));
                break;
            case MOD:
                mipsCodeList.add(new div(leftReg, rightReg));
                mipsCodeList.add(new mfhi(valMeta));
                break;
            case AND:
                mipsCodeList.add(new RTypeInstr.RR3(sne, valMeta, leftReg, Reg.ZERO));
                mipsCodeList.add(new RTypeInstr.RR3(sne, Reg.TR, rightReg, Reg.ZERO));
                mipsCodeList.add(new RTypeInstr.RR3(and, valMeta, valMeta, Reg.TR));
                break;
            case OR:
                mipsCodeList.add(new RTypeInstr.RR3(sne, valMeta, leftReg, Reg.ZERO));
                mipsCodeList.add(new RTypeInstr.RR3(sne, Reg.TR, rightReg, Reg.ZERO));
                mipsCodeList.add(new RTypeInstr.RR3(or, valMeta, valMeta, Reg.TR));
                break;
            case EQ:
                mipsCodeList.add(new RTypeInstr.RR3(seq, valMeta, leftReg, rightReg));
                break;
            case NE:
                mipsCodeList.add(new RTypeInstr.RR3(sne, valMeta, leftReg, rightReg));
                break;
            case GE:
                mipsCodeList.add(new RTypeInstr.RR3(sge, valMeta, leftReg, rightReg));
                break;
            case GT:
                mipsCodeList.add(new RTypeInstr.RR3(sgt, valMeta, leftReg, rightReg));
                break;
            case LE:
                mipsCodeList.add(new RTypeInstr.RR3(sle, valMeta, leftReg, rightReg));
                break;
            case LT:
                mipsCodeList.add(new RTypeInstr.RR3(slt, valMeta, leftReg, rightReg));
                break;
        }
    }

    public void operateUnary(UnaryOperate.UnaryOp unaryOp, Reg valMeta, ValueMeta rightMeta) {
        if (rightMeta instanceof Imm) {
            switch (unaryOp) {
                case POS:
                    mipsCodeList.add(new ITypeInstr.li(valMeta, (Imm) rightMeta));
                    break;
                case NEG:
                    mipsCodeList.add(new ITypeInstr.li(valMeta, new Imm(-((Imm) rightMeta).getValue())));
                    break;
                case NOT:
                    mipsCodeList.add(new ITypeInstr.li(valMeta, new Imm(((Imm) rightMeta).getValue() == 0 ? 1 : 0)));
                    break;
            }
        } else {
            switch (unaryOp) {
                case POS:
                    mipsCodeList.add(new RTypeInstr.RR3(addu, valMeta, (Reg) rightMeta, Reg.ZERO));
                    break;
                case NEG:
                    mipsCodeList.add(new RTypeInstr.RR3(subu, valMeta, Reg.ZERO, (Reg) rightMeta));
                    break;
                case NOT:
                    mipsCodeList.add(new RTypeInstr.RR3(seq, valMeta, (Reg) rightMeta, Reg.ZERO));
                    break;
            }
        }
    }

    public void translateBranch(Branch branch) {
        Branch.BranchOp branchOp = branch.getBranchOp();
        ValueMeta leftMeta = getValueMeta(branch.getLeftValue(), true, false);
        if (leftMeta instanceof Imm) {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) leftMeta));
            if (branchOp == Branch.BranchOp.EQ) {
                mipsCodeList.add(new ITypeInstr.beq(Reg.TR, Reg.ZERO, branch.getBranchLabel().getLabelName()));
            } else {
                mipsCodeList.add(new ITypeInstr.bne(Reg.TR, Reg.ZERO, branch.getBranchLabel().getLabelName()));
            }
        } else if (branchOp == Branch.BranchOp.EQ) {
            mipsCodeList.add(new ITypeInstr.beq((Reg) leftMeta, Reg.ZERO, branch.getBranchLabel().getLabelName()));
        } else {
            mipsCodeList.add(new ITypeInstr.bne((Reg) leftMeta, Reg.ZERO, branch.getBranchLabel().getLabelName()));
        }
    }

    public void translateDeclare(Declare declare) {
        if (declare.getValue() instanceof Word) {
            Reg leftMeta = (Reg) getValueMeta(declare.getValue(), false, false);
            if (!declare.getInitValues().isEmpty()) {
                Value value = declare.getInitValues().get(0);
                ValueMeta rightMeta = getValueMeta(value, true, false);
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new ITypeInstr.li(leftMeta, (Imm) rightMeta));
                } else {
                    mipsCodeList.add(new RTypeInstr.RR3(addu, leftMeta, (Reg) rightMeta, Reg.ZERO));
                }
            }
        } else {
            RelativeAddress addr = (RelativeAddress) value2address.get(declare.getValue());
            for (int i = 0, size = declare.getInitValues().size(); i < size; i++) {
                Value value = declare.getInitValues().get(i);
                ValueMeta rightMeta = getValueMeta(value, true, false);
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
                    mipsCodeList.add(new ITypeInstr.sw(Reg.TR,
                            new RelativeAddress(addr.getBase(), addr.getOffset() + i * 4)));
                } else {
                    mipsCodeList.add(new ITypeInstr.sw((Reg) rightMeta,
                            new RelativeAddress(addr.getBase(), addr.getOffset() + i * 4)));
                }
            }
        }
    }

    public void translateExit() {
        mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(10)));
        mipsCodeList.add(new Syscall());
    }

    public void translateFuncCall(FuncCall funcCall) {
        pushCount = 0;
        HashMap<Reg, Value> reg2value = scheduler.getReg2value();
        reg2value.forEach((reg, value) -> {
            Address address = value2address.get(value);
            if (value instanceof Word) {
                if (curBasicBlock.isLive(value, funcCall) || value.isGlobal()) {
                    mipsCodeList.add(new ITypeInstr.sw(reg, address));
                }
            } else if (value instanceof Addr && ((Addr) value).isTemp() && curBasicBlock.isLive(value, funcCall)) {
                mipsCodeList.add(new ITypeInstr.sw(reg, address));
            }
        });
        mipsCodeList.add(new ITypeInstr.sw(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        mipsCodeList.add(new JTypeInstr.jal(funcCall.getIdent()));
        mipsCodeList.add(new ITypeInstr.lw(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        reg2value.forEach((reg, value) -> {
            if (scheduler.getGlobalRegs().contains(reg) && curBasicBlock.isLive(value, funcCall)) {
                Address address = value2address.get(value);
                if (value instanceof Word) {
                    mipsCodeList.add(new ITypeInstr.lw(reg, address));
                } else if (address instanceof AbsoluteAddress) {
                    mipsCodeList.add(new ITypeInstr.la(reg, address));
                } else if (valueFromArg.contains(value) || ((Addr) value).isTemp()) {
                    mipsCodeList.add(new ITypeInstr.lw(reg, address));
                } else {
                    mipsCodeList.add(new ITypeInstr.la(reg, address));
                }
            }
        });
        scheduler.clear();
    }

    public void translateFuncEntry(FuncEntry funcEntry) {
        valueFromArg.clear();
        int fp = 4;
        LinkedList<Value> values = MidCodeTable.getInstance().getValInfos(funcEntry.getEntryLabel().getLabelName());
        for (int i = values.size() - 1; i >= 0; i--) {
            value2address.put(values.get(i), new RelativeAddress(Reg.SP, fp));
            fp += MidCodeTable.getInstance().getValSize(values.get(i)) * 4;
            //System.out.println(values.get(i) + " " + value2address.get(values.get(i)));
        }
        frameSize = fp;
        mipsCodeList.add(new ITypeInstr.IR2I1(addiu, Reg.SP, Reg.SP, new Imm(-fp)));
    }

    public void translateIntGet() {
        mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(5)));
        mipsCodeList.add(new Syscall());
    }

    public void translateJump(Jump jump) {
        mipsCodeList.add(new JTypeInstr.j(jump.getLabel().getLabelName()));
    }

    public void translateLoad(Load load) {
        ValueMeta rightMeta = getValueMeta(load.getRightValue(), true, false);
        ValueMeta leftMeta = getValueMeta(load.getLeftValue(), false, false);
        mipsCodeList.add(new ITypeInstr.lw((Reg) leftMeta, new RelativeAddress((Reg) rightMeta, 0)));
    }

    public void translateMove(Move move) {
        ValueMeta rightMeta = getValueMeta(move.getRightValue(), true, false);
        ValueMeta leftMeta = getValueMeta(move.getLeftValue(), false, false);
        if (rightMeta instanceof Reg) {
            mipsCodeList.add(new ITypeInstr.IR2I1(addiu, (Reg) leftMeta, (Reg) rightMeta, new Imm(0)));
        } else {
            mipsCodeList.add(new ITypeInstr.li((Reg) leftMeta, (Imm) rightMeta));
        }
    }

    public void translateParaGet(ParaGet paraGet) {
        valueFromArg.add(paraGet.getValue());
        ValueMeta valueMeta = getValueMeta(paraGet.getValue(), false, false);
        mipsCodeList.add(new ITypeInstr.lw((Reg) valueMeta, value2address.get(paraGet.getValue())));
    }

    public void translatePrint(Print print) {
        String formatString = print.getFormatString().substring(1, print.getFormatString().length() - 1);
        int index = formatString.indexOf("%d");
        int count = 1;
        while (index >= 0) {
            String out = formatString.substring(0, index);
            if (!out.isEmpty()) {
                String label = "string" + strCount++;
                macroCodeList.add(new AsciizMacro(label, out));
                mipsCodeList.add(new ITypeInstr.la(Reg.AR, new AbsoluteAddress(label)));
                mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(4)));
                mipsCodeList.add(new Syscall());
            }
            mipsCodeList.add(new ITypeInstr.lw(Reg.AR, new RelativeAddress(Reg.SP, -count * 4)));
            mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(1)));
            mipsCodeList.add(new Syscall());
            formatString = formatString.substring(index + 2);
            index = formatString.indexOf("%d");
            count++;
        }
        if (!formatString.isEmpty()) {
            String label = "string" + strCount++;
            macroCodeList.add(new AsciizMacro(label, formatString));
            mipsCodeList.add(new ITypeInstr.la(Reg.AR, new AbsoluteAddress(label)));
            mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(4)));
            mipsCodeList.add(new Syscall());
        }
        pushCount = 0;
    }

    public void translateReturn(Return ret) {
        if (ret.getValue() != null) {
            ValueMeta valueMeta = getValueMeta(ret.getValue(), true, false);
            if (valueMeta instanceof Imm) {
                mipsCodeList.add(new ITypeInstr.li(Reg.RV, (Imm) valueMeta));
            } else {
                mipsCodeList.add(new ITypeInstr.IR2I1(addiu, Reg.RV, (Reg) valueMeta, new Imm(0)));
            }
        }
        mipsCodeList.add(new ITypeInstr.IR2I1(addiu, Reg.SP, Reg.SP, new Imm(frameSize)));
        mipsCodeList.add(new RTypeInstr.jr(Reg.RA));
    }

    public void translateStore(Store store) {
        if (!store.getLeftValue().isTemp() && !valueFromArg.contains(store.getLeftValue())) {
            Address address = value2address.get(store.getLeftValue());
            ValueMeta rightMeta = getValueMeta(store.getRightValue(), true, false);
            if (rightMeta instanceof Imm) {
                mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
                mipsCodeList.add(new ITypeInstr.sw(Reg.TR, address));
            } else {
                mipsCodeList.add(new ITypeInstr.sw((Reg) rightMeta, address));
            }
        } else {
            ValueMeta leftMeta = getValueMeta(store.getLeftValue(), true, true);
            ValueMeta rightMeta = getValueMeta(store.getRightValue(), true, false);
            if (rightMeta instanceof Reg) {
                mipsCodeList.add(new ITypeInstr.sw((Reg) rightMeta, new RelativeAddress((Reg) leftMeta, 0)));
            } else {
                mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
                mipsCodeList.add(new ITypeInstr.sw(Reg.TR, new RelativeAddress((Reg) leftMeta, 0)));
            }
        }
    }

    public void Peepholes() {
        HashSet<MipsCode> removeSet = new HashSet<>();
        HashMap<MipsCode, MipsCode> replaceMap = new HashMap<>();
        for (int i = 1; i < mipsCodeList.size(); i++) {
            MipsCode prev = mipsCodeList.get(i - 1);
            MipsCode cur = mipsCodeList.get(i);
            if (cur instanceof ITypeInstr.IR2I1 && prev instanceof ITypeInstr.IR2I1) {
                if (((ITypeInstr.IR2I1) cur).getRs() == ((ITypeInstr.IR2I1) prev).getRs() &&
                        ((ITypeInstr.IR2I1) cur).getRt() == ((ITypeInstr.IR2I1) prev).getRt() &&
                        ((ITypeInstr.IR2I1) cur).getRs() == ((ITypeInstr.IR2I1) prev).getRt() &&
                        ((ITypeInstr.IR2I1) cur).getImm().getValue() == -((ITypeInstr.IR2I1) prev).getImm().getValue()) {
                    removeSet.add(prev);
                    removeSet.add(cur);
                }
            } else if (cur instanceof ITypeInstr.IR2I1) {
                if (((ITypeInstr.IR2I1) cur).getRt() == ((ITypeInstr.IR2I1) cur).getRs() && ((ITypeInstr.IR2I1) cur).getImm().getValue() == 0) {
                    removeSet.add(cur);
                }
            } else if (cur instanceof ITypeInstr.beq && prev instanceof RTypeInstr.RR3) {
                if (((RTypeInstr.RR3) prev).getOpCode() == seq) {
                    if (((ITypeInstr.beq) cur).getRs() == ((RTypeInstr.RR3) prev).getRd() &&
                            ((ITypeInstr.beq) cur).getRt() == Reg.ZERO) {
                        removeSet.add(prev);
                        replaceMap.put(cur, new ITypeInstr.bne(((RTypeInstr.RR3) prev).getRs(),
                                ((RTypeInstr.RR3) prev).getRt(), ((ITypeInstr.beq) cur).getLabel()));
                    }
                } else if (((RTypeInstr.RR3) prev).getOpCode() == sne) {
                    if (((ITypeInstr.beq) cur).getRs() == ((RTypeInstr.RR3) prev).getRd() &&
                            ((ITypeInstr.beq) cur).getRt() == Reg.ZERO) {
                        removeSet.add(prev);
                        replaceMap.put(cur, new ITypeInstr.beq(((RTypeInstr.RR3) prev).getRs(),
                                ((RTypeInstr.RR3) prev).getRt(), ((ITypeInstr.beq) cur).getLabel()));
                    }
                }
            } else if (cur instanceof ITypeInstr.bne && prev instanceof RTypeInstr.RR3) {
                if (((RTypeInstr.RR3) prev).getOpCode() == seq) {
                    if (((ITypeInstr.bne) cur).getRs() == ((RTypeInstr.RR3) prev).getRd() &&
                            ((ITypeInstr.bne) cur).getRt() == Reg.ZERO) {
                        removeSet.add(prev);
                        replaceMap.put(cur, new ITypeInstr.beq(((RTypeInstr.RR3) prev).getRs(),
                                ((RTypeInstr.RR3) prev).getRt(), ((ITypeInstr.bne) cur).getLabel()));
                    }
                } else if (((RTypeInstr.RR3) prev).getOpCode() == sne) {
                    if (((ITypeInstr.bne) cur).getRs() == ((RTypeInstr.RR3) prev).getRd() &&
                            ((ITypeInstr.bne) cur).getRt() == Reg.ZERO) {
                        removeSet.add(prev);
                        replaceMap.put(cur, new ITypeInstr.bne(((RTypeInstr.RR3) prev).getRs(),
                                ((RTypeInstr.RR3) prev).getRt(), ((ITypeInstr.bne) cur).getLabel()));
                    }
                }
            }
        }
        removeSet.forEach(mipsCodeList::remove);
        replaceMap.forEach((k, v) -> mipsCodeList.set(mipsCodeList.indexOf(k), v));
    }

    @Override
    public String toString() {
        Peepholes();
        mipsCode.add(".data");
        macroCodeList.forEach(code -> mipsCode.add(code.toString()));
        mipsCode.add(".text");
        mipsCodeList.forEach(code -> mipsCode.add(code.toString()));
        return mipsCode.toString();
    }
}
