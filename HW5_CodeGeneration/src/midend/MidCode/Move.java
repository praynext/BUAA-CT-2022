package midend.MidCode;

public class Move implements MidCode {
    public final boolean isTemp;
    public final Value leftValue;
    public final Value rightValue;

    public Move(boolean isTemp, Value leftValue, Value rightValue) {
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

    public Value getRightValue() {
        return rightValue;
    }

    @Override
    public String toString() {
        return (isTemp ? "TEMP " : "SAVE ") + leftValue.toString() + " <- " + rightValue.toString();
    }
}
