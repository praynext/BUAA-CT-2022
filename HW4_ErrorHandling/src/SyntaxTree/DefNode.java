package SyntaxTree;

import Lexer.Token;
import Lexer.TypeCode;

import java.util.LinkedList;

public class DefNode {
    private TypeCode declType;
    protected final Token ident;
    protected final LinkedList<ExpNode> dimensions = new LinkedList<>();
    private LinkedList<ExpNode> initValues = null;

    public DefNode(Token ident) {
        this.ident = ident;
    }

    public void setDeclType(TypeCode declType) {
        this.declType = declType;
    }

    public void addDimension(ExpNode expNode) {
        dimensions.add(expNode);
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }

    public void setInitValues(int initValue) {
    }

    public TypeCode getDeclType() {
        return declType;
    }

    public Token getIdent() {
        return ident;
    }

    public LinkedList<ExpNode> getDimensions() {
        return dimensions;
    }
}
