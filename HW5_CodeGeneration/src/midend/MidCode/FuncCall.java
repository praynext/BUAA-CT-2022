package midend.MidCode;

public class FuncCall implements MidCode {
    public final String ident;

    public FuncCall(String ident) {
        this.ident = ident;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return "CALL " + ident;
    }
}
