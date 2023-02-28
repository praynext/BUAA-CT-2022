package SyntaxTree;

import SymbolTable.SymbolTable;

import static SyntaxTree.ExpNode.Type.INT;

public class NumberNode implements ExpNode {
    private final int number;

    public NumberNode(int number) {
        this.number = number;
    }

    @Override
    public Type getType(SymbolTable symbolTable) {
        return INT;
    }
}
