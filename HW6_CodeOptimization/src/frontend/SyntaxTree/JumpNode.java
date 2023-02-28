package frontend.SyntaxTree;

import midend.LabelTable.Label;
import midend.MidCode.Jump;
import midend.MidCode.Value;

public class JumpNode extends StmtNode {
    private final Label label;

    public JumpNode(Label label) {
        this.label = label;
    }

    @Override
    public StmtNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        midCodeTable.addMidCode(new Jump(label));
        return null;
    }
}
