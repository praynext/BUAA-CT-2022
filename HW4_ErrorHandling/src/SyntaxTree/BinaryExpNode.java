package SyntaxTree;

import Lexer.Token;
import SymbolTable.SymbolTable;

import static Lexer.TypeCode.*;
import static SyntaxTree.ExpNode.Type.BOOL;
import static SyntaxTree.ExpNode.Type.INT;

public class BinaryExpNode implements ExpNode {
    private Token binaryOp;
    private ExpNode leftExp;
    private ExpNode rightExp;

    public BinaryExpNode() {
    }

    public void setBinaryOp(Token binaryOp) {
        this.binaryOp = binaryOp;
    }

    public void setLeftExp(ExpNode expNode) {
        leftExp = expNode;
    }

    public void setRightExp(ExpNode expNode) {
        rightExp = expNode;
    }

    @Override
    public Type getType(SymbolTable symbolTable) {
        if (binaryOp.isType(PLUS) || binaryOp.isType(MINU) ||
                binaryOp.isType(MULT) || binaryOp.isType(DIV) || binaryOp.isType(MOD)) {
            return INT;
        } else {
            return BOOL;
        }
    }
}
