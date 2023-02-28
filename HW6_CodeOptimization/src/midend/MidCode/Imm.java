package midend.MidCode;

import backend.ValueMeta;

public class Imm extends Value implements ValueMeta {
    private final int value;

    public Imm(int value) {
        super(String.valueOf(value));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
