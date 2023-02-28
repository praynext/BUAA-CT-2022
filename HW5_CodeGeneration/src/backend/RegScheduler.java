package backend;

import backend.MipsCode.AbsoluteAddress;
import backend.MipsCode.Address;
import backend.MipsCode.ITypeInstr;
import backend.MipsCode.RelativeAddress;
import midend.MidCode.Addr;
import midend.MidCode.Imm;
import midend.MidCode.Value;
import midend.MidCode.Word;

import java.util.*;

public class RegScheduler {
    static List<Reg> regs = Arrays.asList(
            Reg.T0, Reg.T1, Reg.T2, Reg.T3, Reg.T4,
            Reg.T5, Reg.T6, Reg.T7, Reg.T8, Reg.T9,
            Reg.S0, Reg.S1, Reg.S2, Reg.S3, Reg.S4,
            Reg.S5, Reg.S6, Reg.S7, Reg.S8, Reg.S9,
            Reg.S10, Reg.S11, Reg.S12, Reg.S13, Reg.S14
    );
    private final HashMap<Reg, Value> reg2value = new HashMap<>();
    private final LinkedList<Reg> busyRegs = new LinkedList<>();
    private final LinkedList<Reg> freeRegs = new LinkedList<>();

    public RegScheduler() {
        freeRegs.addAll(regs);
    }

    public HashMap<Reg, Value> getReg2value() {
        return reg2value;
    }

    public void clear() {
        reg2value.clear();
        busyRegs.clear();
        freeRegs.clear();
        freeRegs.addAll(regs);
    }

    public Reg find(Value value) {
        for (Reg reg : busyRegs) {
            if (reg2value.get(reg).equals(value)) {
                return reg;
            }
        }
        return null;
    }

    public Reg alloc(Value value) {
        if (!freeRegs.isEmpty()) {
            Reg reg = freeRegs.getFirst();
            freeRegs.removeFirst();
            busyRegs.addLast(reg);
            reg2value.put(reg, value);
            return reg;
        }
        return null;
    }

    public Reg preempt(Value value) {
        for (Reg reg : busyRegs) {
            if (!Translator.getInstance().getSynchronizedReg().contains(reg)) {
                Value oldValue = reg2value.get(reg);
                if (oldValue instanceof Word || (value instanceof Addr && ((Addr) oldValue).isTemp())) {
                    Translator.getInstance().getMipsCodeList().add(
                            new ITypeInstr.sw(reg, Translator.getInstance().getValue2address().get(oldValue)));
                }
                reg2value.put(reg, value);
                busyRegs.remove(reg);
                busyRegs.addLast(reg);
                return reg;
            }
        }
        return null;
    }

    public void flush() {
        for (Map.Entry<Reg, Value> entry : reg2value.entrySet()) {
            Address address = Translator.getInstance().getValue2address().get(entry.getValue());
            if (address instanceof AbsoluteAddress && entry.getValue() instanceof Word ||
                    address instanceof RelativeAddress && !(entry.getValue() instanceof Addr)) {
                Translator.getInstance().getMipsCodeList().add(new ITypeInstr.sw(entry.getKey(), address));
            }
        }
        clear();
    }
}
