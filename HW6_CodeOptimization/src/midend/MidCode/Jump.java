package midend.MidCode;

import midend.LabelTable.Label;

public class Jump extends MidCode {
    private Label label;

    public Jump(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "JUMP " + label.getLabelName();
    }
}
