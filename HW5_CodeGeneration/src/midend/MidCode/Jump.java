package midend.MidCode;

import midend.LabelTable.Label;

public class Jump implements MidCode {
    private Label label;

    public Jump(Label label) {
        this.label = label;
        MidCodeTable.getInstance().addMidCode(this);
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
