package SyntaxTree;

import SymbolTable.SymbolTable;

public interface ExpNode extends StmtNode {
    enum Type {
        VOID, INT, BOOL, ARRAY, MATRIX
    }

    Type getType(SymbolTable symbolTable);
}
