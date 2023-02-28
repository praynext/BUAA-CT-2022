package frontend.SyntaxTree;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value;

public abstract class SyntaxNode {
    MidCodeTable midCodeTable = MidCodeTable.getInstance();

    abstract public SyntaxNode simplify();

    abstract public Value generateMidCode();
}
