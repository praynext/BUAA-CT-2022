package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.Jump;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value;

public class BreakNode implements StmtNode {
    private final SymbolTable symbolTable;

    public BreakNode(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public BreakNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        new Jump(MidCodeTable.getInstance().getLoopEnd());
        return null;
    }
}
