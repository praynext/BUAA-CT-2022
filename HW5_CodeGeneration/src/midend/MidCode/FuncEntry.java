package midend.MidCode;

import midend.LabelTable.Label;

public class FuncEntry implements MidCode {
    private final Label entryLabel;

    public FuncEntry(Label entryLabel) {
        this.entryLabel = entryLabel;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public Label getEntryLabel() {
        return entryLabel;
    }

    @Override
    public String toString() {
        return entryLabel.toString();
    }
}
