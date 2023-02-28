package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import SymbolTable.SymbolTable;

import static Lexer.TypeCode.CONSTTK;

public class GetIntNode implements StmtNode {
    private final LValNode lValNode;

    public GetIntNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void check(SymbolTable symbolTable) {
        DefNode variable;
        if ((variable = symbolTable.getVariable(lValNode.getIdent().getStringValue())) != null) {
            if (variable.getDeclType() == CONSTTK) {
                ErrorHandler.getInstance().addError(new Error("h", lValNode.getIdent().getLine()));
            }
        }
    }
}
