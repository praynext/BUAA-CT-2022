package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.Jump;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value;

public class ContinueNode extends StmtNode {
    private final SymbolTable symbolTable;

    public ContinueNode(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public ContinueNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        midCodeTable.addMidCode(new Jump(MidCodeTable.getInstance().getLoopBegin()));
        return null;
    }
}
