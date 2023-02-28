package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import static SyntaxTree.ExpNode.Type.*;

public class LValNode implements ExpNode {
    private Token ident;
    private final LinkedList<ExpNode> dimensions = new LinkedList<>();

    public LValNode() {
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void addDimension(ExpNode expNode) {
        dimensions.add(expNode);
    }

    public Token getIdent() {
        return ident;
    }

    public void check(SymbolTable symbolTable) {
        if (symbolTable.getVariable(ident.getStringValue()) == null) {
            ErrorHandler.getInstance().addError(new Error("c", ident.getLine()));
        }
    }

    @Override
    public Type getType(SymbolTable symbolTable) {
        int dimension = symbolTable.getVariable(ident.getStringValue()).getDimensions().size();
        if (dimensions.size() == 0) {
            if (dimension == 0) {
                return INT;
            } else if (dimension == 1) {
                return ARRAY;
            } else {
                return MATRIX;
            }
        } else if (dimensions.size() == 1) {
            if (dimension == 1) {
                return INT;
            } else {
                return ARRAY;
            }
        } else {
            return INT;
        }
    }
}
