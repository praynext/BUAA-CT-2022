package midend.MidCode;

import midend.LabelTable.Label;

public class FuncEntry extends MidCode {
    private final Label entryLabel;

    public FuncEntry(Label entryLabel) {
        this.entryLabel = entryLabel;
    }

    public Label getEntryLabel() {
        return entryLabel;
    }

    @Override
    public String toString() {
        return "";
    }
}
