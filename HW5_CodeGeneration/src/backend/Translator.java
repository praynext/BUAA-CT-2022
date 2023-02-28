package backend;

import backend.MipsCode.*;
import midend.LabelTable.LabelTable;
import midend.MidCode.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringJoiner;

import static backend.MipsCode.ITypeInstr.IOpCode.addiu;
import static backend.MipsCode.RTypeInstr.ROpCode.*;
import static backend.MipsCode.RTypeInstr.ROpCode.and;

public class Translator {
    private static final Translator TRANSLATOR = new Translator();
    private static final MidCodeTable midCodeTable = MidCodeTable.getInstance();
    private static final LabelTable labelTable = LabelTable.getInstance();
    private static final LinkedList<MipsCode> macroCodeList = new LinkedList<>();
    private static final LinkedList<MipsCode> mipsCodeList = new LinkedList<>();
    private static final StringJoiner mipsCode = new StringJoiner("\n");
    private static final RegScheduler scheduler = new RegScheduler();
    private static final LinkedList<Reg> synchronizedReg = new LinkedList<>();
    private static final HashMap<Value, Address> value2address = new HashMap<>();
    private final LinkedList<Value> valueFromArg = new LinkedList<>();
    private int frameSize;
    private int pushCount = 0;
    private int strCount = 0;

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

