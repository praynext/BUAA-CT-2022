package midend.MidCode;

public class Word extends Value {
    public Word(String ident) {
        super(ident);
    }

    public Word() {
        super();
    }

    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return "$" + ident;
    }
}
