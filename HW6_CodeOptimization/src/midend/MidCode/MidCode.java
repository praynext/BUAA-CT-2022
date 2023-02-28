package midend.MidCode;

import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;

import java.util.LinkedList;

public class MidCode {
    protected MidCode prev;
    protected MidCode next;

    public MidCode link(MidCode next) {
        this.next = next;
        if (next != null) {
            next.prev = this;
        }
        return next;
    }

    public void delete() {
        prev.next = next;
        if (next != null) {
            next.prev = prev;
        }
        LinkedList<Label> labels = LabelTable.getInstance().getLabelList(this);
        labels.forEach(label -> label.setMidCode(next));
        LabelTable.getInstance().removeAllLabel(this);
    }

    public void replaceBy(MidCode... midCodes) {
        MidCode tail = prev;
        for (MidCode midCode : midCodes) {
            tail = tail.link(midCode);
        }
        if (next != null) {
            tail.link(next);
        }
        LinkedList<Label> labels = LabelTable.getInstance().getLabelList(this);
        labels.forEach(label -> label.setMidCode(prev.next));
    }

    public MidCode getPrev() {
        return prev;
    }

    public MidCode getNext() {
        return next;
    }
}
