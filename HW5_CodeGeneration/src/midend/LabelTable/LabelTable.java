package midend.LabelTable;

import midend.MidCode.MidCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LabelTable {
    private static final LabelTable LABEL_TABLE = new LabelTable();
    private static final HashMap<MidCode, LinkedList<Label>> code2label = new HashMap<>();

    public static LabelTable getInstance() {
        return LABEL_TABLE;
    }

    public void setMidCode(MidCode midCode, Label label) {
        if (!code2label.containsKey(midCode)) {
            code2label.put(midCode, new LinkedList<>());
        }
        code2label.get(midCode).add(label);
    }

    public LinkedList<Label> getLabelList(MidCode midCode) {
        return code2label.getOrDefault(midCode, new LinkedList<>());
    }

    public void removeUnusedLabels(HashSet<Label> usedLabels) {
        for (LinkedList<Label> labelList : code2label.values()) {
            labelList.removeIf(label -> !usedLabels.contains(label) && label.getLabelId() != 0);
        }
    }
}
