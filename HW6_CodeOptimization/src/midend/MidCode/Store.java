package midend.MidCode;

import java.util.LinkedList;

public class Store extends MidCode implements UseVal {
    private Addr leftValue;
    private Value rightValue;

    public Store(Addr leftValue, Value rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public Addr getLeftValue() {
        return leftValue;
    }

    public Value getRightValue() {
        return rightValue;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        LinkedList<Value> useVal = new LinkedList<>();
        useVal.add(leftValue);
        useVal.add(rightValue);
        return useVal;
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        if (rightValue == oldVal) {
            rightValue = newVal;
        } else if (leftValue == oldVal) {
            leftValue = (Addr) newVal;
        }
    }

    @Override
    public String toString() {
        return "*" + leftValue + " <- " + rightValue;
    }
}
