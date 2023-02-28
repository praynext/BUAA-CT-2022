package frontend.SyntaxTree;

import midend.MidCode.Nop;
import midend.MidCode.Value;

public class NopNode implements StmtNode {
    public NopNode() {
    }

    @Override
    public NopNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        new Nop();
        return null;
    }
}
