package backend;

import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;
import midend.MidCode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class FuncBlock {
    private final MidCode head;
    private final MidCode tail;
    private final LinkedList<BasicBlock> basicBlockList = new LinkedList<>();
    private final HashMap<Value, LinkedList<MidCode>> val2MidCode = new HashMap<>();
    private final LinkedList<LoopBlock> loopBlockList = new LinkedList<>();
    private final ConflictGraph conflictGraph = new ConflictGraph(this);
    private final HashMap<Value, Integer> val2Depth = new HashMap<>();
    private final HashMap<Value, Reg> val2Reg = new HashMap<>();

    public FuncBlock(MidCode head, MidCode tail) {
        this.head = head;
        this.tail = tail;
        divideBasicBlock();
    }

    public LinkedList<BasicBlock> getBasicBlockList() {
        return basicBlockList;
    }

    public HashMap<Value, Reg> getVal2Reg() {
        return val2Reg;
    }

    public void divideBasicBlock() {
        MidCode curHead = head;
        MidCode curTail = head;
        while (curTail != tail) {
            if (!LabelTable.getInstance().getLabelList(curTail.getNext()).isEmpty() || curTail instanceof Jump || curTail instanceof Branch || curTail instanceof Return) {
                basicBlockList.add(new BasicBlock(this, curHead, curTail));
                curHead = curTail.getNext();
            }
            curTail = curTail.getNext();
        }
        basicBlockList.add(new BasicBlock(this, curHead, curTail));
        for (int index = 0; index < basicBlockList.size(); index++) {
            if (basicBlockList.get(index).getTail() instanceof Jump) {
                Jump jump = (Jump) basicBlockList.get(index).getTail();
                MidCode midCode = jump.getLabel().getMidCode();
                for (BasicBlock basicBlock : basicBlockList) {
                    if (basicBlock.getHead() == midCode) {
                        basicBlockList.get(index).link(basicBlock);
                        break;
                    }
                }
            } else if (basicBlockList.get(index).getTail() instanceof Branch) {
                Branch branch = (Branch) basicBlockList.get(index).getTail();
                MidCode midCode = branch.getBranchLabel().getMidCode();
                for (BasicBlock basicBlock : basicBlockList) {
                    if (basicBlock.getHead() == midCode) {
                        basicBlockList.get(index).link(basicBlock);
                        break;
                    }
                }
                if (index != basicBlockList.size() - 1) {
                    basicBlockList.get(index).link(basicBlockList.get(index + 1));
                }
            } else if (!(basicBlockList.get(index).getTail() instanceof Return) && index != basicBlockList.size() - 1) {
                basicBlockList.get(index).link(basicBlockList.get(index + 1));
            }
        }
    }

    public boolean simplify() {
        basicBlockList.forEach(BasicBlock::flushAll);
        generateDefUse();
        generateGenKill();
        generateReachInOut();
        if (propagate()) {
            return true;
        }
        generateLiveInOut();
        return removeDeadCode();
    }

    public boolean simplifyLoop() {
        divideLoopBlock();
        return extractLoopInvariant();
    }

    public void generateDefUse() {
        for (BasicBlock basicBlock : basicBlockList) {
            basicBlock.generateDefUse();
        }
    }

    public void setVal(Value val, MidCode midCode) {
        if (!val2MidCode.containsKey(val)) {
            val2MidCode.put(val, new LinkedList<>());
        }
        val2MidCode.get(val).add(midCode);
    }

    public void generateGenKill() {
        for (BasicBlock basicBlock : basicBlockList) {
            basicBlock.generateGenKill();
        }
    }

    public LinkedList<MidCode> getMidCode(Value defVal) {
        return val2MidCode.getOrDefault(defVal, new LinkedList<>());
    }

    public void generateReachInOut() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlockList) {
                changed |= basicBlock.generateReachInOut();
            }
        } while (changed);
    }

    public boolean propagate() {
        boolean changed = false;
        for (BasicBlock basicBlock : basicBlockList) {
            changed |= basicBlock.propagate();
        }
        return changed;
    }

    public void generateLiveInOut() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlockList) {
                changed |= basicBlock.generateLiveInOut();
            }
        } while (changed);
    }

    public boolean removeDeadCode() {
        boolean changed = false;
        for (BasicBlock basicBlock : basicBlockList) {
            changed |= basicBlock.removeDeadCode();
        }
        return changed;
    }

    public boolean isBegin(BasicBlock basicBlock) {
        return basicBlockList.getFirst() == basicBlock;
    }

    public void generateLiveOutOfCode() {
        basicBlockList.forEach(BasicBlock::generateLiveOutOfCode);
    }

    public void divideLoopBlock() {
        HashSet<Label> loopMark = MidCodeTable.getInstance().getLoopMark();
        for (int i = 0; i < basicBlockList.size(); i++) {
            BasicBlock basicBlock = basicBlockList.get(i);
            LinkedList<Label> labelList = LabelTable.getInstance().getLabelList(basicBlock.getHead());
            for (Label label : labelList) {
                if (loopMark.contains(label)) {
                    LinkedList<BasicBlock> loop = new LinkedList<>();
                    loop.add(basicBlock);
                    int j = basicBlockList.size() - 1;
                    for (; j >= i; j--) {
                        BasicBlock curBasicBlock = basicBlockList.get(j);
                        if (curBasicBlock.getTail() instanceof Branch) {
                            Branch branch = (Branch) curBasicBlock.getTail();
                            if (branch.getBranchLabel().getMidCode() == basicBlock.getHead()) {
                                break;
                            }
                        } else if (curBasicBlock.getTail() instanceof Jump) {
                            Jump jump = (Jump) curBasicBlock.getTail();
                            if (jump.getLabel().getMidCode() == basicBlock.getHead()) {
                                break;
                            }
                        }
                    }
                    for (int k = i + 1; k <= j; k++) {
                        loop.add(basicBlockList.get(k));
                    }
                    loopBlockList.add(new LoopBlock(loop));
                }
            }
        }
    }

    public boolean extractLoopInvariant() {
        for (LoopBlock loopBlock : loopBlockList) {
            if (loopBlock.extractLoopInvariant()) {
                return true;
            }
        }
        return false;
    }

    public void allocateRegister() {
        for (BasicBlock basicBlock : basicBlockList) {
            HashSet<Value> liveOutSet = new HashSet<>(basicBlock.getLiveOutSet());
            MidCode midCode = basicBlock.getTail();
            while (midCode != basicBlock.getHead().getPrev()) {
                if (midCode instanceof DefVal) {
                    Value defVal = ((DefVal) midCode).getDefVal();
                    if (!defVal.isGlobal() && !defVal.isReturn()) {
                        boolean changed = false;
                        if (midCode instanceof UseVal) {
                            for (Value value : liveOutSet) {
                                if (!value.equals(defVal) && !value.isGlobal() && !value.isReturn()) {
                                    conflictGraph.addEdge(value, defVal);
                                    changed = true;
                                }
                            }
                        }
                        if (!changed) {
                            conflictGraph.addNode(defVal);
                        }
                        liveOutSet.remove(defVal);
                    }
                }
                if (midCode instanceof UseVal) {
                    for (Value value : ((UseVal) midCode).getUseVal()) {
                        if (!value.isGlobal() && !(value instanceof Imm) && !value.isReturn()) {
                            liveOutSet.add(value);
                        }
                    }
                }
                midCode = midCode.getPrev();
            }
        }
        generateDepth();
        HashSet<Value> localValue = new HashSet<>(conflictGraph.getBlacklist());
        LinkedList<Value> valueList = new LinkedList<>();
        HashMap<Value, HashSet<Value>> val2Conflict = new HashMap<>();
        LinkedList<Reg> freeRegs = new LinkedList<>(Reg.globalRegs);
        HashSet<Reg> usedRegs = new HashSet<>();
        while (!conflictGraph.isEmpty()) {
            Value value = conflictGraph.findNode(freeRegs.size());
            if (value != null) {
                HashSet<Value> conflictSet = conflictGraph.removeNode(value);
                valueList.add(value);
                val2Conflict.put(value, conflictSet);
            } else {
                Value localVal = conflictGraph.discardNode();
                HashSet<Value> conflictSet = conflictGraph.removeNode(localVal);
                localValue.add(localVal);
                valueList.add(localVal);
                val2Conflict.put(localVal, conflictSet);
            }
        }
        while (!valueList.isEmpty()) {
            Value value = valueList.removeLast();
            conflictGraph.store(value, val2Conflict.get(value));
            if (!localValue.contains(value)) {
                HashSet<Value> conflictSet = val2Conflict.get(value);
                HashSet<Reg> conflictReg = new HashSet<>();
                for (Value conflictVal : conflictSet) {
                    if (val2Reg.containsKey(conflictVal)) {
                        conflictReg.add(val2Reg.get(conflictVal));
                    }
                }
                for (Reg reg : freeRegs) {
                    if (!conflictReg.contains(reg)) {
                        val2Reg.put(value, reg);
                        usedRegs.add(reg);
                        break;
                    }
                }
            }
        }
        RegScheduler.getInstance().reClassify(usedRegs);
    }

    public void generateDepth() {
        int depth = 0;
        HashMap<BasicBlock, Integer> basicBlock2Depth = new HashMap<>();
        HashMap<BasicBlock, Integer> loopBegin = new HashMap<>();
        HashMap<BasicBlock, Integer> loopEnd = new HashMap<>();
        for (LoopBlock loopBlock : loopBlockList) {
            loopBegin.merge(loopBlock.getLoopBegin(), 1, Integer::sum);
            loopEnd.merge(loopBlock.getLoopEnd(), 1, Integer::sum);
        }
        for (BasicBlock basicBlock : basicBlockList) {
            if (loopBegin.containsKey(basicBlock)) {
                depth += loopBegin.get(basicBlock);
            }
            basicBlock2Depth.put(basicBlock, depth);
            if (loopEnd.containsKey(basicBlock)) {
                depth -= loopEnd.get(basicBlock);
            }
        }
        for (BasicBlock basicBlock : basicBlockList) {
            depth = basicBlock2Depth.get(basicBlock);
            MidCode midCode = basicBlock.getHead();
            while (midCode != basicBlock.getTail().getNext()) {
                if (midCode instanceof DefVal) {
                    Value defVal = ((DefVal) midCode).getDefVal();
                    val2Depth.merge(defVal, depth, Integer::max);
                }
                if (midCode instanceof UseVal) {
                    LinkedList<Value> useVal = ((UseVal) midCode).getUseVal();
                    for (Value value : useVal) {
                        if (!(value instanceof Imm)) {
                            val2Depth.merge(value, depth, Integer::max);
                        }
                    }
                }
                midCode = midCode.getNext();
            }
        }
    }

    public int getDepth(Value value) {
        return val2Depth.get(value);
    }

    public Reg findReg(Value value) {
        return val2Reg.get(value);
    }

    public void generateUseTime() {
        basicBlockList.forEach(BasicBlock::generateUseTime);
    }
}
