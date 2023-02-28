package midend.LabelTable;

import backend.MipsCode.MipsCode;
import midend.MidCode.Jump;
import midend.MidCode.MidCode;

public class Label implements MipsCode {
    private static int labelCount = 1;
    private int labelId = 0;
    private String labelName;
    private MidCode midCode;

    public Label() {
        this.labelId = labelCount++;
    }

    public Label(String labelName) {
        this.labelName = labelName;
    }

    public int getLabelId() {
        return labelId;
    }

    public String getLabelName() {
        if (labelName != null) {
            return labelName;
        } else {
            return "Label" + labelId;
        }
    }

    public MidCode getMidCode() {
        return midCode;
    }

    public void setMidCode(MidCode midCode) {
        this.midCode = midCode;
        LabelTable.getInstance().setMidCode(midCode, this);
    }

    public Label getTarget() {
        if (midCode instanceof Jump) {
            if (((Jump) midCode).getLabel() == this) {
                return this;
            }
            return ((Jump) midCode).getLabel().getTarget();
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        if (labelName != null) {
            return labelName + ":";
        } else {
            return "Label" + labelId + ":";
        }
    }
}
