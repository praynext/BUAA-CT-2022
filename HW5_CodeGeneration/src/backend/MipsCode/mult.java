package backend.MipsCode;

import backend.Reg;

public class mult implements MipsCode {
    private final Reg rs;
    private final Reg rt;

    public mult(Reg rs, Reg rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return "mult " + rs + ", " + rt;
    }
}
