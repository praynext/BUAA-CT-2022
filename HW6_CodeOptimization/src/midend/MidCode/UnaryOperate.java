package midend.MidCode;

import java.util.LinkedList;

public class UnaryOperate implements Operate {
    public enum UnaryOp {
        POS, NEG, NOT
    }

    private final UnaryOp unaryOp;
    private Value value;

    public UnaryOperate(UnaryOp unaryOp, Value value) {
        this.unaryOp = unaryOp;
        this.value = value;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
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
        return unaryOp.toString() + " " + value.toString();
    }
}
