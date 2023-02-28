package frontend.SyntaxTree;

import midend.MidCode.Nop;
import midend.MidCode.Value;

public class NopNode extends StmtNode {
    public NopNode() {
    }

    @Override
    public NopNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        midCodeTable.addMidCode(new Nop());
        return null;
    }
}
