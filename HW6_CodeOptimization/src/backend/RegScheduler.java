package backend;

import backend.MipsCode.AbsoluteAddress;
import backend.MipsCode.Address;
import backend.MipsCode.ITypeInstr;
import backend.MipsCode.RelativeAddress;
import midend.MidCode.Addr;
import midend.MidCode.Nop;
import midend.MidCode.Value;
import midend.MidCode.Word;

import java.util.*;

public class RegScheduler {
    private static final RegScheduler REG_SCHEDULER = new RegScheduler();
    private final List<Reg> totalRegs = Arrays.asList(
            Reg.S0, Reg.S1, Reg.S2, Reg.S3, Reg.S4,
            Reg.S5, Reg.S6, Reg.S7, Reg.S8, Reg.S9,
            Reg.S10, Reg.S11, Reg.S12, Reg.S13, Reg.S14,
            Reg.T0, Reg.T1, Reg.T2, Reg.T3, Reg.T4,
            Reg.T5, Reg.T6, Reg.T7, Reg.T8, Reg.T9
    );
    private final List<Reg> globalRegs = new ArrayList<>();
    private final List<Reg> localRegs = new ArrayList<>();
    private final HashMap<Reg, Value> reg2value = new HashMap<>();
    private final HashMap<Value, Reg> value2reg = new HashMap<>();
    private final LinkedList<Reg> busyRegs = new LinkedList<>();
    private final LinkedList<Reg> freeRegs = new LinkedList<>();
    private FuncBlock curFuncBlock;

    public static RegScheduler getInstance() {
        return REG_SCHEDULER;
    }

    public List<Reg> getGlobalRegs() {
        return globalRegs;
    }

    public void reClassify(HashSet<Reg> usedRegs) {
        globalRegs.addAll(usedRegs);
        localRegs.clear();
        for (Reg reg : totalRegs) {
            if (!globalRegs.contains(reg)) {
                localRegs.add(reg);
            }
        }
    }

    public void setCurFuncBlock(FuncBlock curFuncBlock) {
        this.curFuncBlock = curFuncBlock;
    }

    public HashMap<Reg, Value> getReg2value() {
        return reg2value;
    }

    public void clearAll() {
        reg2value.clear();
        value2reg.clear();
        busyRegs.clear();
        freeRegs.clear();
        freeRegs.addAll(localRegs);
    }

    public void clear() {
        localRegs.forEach(reg2value::remove);
        busyRegs.clear();
        freeRegs.clear();
        freeRegs.addAll(localRegs);
    }

    public Reg find(Value value) {
        for (Reg reg : reg2value.keySet()) {
            if (reg2value.get(reg).equals(value)) {
                if (busyRegs.contains(reg)) {
                    busyRegs.remove(reg);
                    busyRegs.addFirst(reg);
                }
                return reg;
            }
        }
        if (value2reg.containsKey(value)) {
            reg2value.put(value2reg.get(value), value);
        }
        return value2reg.get(value);
    }

    public Reg alloc(Value value) {
        if (curFuncBlock.findReg(value) != null) {
            reg2value.put(curFuncBlock.findReg(value), value);
            value2reg.put(value, curFuncBlock.findReg(value));
            return curFuncBlock.findReg(value);
        }
        if (!freeRegs.isEmpty()) {
            Reg reg = freeRegs.removeFirst();
            busyRegs.addLast(reg);
            reg2value.put(reg, value);
            return reg;
        }
        return null;
    }

    public Reg preempt(Value value) {
        Reg selectedReg = null;
        for (Reg reg : busyRegs) {
            Value oldValue = reg2value.get(reg);
            if (Translator.getInstance().getCurBasicBlock().usedUp(oldValue, new Nop())
                    && !Translator.getInstance().getSynchronizedReg().contains(reg)) {
                selectedReg = reg;
                break;
            }
        }
        if (selectedReg == null) {
            for (Reg reg : busyRegs) {
                Value oldValue = reg2value.get(reg);
                if (!Translator.getInstance().getCurBasicBlock().isLive(oldValue, Translator.getInstance().getCurMidCode())
                        && !Translator.getInstance().getSynchronizedReg().contains(reg)) {
                    selectedReg = reg;
                    break;
                }
            }
        }
        if (selectedReg == null) {
            for (Reg reg : busyRegs) {
                if (!Translator.getInstance().getSynchronizedReg().contains(reg)) {
                    selectedReg = reg;
                }
            }
        }
        if (selectedReg != null) {
            Value oldValue = reg2value.get(selectedReg);
            if ((oldValue instanceof Word || (value instanceof Addr && ((Addr) oldValue).isTemp())) &&
                    Translator.getInstance().getCurBasicBlock().isLive(oldValue, Translator.getInstance().getCurMidCode())) {
                Translator.getInstance().getMipsCodeList().add(
                        new ITypeInstr.sw(selectedReg, Translator.getInstance().getValue2address().get(oldValue)));
            }
            reg2value.put(selectedReg, value);
            busyRegs.remove(selectedReg);
            busyRegs.addLast(selectedReg);
            return selectedReg;
        }
        return null;
    }

    public void flush(boolean isReturn) {
        for (Reg reg : busyRegs) {
            Value value = reg2value.get(reg);
            Address address = Translator.getInstance().getValue2address().get(value);
            if (address instanceof AbsoluteAddress && value instanceof Word) {
                Translator.getInstance().getMipsCodeList().add(new ITypeInstr.sw(reg, address));
            } else if (!isReturn && address instanceof RelativeAddress && !(value instanceof Addr && !((Addr) value).isTemp()) &&
                    Translator.getInstance().getCurBasicBlock().getLiveOutSet().contains(value)) {
                Translator.getInstance().getMipsCodeList().add(new ITypeInstr.sw(reg, address));
            }
        }
    }

    public void unMap(Value useVal) {
        for (Reg reg : busyRegs) {
            if (reg2value.get(reg).equals(useVal)) {
                freeRegs.add(reg);
                busyRegs.remove(reg);
                reg2value.remove(reg);
                break;
            }
        }
    }
}