    public void translate() {
        for (MidCode midCode : midCodeTable.getGlobalCodeList()) {
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
        mipsCodeList.add(new ITypeInstr.IR2I1(addiu, Reg.SP, Reg.SP, new Imm(-4)));
        for (MidCode midCode : midCodeTable.getMidCodeList()) {
            synchronizedReg.clear();
            mipsCodeList.add(new Comment(midCode.toString()));
            if (midCode instanceof Branch || midCode instanceof Return || midCode instanceof Jump) {
                scheduler.flush();
            }
            if (labelTable.getLabelList(midCode).size() != 0) {
                scheduler.flush();
                mipsCodeList.addAll(labelTable.getLabelList(midCode));
            }
            switch (midCode.getClass().toString()) {
                case "class midend.MidCode.ArgPush":
                    assert midCode instanceof ArgPush;
                    generateMips((ArgPush) midCode);
                    break;
                case "class midend.MidCode.Assign":
                    assert midCode instanceof Assign;
                    generateMips((Assign) midCode);
                    break;
                case "class midend.MidCode.Branch":
                    assert midCode instanceof Branch;
                    generateMips((Branch) midCode);
                    break;
                case "class midend.MidCode.Declare":
                    assert midCode instanceof Declare;
                    generateMips((Declare) midCode);
                    break;
                case "class midend.MidCode.Exit":
                    assert midCode instanceof Exit;
                    generateMips((Exit) midCode);
                    break;
                case "class midend.MidCode.FuncCall":
                    assert midCode instanceof FuncCall;
                    generateMips((FuncCall) midCode);
                    break;
                case "class midend.MidCode.FuncEntry":
                    assert midCode instanceof FuncEntry;
                    generateMips((FuncEntry) midCode);
                    break;
                case "class midend.MidCode.IntGet":
                    assert midCode instanceof IntGet;
                    generateMips((IntGet) midCode);
                    break;
                case "class midend.MidCode.Jump":
                    assert midCode instanceof Jump;
                    generateMips((Jump) midCode);
                    break;
                case "class midend.MidCode.Load":
                    assert midCode instanceof Load;
                    generateMips((Load) midCode);
                    break;
                case "class midend.MidCode.Move":
                    assert midCode instanceof Move;
                    generateMips((Move) midCode);
                    break;
                case "class midend.MidCode.ParaGet":
                    assert midCode instanceof ParaGet;
                    generateMips((ParaGet) midCode);
                    break;
                case "class midend.MidCode.Print":
                    assert midCode instanceof Print;
                    generateMips((Print) midCode);
                    break;
                case "class midend.MidCode.Return":
                    assert midCode instanceof Return;
                    generateMips((Return) midCode);
                    break;
                case "class midend.MidCode.Store":
                    assert midCode instanceof Store;
                    generateMips((Store) midCode);
                    break;
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

    public void generateMips(ArgPush argPush) {
        pushCount++;
        ValueMeta valueMeta = getValueMeta(argPush.getValue(), true, false);
        if (valueMeta instanceof Reg) {
            mipsCodeList.add(new ITypeInstr.sw((Reg) valueMeta, new RelativeAddress(Reg.SP, -pushCount * 4)));
        } else {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) valueMeta));
            mipsCodeList.add(new ITypeInstr.sw(Reg.TR, new RelativeAddress(Reg.SP, -pushCount * 4)));
        }
    }

    public void generateMips(Assign assign) {
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
                    generateMips(binaryOp, valMeta, leftMeta, Reg.TR);
                } else {
                    mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.TR, (Reg) rightMeta, new Imm(2)));
                    generateMips(binaryOp, valMeta, leftMeta, Reg.TR);
                }
            } else if (rightValue instanceof Addr) {
                if (leftMeta instanceof Imm) {
                    mipsCodeList.add(new ITypeInstr.li(Reg.TR, new Imm(((Imm) leftMeta).getValue() * 4)));
                    generateMips(binaryOp, valMeta, Reg.TR, rightMeta);
                } else {
                    mipsCodeList.add(new RTypeInstr.RR2I1(sll, Reg.TR, (Reg) leftMeta, new Imm(2)));
                    generateMips(binaryOp, valMeta, Reg.TR, rightMeta);
                }
            } else {
                generateMips(binaryOp, valMeta, leftMeta, rightMeta);
            }
        } else {
            UnaryOperate.UnaryOp unaryOp = ((UnaryOperate) assign.getRightValue()).getUnaryOp();
            Reg valMeta = (Reg) getValueMeta(assign.getLeftValue(), false, false);
            ValueMeta rightMeta = getValueMeta(((UnaryOperate) assign.getRightValue()).getValue(), true, false);
            generateMips(unaryOp, valMeta, rightMeta);
        }
    }

    public void generateMips(BinaryOperate.BinaryOp binaryOp, Reg valMeta, ValueMeta leftMeta, ValueMeta rightMeta) {
        if (leftMeta instanceof Imm && rightMeta instanceof Imm) {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) leftMeta));
            mipsCodeList.add(new ITypeInstr.li(valMeta, (Imm) rightMeta));
            generateMips(valMeta, binaryOp, Reg.TR, valMeta);
        } else if (leftMeta instanceof Imm) {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) leftMeta));
            generateMips(valMeta, binaryOp, Reg.TR, (Reg) rightMeta);
        } else if (rightMeta instanceof Imm) {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
            generateMips(valMeta, binaryOp, (Reg) leftMeta, Reg.TR);
        } else {
            generateMips(valMeta, binaryOp, (Reg) leftMeta, (Reg) rightMeta);
        }
    }

    private void generateMips(Reg valMeta, BinaryOperate.BinaryOp binaryOp, Reg leftReg, Reg rightReg) {
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
            case BITAND:
                mipsCodeList.add(new RTypeInstr.RR3(and, valMeta, leftReg, rightReg));
                break;
        }
    }

    public void generateMips(UnaryOperate.UnaryOp unaryOp, Reg valMeta, ValueMeta rightMeta) {
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

    public void generateMips(Branch branch) {
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

    public void generateMips(Declare declare) {
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

    public void generateMips(Exit exit) {
        mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(10)));
        mipsCodeList.add(new Syscall());
    }

    public void generateMips(FuncCall funcCall) {
        pushCount = 0;
        HashMap<Reg, Value> reg2value = scheduler.getReg2value();
        reg2value.forEach((reg, value) -> {
            if (value2address.containsKey(value)) {
                if (value instanceof Word || (value instanceof Addr && ((Addr) value).isTemp())) {
                    mipsCodeList.add(new ITypeInstr.sw(reg, value2address.get(value)));
                }
            }
        });
        mipsCodeList.add(new ITypeInstr.sw(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        mipsCodeList.add(new JTypeInstr.jal(funcCall.getIdent()));
        mipsCodeList.add(new ITypeInstr.lw(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        scheduler.clear();
    }

    public void generateMips(FuncEntry funcEntry) {
        scheduler.clear();
        valueFromArg.clear();
        int fp = 4;
        LinkedList<Value> values = MidCodeTable.getInstance().getValInfos(funcEntry.getEntryLabel().getLabelName());
        for (int i = values.size() - 1; i >= 0; i--) {
            value2address.put(values.get(i), new RelativeAddress(Reg.SP, fp));
            fp += MidCodeTable.getInstance().getValSize(values.get(i)) * 4;
            System.out.println(values.get(i) + " " + value2address.get(values.get(i)));
        }
        frameSize = fp;
        mipsCodeList.add(new ITypeInstr.IR2I1(addiu, Reg.SP, Reg.SP, new Imm(-fp)));
    }

    public void generateMips(IntGet intGet) {
        mipsCodeList.add(new ITypeInstr.li(Reg.RV, new Imm(5)));
        mipsCodeList.add(new Syscall());
    }

    public void generateMips(Jump jump) {
        mipsCodeList.add(new JTypeInstr.j(jump.getLabel().getLabelName()));
    }

    public void generateMips(Load load) {
        ValueMeta leftMeta = getValueMeta(load.getLeftValue(), false, false);
        ValueMeta rightMeta = getValueMeta(load.getRightValue(), true, false);
        mipsCodeList.add(new ITypeInstr.lw((Reg) leftMeta, new RelativeAddress((Reg) rightMeta, 0)));
    }

    public void generateMips(Move move) {
        ValueMeta leftMeta = getValueMeta(move.getLeftValue(), false, false);
        ValueMeta rightMeta = getValueMeta(move.getRightValue(), true, false);
        if (rightMeta instanceof Reg) {
            mipsCodeList.add(new ITypeInstr.IR2I1(addiu, (Reg) leftMeta, (Reg) rightMeta, new Imm(0)));
        } else {
            mipsCodeList.add(new ITypeInstr.li((Reg) leftMeta, (Imm) rightMeta));
        }
    }

    public void generateMips(ParaGet paraGet) {
        valueFromArg.add(paraGet.getValue());
        ValueMeta valueMeta = getValueMeta(paraGet.getValue(), false, false);
        mipsCodeList.add(new ITypeInstr.lw((Reg) valueMeta, value2address.get(paraGet.getValue())));
    }

    public void generateMips(Print print) {
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

    public void generateMips(Return ret) {
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

    public void generateMips(Store store) {
        ValueMeta leftMeta = getValueMeta(store.getLeftValue(), true, true);
        ValueMeta rightMeta = getValueMeta(store.getRightValue(), true, false);
        if (rightMeta instanceof Reg) {
            mipsCodeList.add(new ITypeInstr.sw((Reg) rightMeta, new RelativeAddress((Reg) leftMeta, 0)));
        } else {
            mipsCodeList.add(new ITypeInstr.li(Reg.TR, (Imm) rightMeta));
            mipsCodeList.add(new ITypeInstr.sw(Reg.TR, new RelativeAddress((Reg) leftMeta, 0)));
        }
    }

    @Override
    public String toString() {
        mipsCode.add(".data");
        macroCodeList.forEach(code -> mipsCode.add(code.toString()));
        mipsCode.add(".text");
        mipsCodeList.forEach(code -> mipsCode.add(code.toString()));
        return mipsCode.toString();
    }
}
