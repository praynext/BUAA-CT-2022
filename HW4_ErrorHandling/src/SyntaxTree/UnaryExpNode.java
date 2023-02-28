package SyntaxTree;

import Lexer.Token;
import SymbolTable.SymbolTable;

public class UnaryExpNode implements ExpNode {
    private final Token unaryOp;
    private ExpNode expNode;

    public UnaryExpNode(Token unaryOp) {
        this.unaryOp = unaryOp;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    @Override
    public Type getType(SymbolTable symbolTable) {
        return expNode.getType(symbolTable);
    }
}
