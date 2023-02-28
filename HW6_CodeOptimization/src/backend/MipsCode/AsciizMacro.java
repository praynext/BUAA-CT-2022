package backend.MipsCode;

public class AsciizMacro implements MipsCode {
    private final String label;
    private final String stringValue;

    public AsciizMacro(String label, String stringValue) {
        this.label = label;
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return label + ": .asciiz \"" + stringValue + "\"";
    }
}
