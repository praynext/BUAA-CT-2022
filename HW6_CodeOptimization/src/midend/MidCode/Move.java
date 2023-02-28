package midend.MidCode;

import java.util.LinkedList;

public class Move extends MidCode implements DefVal, UseVal, Copy {
    public final boolean isTemp;
    public final Value leftValue;
    public Value rightValue;

    public Move(boolean isTemp, Value leftValue, Value rightValue) {
        this.isTemp = isTemp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Value getRightValue() {
        return rightValue;
    }

    public void simplify() {
        if (leftValue.equals(rightValue)) {
            this.delete();
        }
    }

    @Override
    public Value getDefVal() {
        return leftValue;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        LinkedList<Value> useVal = new LinkedList<>();
        useVal.add(rightValue);
        return useVal;
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        if (rightValue == oldVal) {
            rightValue = newVal;
        }
    }

    @Override
    public LinkedList<Value> getSrc() {
        LinkedList<Value> src = new LinkedList<>();
        src.add(rightValue);
        return src;
    }

    @Override
    public Value getDst() {
        return leftValue;
    }

    @Override
    public String toString() {
        return (isTemp ? "TEMP " : "SAVE ") + leftValue.toString() + " <- " + rightValue.toString();
    }
}
