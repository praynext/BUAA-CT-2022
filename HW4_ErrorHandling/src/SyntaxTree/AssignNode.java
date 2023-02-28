package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import SymbolTable.SymbolTable;

import static Lexer.TypeCode.CONSTTK;

public class AssignNode implements StmtNode {
    private LValNode lValNode;
    private ExpNode expNode;

    public AssignNode() {
    }

    public void setLValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
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
