package midend.MidCode;

import java.util.LinkedList;

public class Declare extends MidCode implements DefVal, UseVal, Copy {
    private final boolean isGlobal;
    private final boolean isFinal;
    private final Value value;
    private final int size;
    private final LinkedList<Value> initValues;

    public Declare(boolean isGlobal, boolean isFinal, Value value, int size, LinkedList<Value> initValues) {
        this.isGlobal = isGlobal;
        this.isFinal = isFinal;
        this.value = value;
        this.size = size;
        this.initValues = initValues;
    }

    public Value getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }

    public LinkedList<Value> getInitValues() {
        return initValues;
    }

    @Override
    public Value getDefVal() {
        return value;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        return initValues;
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        for (int i = 0; i < initValues.size(); i++) {
            if (initValues.get(i) == oldVal) {
                initValues.set(i, newVal);
            }
        }
    }

    @Override
    public LinkedList<Value> getSrc() {
        return initValues;
    }

    @Override
    public Value getDst() {
        return value;
    }

    @Override
    public String toString() {
        return (isGlobal ? "GLOBAL" : "LOCAL") + " " + (isFinal ? "CONST" : "VAR") + " " + value + " " +
                initValues.stream().map(Value::toString).reduce((s1, s2) -> s1 + ", " + s2).orElse("");
    }
}
