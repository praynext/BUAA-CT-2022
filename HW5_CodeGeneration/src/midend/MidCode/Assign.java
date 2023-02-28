package midend.MidCode;

import static midend.MidCode.UnaryOperate.UnaryOp.POS;

public class Assign implements MidCode {
    private final boolean isTemp;
    private final Value leftValue;
    private final Operate rightValue;

    public Assign(boolean isTemp, Value leftValue, Operate rightValue) {
        this.isTemp = isTemp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        MidCodeTable.getInstance().addMidCode(this);
        if (isTemp) {
            MidCodeTable.getInstance().addVarInfo(leftValue, 1);
        }
    }

    public boolean isTemp() {
        return isTemp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Operate getRightValue() {
        return rightValue;
    }

    public void simplify() {
    }

    @Override
    public String toString() {
        return leftValue + " <- " + rightValue;
    }
}
