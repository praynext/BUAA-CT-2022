package midend.MidCode;

public class Return implements MidCode {
    private Value value = null;

    public Return() {
        MidCodeTable.getInstance().addMidCode(this);
    }

    public Return(Value value) {
        this.value = value;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? "RETURN" : "RETURN " + value;
    }
}
