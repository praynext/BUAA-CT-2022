package midend.MidCode;

import backend.ValueMeta;

public class Imm extends Value implements ValueMeta {
    private final int value;

    public Imm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getIdent() {
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
