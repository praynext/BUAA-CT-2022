package backend.MipsCode;

public class JTypeInstr implements MipsCode {
    public static class j extends JTypeInstr {
        private final String label;

        public j(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "j " + label;
        }
    }

    public static class jal extends JTypeInstr {
        private final String label;

        public jal(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "jal " + label;
        }
    }
}
