package midend.MidCode;

public class IntGet extends MidCode implements DefVal {
    public IntGet() {
    }

    @Override
    public Value getDefVal() {
        return new Word("?");
    }

    @Override
    public String toString() {
        return "CALL GETINT";
    }
}
