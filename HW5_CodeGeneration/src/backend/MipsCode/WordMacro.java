package backend.MipsCode;

import java.util.LinkedList;
import java.util.Objects;

public class WordMacro implements MipsCode {
    private final String label;
    private final int size;
    private final LinkedList<Integer> intValues;

    public WordMacro(String label, int size, LinkedList<Integer> intValues) {
        this.label = label.substring(0, label.indexOf('@') >= 0 ? label.indexOf('@') : label.length());
        this.size = size;
        this.intValues = intValues;
    }

    @Override
    public String toString() {
        if (intValues.isEmpty()) {
            return label + ": .word 0:" + size;
        }
        return label + ": .word " + intValues.stream().map(Objects::toString).
                reduce((sum, item) -> sum + ", " + item).orElse("");
    }
}
