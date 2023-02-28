package backend.MipsCode;

public class AbsoluteAddress implements Address {
    private final String label;

    public AbsoluteAddress(String label) {
        this.label = label.substring(0, label.indexOf('@') >= 0 ? label.indexOf('@') : label.length());
    }

    @Override
    public String toString() {
        return label;
    }
}
