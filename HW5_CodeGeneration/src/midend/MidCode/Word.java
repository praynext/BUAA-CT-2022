package midend.MidCode;

public class Word extends Value {
    private final String ident;

    public Word(String ident) {
        this.ident = ident;
    }

    public Word() {
        this.ident = String.valueOf(tempCount++);
    }

    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return "$" + ident;
    }
}
