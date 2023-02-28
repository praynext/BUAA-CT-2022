package midend.MidCode;

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
    private static final LinkedList<MidCode> globalCodeList = new LinkedList<>();
    private static final LinkedList<MidCode> midCodeList = new LinkedList<>();
    private static final LinkedList<MidCode> simMidCodeList = new LinkedList<>();
    private static final HashMap<String, LinkedList<Value>> func2valList = new HashMap<>();
    private static final HashMap<Value, Integer> val2size = new HashMap<>();

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

    public LinkedList<MidCode> getGlobalCodeList() {
        return globalCodeList;
    }

    public LinkedList<MidCode> getMidCodeList() {
        return midCodeList;
    }

    public LinkedList<Value> getValInfos(String func) {
        return func2valList.get(func);
    }

    public int getValSize(Value value) {
        return val2size.get(value);
    }

    public void setCurFunc(String func) {
        curFunc = func;
        func2valList.putIfAbsent(func, new LinkedList<>());
    }

    public void setLoop(Label loopBegin, Label loopEnd) {
        loopBegins.add(loopBegin);
        loopEnds.add(loopEnd);
    }

    public void unsetLoop() {
        loopBegins.removeLast();
        loopEnds.removeLast();
    }

    public void addMidCode(MidCode midCode) {
        if (curFunc.equals("@global")) {
            globalCodeList.add(midCode);
        } else {
            midCodeList.add(midCode);
        }
    }

    public void addVarInfo(Value value, int size) {
        func2valList.get(curFunc).add(value);
        val2size.put(value, size);
    }

    public void simplify() {
        simplifyNop();
        simplifyLabel();
        simplifyExp();
    }

    public void simplifyNop() {
        int index;
        for (MidCode midCode : midCodeList) {
            if (midCode instanceof Nop) {
                index = midCodeList.indexOf(midCode);
                for (Label label : LabelTable.getInstance().getLabelList(midCode)) {
                    label.setMidCode(midCodeList.get(index + 1));
                }
                midCodeList.remove(index);
            }
        }
    }

    public void simplifyLabel() {
        int index;
        HashSet<Label> usedLabels = new HashSet<>();
        for (MidCode midCode : midCodeList) {
            if (midCode instanceof Jump) {
                Jump jump = (Jump) midCode;
                Label target = jump.getLabel().getTarget();
                index = midCodeList.indexOf(jump);
                if (midCodeList.indexOf(target.getMidCode()) - index == 1) {
                    midCodeList.remove(index);
                } else {
                    jump.setLabel(target);
                    usedLabels.add(target);
                }
            } else if (midCode instanceof Branch) {
                Branch branch = (Branch) midCode;
                Label target = branch.getBranchLabel().getTarget();
                index = midCodeList.indexOf(branch);
                if (midCodeList.indexOf(target.getMidCode()) - index == 1) {
                    midCodeList.remove(index);
                } else {
                    branch.setLabel(target);
                    usedLabels.add(target);
                }
            }
        }
        LabelTable.getInstance().removeUnusedLabels(usedLabels);
    }

    public void simplifyExp() {
        for (MidCode midCode : midCodeList) {
            if (midCode instanceof Assign) {
                ((Assign) midCode).simplify();
            } else {

            }
        }
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (MidCode midCode : globalCodeList) {
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
