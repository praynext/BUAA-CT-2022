package backend;

import midend.MidCode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LoopBlock {
    private final LinkedList<BasicBlock> blockList;
    private final BasicBlock entry = new BasicBlock(null, null, null);
    private final HashMap<BasicBlock, HashSet<BasicBlock>> outMap = new HashMap<>();
    private final HashSet<BasicBlock> exitNode = new HashSet<>();
    private final HashSet<BasicBlock> dominator = new HashSet<>();
    private final HashMap<Value, HashSet<MidCode>> defMap = new HashMap<>();
    private final LinkedList<MidCode> invariant = new LinkedList<>();

    public LoopBlock(LinkedList<BasicBlock> blockList) {
        this.blockList = blockList;
        for (BasicBlock basicBlock : blockList) {
            HashSet<BasicBlock> next = basicBlock.getNext();
            next.forEach(item -> {
                if (!blockList.contains(item)) {
                    exitNode.add(basicBlock);
                }
            });
        }
    }

    public BasicBlock getLoopBegin() {
        return blockList.getFirst();
    }

    public BasicBlock getLoopEnd() {
        return blockList.getLast();
    }

    public boolean extractLoopInvariant() {
        blockList.forEach(basicBlock -> outMap.put(basicBlock, new HashSet<>()));
        generateNodeInOut();
        exitNode.forEach(basicBlock -> dominator.addAll(outMap.get(basicBlock)));
        exitNode.forEach(basicBlock -> dominator.removeIf(item -> !outMap.get(basicBlock).contains(item)));
        generateDefMap();
        generateInvariant();
        if (!invariant.isEmpty()) {
            Nop nop = new Nop();
            MidCode prev = blockList.get(0).getHead().getPrev();
            prev.link(nop).link(blockList.get(0).getHead());
            for (BasicBlock basicBlock : blockList) {
                for (MidCode midCode = basicBlock.getHead(); midCode != basicBlock.getTail().getNext(); midCode = midCode.getNext()) {
                    if (invariant.contains(midCode)) {
                        midCode.delete();
                        prev = prev.link(midCode);
                    }
                }
            }
            prev.link(nop);
        }
        return !invariant.isEmpty();
    }

    private HashSet<BasicBlock> getOutNode(BasicBlock basicBlock) {
        if (outMap.containsKey(basicBlock)) {
            return outMap.get(basicBlock);
        } else {
            HashSet<BasicBlock> outNode = new HashSet<>();
            outNode.add(entry);
            return outNode;
        }
    }

    private void generateNodeInOut() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : blockList) {
                HashSet<BasicBlock> oldOut = new HashSet<>(outMap.get(basicBlock));
                HashSet<BasicBlock> out = outMap.get(basicBlock);
                HashSet<BasicBlock> in = new HashSet<>();
                HashSet<BasicBlock> prev = basicBlock.getPrev();
                if (prev.size() != 0) {
                    prev.forEach(item -> in.addAll(getOutNode(item)));
                    prev.forEach(item -> in.removeIf(block -> !getOutNode(item).contains(block)));
                }
                out.addAll(in);
                out.add(basicBlock);
                changed |= !oldOut.equals(out);
            }
        } while (changed);
    }

    public void generateDefMap() {
        for (BasicBlock basicBlock : blockList) {
            MidCode midCode = basicBlock.getHead();
            while (midCode != basicBlock.getTail().getNext()) {
                if (midCode instanceof DefVal) {
                    Value value = ((DefVal) midCode).getDefVal();
                    if (!defMap.containsKey(value)) {
                        defMap.put(value, new HashSet<>());
                    }
                    defMap.get(value).add(midCode);
                }
                midCode = midCode.getNext();
            }
        }
    }

    public void generateInvariant() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : dominator) {
                MidCode midCode = basicBlock.getHead();
                while (midCode != basicBlock.getTail().getNext()) {
                    if (midCode instanceof Assign || midCode instanceof Declare ||
                            midCode instanceof Move || midCode instanceof Store) {
                        if (!invariant.contains(midCode) && isInvariant(midCode)) {
                            invariant.add(midCode);
                            changed = true;
                        }
                    }
                    midCode = midCode.getNext();
                }
            }
        } while (changed);
    }

    public boolean isInvariant(MidCode midCode) {
        if (midCode instanceof DefVal) {
            if (defMap.getOrDefault(((DefVal) midCode).getDefVal(), new HashSet<>()).size() > 1) {
                return false;
            }
            for (BasicBlock basicBlock : blockList) {
                HashSet<MidCode> reachIn = basicBlock.getReachInSet();
                for (MidCode code : reachIn) {
                    if (code != midCode && code instanceof DefVal &&
                            ((DefVal) code).getDefVal().equals(((DefVal) midCode).getDefVal()) &&
                            basicBlock.getUseValSet().contains(((DefVal) midCode).getDefVal())) {
                        return false;
                    }
                }
            }
        }
        LinkedList<Value> usedValue = ((UseVal) midCode).getUseVal();
        for (Value value : usedValue) {
            if (value.isGlobal() || value.isReturn()) {
                return false;
            } else if (!(value instanceof Imm)) {
                HashSet<MidCode> totDef = new HashSet<>(defMap.getOrDefault(value, new HashSet<>()));
                if (totDef.size() > 1) {
                    return false;
                } else {
                    totDef.removeIf(invariant::contains);
                    if (!totDef.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
