package midend.MidCode;

import backend.FuncBlock;
import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;

public class MidCodeTable {
    private static final MidCodeTable MID_CODE_TABLE = new MidCodeTable();
    private static String curFunc = "@global";
    private static final LinkedList<Label> loopBegins = new LinkedList<>();
    private static final LinkedList<Label> loopEnds = new LinkedList<>();
    private static final LinkedList<MidCode> macroList = new LinkedList<>();
    private static final LinkedList<MidCode> midCodeList = new LinkedList<>();
    private static final HashMap<String, LinkedList<Value>> func2valList = new HashMap<>();
    private static final HashMap<Value, Integer> val2size = new HashMap<>();
    private static final LinkedList<FuncBlock> funcBlockList = new LinkedList<>();
    private static final HashSet<Label> loopMark = new HashSet<>();
    private static final MidCode head = new Nop();
    private static MidCode tail = head;

    public static MidCodeTable getInstance() {
        return MID_CODE_TABLE;
    }

    static {
        func2valList.put("@global", new LinkedList<>());
    }

    public Label getLoopBegin() {
        return loopBegins.getLast();
    }

    public Label getLoopEnd() {
        return loopEnds.getLast();
    }

    public LinkedList<MidCode> getMacroList() {
        return macroList;
    }

    public LinkedList<Value> getValInfos(String func) {
        return func2valList.get(func);
    }

    public int getValSize(Value value) {
        return val2size.get(value);
    }

    public LinkedList<FuncBlock> getFuncBlockList() {
        return funcBlockList;
    }

    public HashSet<Label> getLoopMark() {
        return loopMark;
    }

    public void setCurFunc(String func) {
        curFunc = func;
        func2valList.putIfAbsent(func, new LinkedList<>());
    }

    public void setLoop(Label loopBegin, Label loopEnd) {
        loopBegins.add(loopBegin);
        loopEnds.add(loopEnd);
    }

    public void markLoop(Label stmtBegin) {
        loopMark.add(stmtBegin);
    }

    public void unsetLoop() {
        loopBegins.removeLast();
        loopEnds.removeLast();
    }

    public void addMidCode(MidCode midCode) {
        if (curFunc.equals("@global")) {
            macroList.add(midCode);
        } else if (curFunc.equals("main") && midCode instanceof Return) {
            Exit exit = new Exit();
            midCodeList.add(exit);
            tail = tail.link(exit);
        } else {
            midCodeList.add(midCode);
            tail = tail.link(midCode);
        }
    }

    public void addVarInfo(Value value, int size) {
        func2valList.get(curFunc).add(value);
        val2size.put(value, size);
    }

    public void simplify() {
        boolean changed;
        do {
            changed = false;
            simplifyNop();
            simplifyLabel();
            simplifyExp();
            divideFuncBlock();
            for (FuncBlock funcBlock : funcBlockList) {
                changed |= funcBlock.simplify();
            }
            if (!changed) {
                for (FuncBlock funcBlock : funcBlockList) {
                    changed |= funcBlock.simplifyLoop();
                }
            }
        } while (changed);
        midCodeList.clear();
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            midCodeList.add(midCode);
        }
        funcBlockList.forEach(FuncBlock::generateLiveOutOfCode);
        funcBlockList.forEach(FuncBlock::allocateRegister);
        funcBlockList.forEach(FuncBlock::generateUseTime);
    }

    public void simplifyNop() {
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            if (midCode instanceof Nop) {
                midCode.delete();
            }
        }
    }

    public void simplifyLabel() {
        HashSet<Label> usedLabels = new HashSet<>();
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            if (midCode instanceof Jump) {
                Jump jump = (Jump) midCode;
                Label target = jump.getLabel().getTarget();
                if (midCode.getNext() == target.getMidCode()) {
                    midCode.delete();
                } else {
                    jump.setLabel(target);
                    usedLabels.add(target);
                }
            } else if (midCode instanceof Branch) {
                Branch branch = (Branch) midCode;
                Label target = branch.getBranchLabel().getTarget();
                if (midCode.getNext() == target.getMidCode()) {
                    midCode.delete();
                } else if (midCode.getNext() != null) {
                    MidCode nextCode = midCode.getNext();
                    if (nextCode instanceof Jump && nextCode.getNext() == branch.getBranchLabel().getMidCode() &&
                            LabelTable.getInstance().getLabelList(nextCode).isEmpty()) {
                        ((Branch) midCode).negative(((Jump) nextCode).getLabel());
                        usedLabels.add(((Jump) nextCode).getLabel());
                        nextCode.delete();
                    } else {
                        branch.setLabel(target);
                        usedLabels.add(target);
                    }
                } else {
                    branch.setLabel(target);
                    usedLabels.add(target);
                }
            }
        }
        LabelTable.getInstance().removeUnusedLabels(usedLabels);
    }

    public void simplifyExp() {
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            if (midCode instanceof Assign) {
                ((Assign) midCode).simplify();
            } else if (midCode instanceof Branch) {
                ((Branch) midCode).simplify();
            } else if (midCode instanceof Move) {
                ((Move) midCode).simplify();
            }
        }
    }

    public void divideFuncBlock() {
        MidCode curHead = MidCodeTable.head.getNext();
        MidCode curTail = curHead;
        funcBlockList.clear();
        while (curTail.getNext() != null) {
            if (curTail.getNext() instanceof FuncEntry) {
                funcBlockList.add(new FuncBlock(curHead, curTail));
                curHead = curTail.getNext();
            }
            curTail = curTail.getNext();
        }
        funcBlockList.add(new FuncBlock(curHead, curTail));
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (MidCode midCode : macroList) {
            stringJoiner.add(midCode.toString());
        }
        for (MidCode midCode : midCodeList) {
            for (Label label : LabelTable.getInstance().getLabelList(midCode)) {
                stringJoiner.add(label.toString());
            }
            stringJoiner.add(midCode.toString());
        }
        return stringJoiner.toString();
    }
}
