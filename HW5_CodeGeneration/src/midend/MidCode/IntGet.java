package midend.MidCode;

public class IntGet implements MidCode {
    public IntGet() {
        MidCodeTable.getInstance().addMidCode(this);
    }

    @Override
    public String toString() {
        return "CALL GETINT";
    }
}
