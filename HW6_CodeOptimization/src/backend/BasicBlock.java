package backend;

import midend.MidCode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;

public class BasicBlock {
    private final FuncBlock funcBlock;
    private MidCode head;
    private MidCode tail;
    private final HashSet<BasicBlock> prev = new HashSet<>();
    private final HashSet<BasicBlock> next = new HashSet<>();
    private final HashSet<Value> defValSet = new HashSet<>();
    private final HashSet<Value> useValSet = new HashSet<>();
    private final HashSet<MidCode> genCodeSet = new HashSet<>();
    private final HashSet<MidCode> killCodeSet = new HashSet<>();
    private final HashSet<MidCode> reachInSet = new HashSet<>();
    private final HashSet<MidCode> reachOutSet = new HashSet<>();
    private final HashSet<Value> liveInSet = new HashSet<>();
    private final HashSet<Value> liveOutSet = new HashSet<>();
    private final HashMap<MidCode, HashSet<Value>> code2LiveOutSet = new HashMap<>();
    private final HashMap<Value, HashSet<MidCode>> useTime = new HashMap<>();

    public BasicBlock(FuncBlock funcBlock, MidCode head, MidCode tail) {
        this.funcBlock = funcBlock;
        this.head = head;
        this.tail = tail;
    }

    public MidCode getHead() {
        return head;
    }

    public MidCode getTail() {
        return tail;
    }

    public HashSet<BasicBlock> getPrev() {
        return prev;
    }

    public HashSet<BasicBlock> getNext() {
        return next;
    }

    public HashSet<MidCode> getMidCodeList() {
        HashSet<MidCode> midCodeList = new HashSet<>();
        MidCode cur = head;
        while (cur != tail) {
            midCodeList.add(cur);
            cur = cur.getNext();
        }
        midCodeList.add(tail);
        return midCodeList;
    }

    public HashSet<Value> getUseValSet() {
        return useValSet;
    }

    public HashSet<MidCode> getReachInSet() {
        return reachInSet;
    }

    public HashSet<Value> getLiveOutSet() {
        return liveOutSet;
    }

    public void link(BasicBlock next) {
        this.next.add(next);
        next.prev.add(this);
    }

    public void flushAll() {
        defValSet.clear();
        useValSet.clear();
        genCodeSet.clear();
        killCodeSet.clear();
        reachInSet.clear();
        reachOutSet.clear();
        liveInSet.clear();
        liveOutSet.clear();
    }

    public void generateDefUse() {
        MidCode midCode = head;
        while (midCode != tail.getNext()) {
            if (midCode instanceof UseVal) {
                ((UseVal) midCode).getUseVal().stream().filter(value -> !(defValSet.contains(value) || value instanceof Imm)).forEach(useValSet::add);
            }
            if (midCode instanceof DefVal) {
                Value defVal = ((DefVal) midCode).getDefVal();
                funcBlock.setVal(defVal, midCode);
                if (!useValSet.contains(defVal)) {
                    defValSet.add(defVal);
                }
            }
            midCode = midCode.getNext();
        }
    }

    public void generateGenKill() {
        MidCode midCode = tail;
        while (midCode != head.getPrev()) {
            if (midCode instanceof DefVal) {
                if (!killCodeSet.contains(midCode)) {
                    genCodeSet.add(midCode);
                }
                killCodeSet.addAll(funcBlock.getMidCode(((DefVal) midCode).getDefVal()));
            }
            midCode = midCode.getPrev();
        }
    }

    public boolean generateReachInOut() {
        HashSet<MidCode> oldReachOutSet = new HashSet<>(reachOutSet);
        reachOutSet.addAll(genCodeSet);
        prev.forEach(basicBlock -> reachInSet.addAll(basicBlock.reachOutSet));
        for (MidCode midCode : reachInSet) {
            if (!killCodeSet.contains(midCode)) {
                reachOutSet.add(midCode);
            }
        }
        return !oldReachOutSet.equals(reachOutSet);
    }

