package midend.MidCode;

import java.util.LinkedList;

public class ParaGet extends MidCode implements DefVal, UseVal {
    private Value value;

    public ParaGet(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Value getDefVal() {
        return value;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        LinkedList<Value> useVal = new LinkedList<>();
        useVal.add(value);
        return useVal;
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        if (value == oldVal) {
            value = newVal;
        }
    }

    @Override
    public String toString() {
        return "PARA " + value;
    }
}
