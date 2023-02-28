package midend.MidCode;

import java.util.LinkedList;

public class Return extends MidCode implements UseVal {
    private Value value = null;

    public Return() {
    }

    public Return(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        LinkedList<Value> useVal = new LinkedList<>();
        if (value != null) {
            useVal.add(value);
        }
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
        return value == null ? "RETURN" : "RETURN " + value;
    }
}