    public boolean propagate() {
        boolean changed = false;
        MidCode midCode = head;
        HashMap<Value, MidCode> intraDef = new HashMap<>();
        while (midCode != tail.getNext()) {
            if (midCode instanceof UseVal) {
                LinkedList<Value> useValList = ((UseVal) midCode).getUseVal();
                for (Value useVal : useValList) {
                    if (!useVal.isGlobal() && !useVal.isReturn()) {
                        if (intraDef.containsKey(useVal)) {
                            MidCode defMidCode = intraDef.get(useVal);
                            if (!useVal.isRunTimeInvariant(defMidCode, midCode)) {
                                continue;
                            }
                            if (midCode instanceof Declare) {
                                if (defMidCode instanceof Move) {
                                    Value value = ((Move) defMidCode).getRightValue();
                                    if (value instanceof Imm || (value instanceof Word && !value.isGlobal() && !value.isReturn())) {
                                        ((Declare) midCode).replaceUseVal(useVal, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            } else if (midCode instanceof Move) {
                                if (defMidCode instanceof Declare) {
                                    Declare declare = (Declare) defMidCode;
                                    if (declare.getUseVal().size() == 1 && declare.getValue() instanceof Word) {
                                        Value value = declare.getUseVal().get(0);
                                        if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                            ((Move) midCode).replaceUseVal(useVal, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                } else if (defMidCode instanceof Load) {
                                    Load load = (Load) defMidCode;
                                    Addr value = load.getRightValue();
                                    if (!value.isGlobal() && !value.isReturn()) {
                                        Load newLoad = new Load(load.isTemp(), ((Move) midCode).getLeftValue(), value);
                                        midCode.replaceBy(newLoad);
                                        return true;
                                    }
                                } else if (defMidCode instanceof Move) {
                                    Move move = (Move) defMidCode;
                                    Value value = move.getUseVal().get(0);
                                    if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                        ((Move) midCode).replaceUseVal(useVal, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            } else if (defMidCode instanceof Copy && ((Copy) defMidCode).getSrc().size() == 1) {
                                Value value = ((Copy) defMidCode).getSrc().get(0);
                                if (defMidCode instanceof Declare && ((Declare) defMidCode).getDefVal() instanceof Addr) {
                                    continue;
                                }
                                if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                    ((UseVal) midCode).replaceUseVal(useVal, value);
                                    changed = true;
                                    break;
                                }
                            }
                        } else if (useVal instanceof Word) {
                            LinkedList<MidCode> valReachInList = new LinkedList<>();
                            reachInSet.forEach(item -> {
                                if (item instanceof DefVal && ((DefVal) item).getDefVal().equals(useVal)) {
                                    valReachInList.add(item);
                                }
                            });
                            if (valReachInList.size() == 1) {
                                MidCode defMidCode = valReachInList.get(0);
                                if (!isDfsInvariant(defMidCode, midCode)) {
                                    continue;
                                }
                                if (midCode instanceof Declare) {
                                    if (defMidCode instanceof Move) {
                                        Value value = ((Move) defMidCode).getRightValue();
                                        if (value instanceof Imm || (value instanceof Word && !value.isGlobal() && !value.isReturn())) {
                                            ((Declare) midCode).replaceUseVal(useVal, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                } else if (midCode instanceof Move) {
                                    if (defMidCode instanceof Declare) {
                                        Declare declare = (Declare) defMidCode;
                                        if (declare.getUseVal().size() == 1 && declare.getValue() instanceof Word) {
                                            Value value = declare.getUseVal().get(0);
                                            if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                                ((Move) midCode).replaceUseVal(useVal, value);
                                                changed = true;
                                                break;
                                            }
                                        }
                                    } else if (defMidCode instanceof Load) {
                                        Load load = (Load) defMidCode;
                                        Addr value = load.getRightValue();
                                        if (!value.isGlobal() && !value.isReturn()) {
                                            Load newLoad = new Load(load.isTemp(), ((Move) midCode).getLeftValue(), value);
                                            midCode.replaceBy(newLoad);
                                            return true;
                                        }
                                    } else if (defMidCode instanceof Move) {
                                        Move move = (Move) defMidCode;
                                        Value value = move.getUseVal().get(0);
                                        if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                            ((Move) midCode).replaceUseVal(useVal, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                } else if (defMidCode instanceof Copy && ((Copy) defMidCode).getSrc().size() == 1) {
                                    Value value = ((Copy) defMidCode).getSrc().get(0);
                                    if (defMidCode instanceof Declare && ((Declare) defMidCode).getDefVal() instanceof Addr) {
                                        continue;
                                    }
                                    if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                        ((UseVal) midCode).replaceUseVal(useVal, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (midCode instanceof DefVal) {
                Value defVal = ((DefVal) midCode).getDefVal();
                intraDef.put(defVal, midCode);
            }
            midCode = midCode.getNext();
        }
        return changed;
    }

    public boolean isDfsInvariant(MidCode defMidCode, MidCode midCode) {
        if (defMidCode instanceof Load) {
            return false;
        }
        LinkedList<Value> useValList = new LinkedList<>();
        if (defMidCode instanceof UseVal) {
            useValList.addAll(((UseVal) defMidCode).getUseVal());
        }
        HashSet<BasicBlock> visited = new HashSet<>();
        for (BasicBlock basicBlock : funcBlock.getBasicBlockList()) {
            if (basicBlock.getMidCodeList().contains(defMidCode)) {
                return dfsInvariant(basicBlock, useValList, defMidCode, midCode, visited);
            }
        }
        return false;
    }

    public boolean dfsInvariant(BasicBlock basicBlock, LinkedList<Value> useValList, MidCode beginCode, MidCode endCode, HashSet<BasicBlock> visited) {
        MidCode curCode = beginCode;
        visited.add(basicBlock);
        while (curCode != endCode) {
            if (curCode instanceof DefVal && useValList.contains(((DefVal) curCode).getDefVal())) {
                return false;
            }
            if (curCode == basicBlock.tail) {
                break;
            }
            curCode = curCode.getNext();
        }
        if (curCode == endCode) {
            return true;
        }
        for (BasicBlock item : basicBlock.next) {
            if (!visited.contains(item)) {
                if (!dfsInvariant(item, useValList, item.head, endCode, visited)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean generateLiveInOut() {
        HashSet<Value> oldLiveInSet = new HashSet<>(liveInSet);
        liveInSet.addAll(useValSet);
        next.forEach(basicBlock -> liveOutSet.addAll(basicBlock.liveInSet));
        for (Value value : liveOutSet) {
            if (!defValSet.contains(value)) {
                liveInSet.add(value);
            }
        }
        return !oldLiveInSet.equals(liveInSet);
    }

    public boolean removeDeadCode() {
        boolean changed = false;
        if (!funcBlock.isBegin(this) && prev.isEmpty()) {
            MidCode midCode = head;
            while (midCode != tail.getNext()) {
                Nop nop = new Nop();
                midCode.replaceBy(nop);
                if (midCode == head) {
                    head = nop;
                }
                if (midCode == tail) {
                    tail = nop;
                }
                midCode = nop.getNext();
            }
            return true;
        } else {
            MidCode midCode = head;
            while (midCode != tail.getNext()) {
                if (midCode instanceof DefVal) {
                    boolean used = false;
                    Value defVal = ((DefVal) midCode).getDefVal();
                    MidCode item = midCode.getNext();
                    while (item != tail.getNext()) {
                        if (item instanceof UseVal && ((UseVal) item).getUseVal().contains(defVal)) {
                            used = true;
                            break;
                        }
                        item = item.getNext();
                    }
                    if (!used && !liveOutSet.contains(defVal) && !defVal.isGlobal() && !defVal.isReturn()) {
                        midCode.replaceBy(new Nop());
                        changed = true;
                    }
                }
                midCode = midCode.getNext();
            }
        }
        return changed;
    }

    public void generateLiveOutOfCode() {
        MidCode code = head;
        while (code != tail.getNext()) {
            HashSet<Value> liveSet = new HashSet<>(liveOutSet);
            MidCode midCode = tail;
            while (midCode != code) {
                if (midCode instanceof DefVal) {
                    Value defVal = ((DefVal) midCode).getDefVal();
                    liveSet.remove(defVal);
                }
                if (midCode instanceof UseVal) {
                    for (Value useVal : ((UseVal) midCode).getUseVal()) {
                        if (!(useVal instanceof Imm)) {
                            liveSet.add(useVal);
                        }
                    }
                }
                midCode = midCode.getPrev();
            }
            code2LiveOutSet.put(code, liveSet);
            code = code.getNext();
        }
    }

    public boolean isLive(Value value, MidCode code) {
        return code2LiveOutSet.get(code).contains(value);
    }

    public void generateUseTime() {
        MidCode midCode = head;
        while (midCode != tail.getNext()) {
            if (midCode instanceof UseVal) {
                for (Value useVal : ((UseVal) midCode).getUseVal()) {
                    if (!useVal.isGlobal() && !useVal.isReturn()) {
                        if (!useTime.containsKey(useVal)) {
                            useTime.put(useVal, new HashSet<>());
                        }
                        useTime.get(useVal).add(midCode);
                    }
                }
            }
            midCode = midCode.getNext();
        }
    }

    public boolean usedUp(Value useVal, MidCode midCode) {
        if (!useTime.containsKey(useVal)) {
            return false;
        } else {
            HashSet<MidCode> useSet = useTime.get(useVal);
            useSet.remove(midCode);
            return useSet.isEmpty();
        }
    }

    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        MidCode midCode = head;
        while (midCode != tail.getNext()) {
            if (midCode != null) {
                stringJoiner.add(midCode.toString());
            } else {
                stringJoiner.add("null");
                break;
            }
            midCode = midCode.getNext();
        }
        return stringJoiner.toString();
    }
}
