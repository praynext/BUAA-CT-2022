package midend.MidCode;

public class FuncCall extends MidCode {
    public final String ident;

    public FuncCall(String ident) {
        this.ident = ident;
    }

    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return "CALL " + ident;
    }
}
