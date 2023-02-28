package midend.MidCode;

public class ParaGet implements MidCode {
    private final Value value;

    public ParaGet(Value value) {
        this.value = value;
        MidCodeTable.getInstance().addMidCode(this);
        MidCodeTable.getInstance().addVarInfo(value, 1);
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PARA " + value;
    }
}
