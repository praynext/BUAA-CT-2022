package backend.MipsCode;

import backend.Reg;
import midend.MidCode.Imm;

public class RTypeInstr implements MipsCode {
    public enum ROpCode {
        addu, subu, seq, sne, sge, sgt, sle, slt,
        sll, and
    }

    public static class RR3 extends RTypeInstr {
        public final ROpCode opCode;
        private final Reg rd;
        private final Reg rs;
        private final Reg rt;

        public RR3(ROpCode opCode, Reg rd, Reg rs, Reg rt) {
            this.opCode = opCode;
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
        }

        @Override
        public String toString() {
            return opCode.toString() + " " + rd + ", " + rs + ", " + rt;
        }
    }

    public static class RR2I1 extends RTypeInstr {
        public final ROpCode opCode;
        private final Reg rd;
        private final Reg rt;
        private final Imm shamt;

        public RR2I1(ROpCode opCode, Reg rd, Reg rt, Imm shamt) {
            this.opCode = opCode;
            this.rd = rd;
            this.rt = rt;
            this.shamt = shamt;
        }

        @Override
        public String toString() {
            return opCode.toString() + " " + rd + ", " + rt + ", " + shamt;
        }
    }

    public static class jr extends RTypeInstr {
        private final Reg rs;

        public jr(Reg rs) {
            this.rs = rs;
        }

        @Override
        public String toString() {
            return "jr " + rs;
        }
    }
}
