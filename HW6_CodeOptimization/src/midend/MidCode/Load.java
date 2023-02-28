package midend.MidCode;

import java.util.LinkedList;

public class Load extends MidCode implements DefVal, UseVal {
    private final boolean isTemp;
    private final Value leftValue;
    private Addr rightValue;

    public Load(boolean isTemp, Value leftValue, Addr rightValue) {
        this.isTemp = isTemp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public boolean isTemp() {
        return isTemp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Addr getRightValue() {
        return rightValue;
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
        if (rightValue.equals(oldVal)) {
            rightValue = (Addr) newVal;
        }
    }

    @Override
    public String toString() {
        return (isTemp ? "TEMP " : "SAVE ") + leftValue + " <- *" + rightValue;
    }
}
