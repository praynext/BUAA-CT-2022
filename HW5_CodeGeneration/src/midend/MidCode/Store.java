package midend.MidCode;

public class Store implements MidCode {
    private final Addr leftValue;
    private final Value rightValue;

    public Store(Addr leftValue, Value rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public Addr getLeftValue() {
        return leftValue;
    }

    public Value getRightValue() {
        return rightValue;
    }

    @Override
    public String toString() {
        return "*" + leftValue + " <- " + rightValue;
    }
}
