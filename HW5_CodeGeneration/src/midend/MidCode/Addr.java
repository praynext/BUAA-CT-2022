package midend.MidCode;

public class Addr extends Value {
    private final String ident;

    public Addr(String ident) {
        this.ident = ident;
    }

    public Addr() {
        this.ident = String.valueOf(tempCount++);
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
