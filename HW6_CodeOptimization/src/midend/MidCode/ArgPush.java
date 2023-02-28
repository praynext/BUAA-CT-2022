package midend.MidCode;

import java.util.LinkedList;

public class ArgPush extends MidCode implements UseVal {
    private Value value;

    public ArgPush(Value value) {
        this.value = value;
    }

    public Value getValue() {
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
        return "PUSH " + value;
    }
}
