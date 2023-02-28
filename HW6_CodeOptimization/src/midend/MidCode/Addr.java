package midend.MidCode;

public class Addr extends Value {
    public Addr(String ident) {
        super(ident);
    }

    public Addr() {
        super();
    }

    public boolean isTemp() {
        return Character.isDigit(ident.charAt(0));
    }

    @Override
    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return "&" + ident;
    }
}
