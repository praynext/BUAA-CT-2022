package backend;

import java.util.Arrays;
import java.util.List;

public class Reg implements ValueMeta {
    public static final Reg ZERO = new Reg("0");
    public static final Reg RV = new Reg("v0");
    public static final Reg RA = new Reg("ra");
    public static final Reg SP = new Reg("sp");
    public static final Reg AR = new Reg("a0");
    public static final Reg T0 = new Reg("t0");
    public static final Reg T1 = new Reg("t1");
    public static final Reg T2 = new Reg("t2");
    public static final Reg T3 = new Reg("t3");
    public static final Reg T4 = new Reg("t4");
    public static final Reg T5 = new Reg("t5");
    public static final Reg T6 = new Reg("t6");
    public static final Reg T7 = new Reg("t7");
    public static final Reg T8 = new Reg("t8");
    public static final Reg T9 = new Reg("t9");
    public static final Reg S0 = new Reg("s0");
    public static final Reg S1 = new Reg("s1");
    public static final Reg S2 = new Reg("s2");
    public static final Reg S3 = new Reg("s3");
    public static final Reg S4 = new Reg("s4");
    public static final Reg S5 = new Reg("s5");
    public static final Reg S6 = new Reg("s6");
    public static final Reg S7 = new Reg("s7");
    public static final Reg S8 = new Reg("gp");
    public static final Reg S9 = new Reg("k0");
    public static final Reg S10 = new Reg("k1");
    public static final Reg S11 = new Reg("a1");
    public static final Reg S12 = new Reg("a2");
    public static final Reg S13 = new Reg("a3");
    public static final Reg S14 = new Reg("v1");
    public static final Reg TR = new Reg("fp");
    public static final List<Reg> globalRegs = Arrays.asList(
            S0, S1, S2, S3, S4, S5, S6
    );
    public final String name;

    private Reg(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}
