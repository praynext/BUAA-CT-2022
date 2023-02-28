package midend.MidCode;

public class ArgPush implements MidCode {
    private final Value value;

    public ArgPush(Value value) {
        this.value = value;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PUSH " + value;
    }
}
