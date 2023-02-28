package backend.MipsCode;

import backend.Reg;

public class RelativeAddress implements Address {
    private final Reg base;
    private final int offset;

    public RelativeAddress(Reg base, int offset) {
        this.base = base;
        this.offset = offset;
    }

    public Reg getBase() {
        return base;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return offset + "(" + base + ")";
    }
}
