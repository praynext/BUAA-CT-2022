package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class DefNode extends SyntaxNode {
    protected final SymbolTable symbolTable;
    private final boolean isFinal;
    protected final Token ident;
    private LinkedList<ExpNode> dimensions;
    private LinkedList<ExpNode> initValues;

    public DefNode(SymbolTable symbolTable, boolean isFinal, Token ident,
                   LinkedList<ExpNode> dimensions, LinkedList<ExpNode> initValues) {
        this.symbolTable = symbolTable;
        this.isFinal = isFinal;
        this.ident = ident;
        this.dimensions = dimensions;
        this.initValues = initValues;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Token getIdent() {
        return ident;
    }

    public LinkedList<ExpNode> getDimensions() {
        return dimensions;
    }

    public NumberNode getValue(LinkedList<ExpNode> dimensions) {
        simplify();
        if (initValues.size() == 0) {
            return new NumberNode(0);
        }
        if (dimensions.size() == 0) {
            return (NumberNode) initValues.get(0);
        } else if (dimensions.size() == 1) {
            return (NumberNode) initValues.get(((NumberNode) dimensions.get(0)).getValue());
        } else {
            return (NumberNode) initValues.get(((NumberNode) dimensions.get(0)).getValue() *
                    ((NumberNode) this.dimensions.get(1)).getValue() + ((NumberNode) dimensions.get(1)).getValue());
        }
    }

    public int getId() {
        return symbolTable.getId();
    }

    @Override
    public DefNode simplify() {
        dimensions = dimensions.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        initValues = initValues.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public Value generateMidCode() {
        boolean isGlobal = symbolTable.getParent() == null;
        LinkedList<Value> values = new LinkedList<>();
        initValues.forEach(initValue -> values.add(initValue.generateMidCode()));
        int size = dimensions.size() == 0 ? 1 : dimensions.size() == 1 ?
                ((NumberNode) dimensions.get(0)).getValue() :
                ((NumberNode) dimensions.get(0)).getValue() *
                        ((NumberNode) dimensions.get(1)).getValue();
        if (dimensions.size() == 0) {
            Word value = new Word(ident.getStringValue() + "@" + symbolTable.getId());
            midCodeTable.addMidCode(new Declare(isGlobal, isFinal, value, size, values));
            midCodeTable.addVarInfo(value, size);
        } else {
            Addr value = new Addr(ident.getStringValue() + "@" + symbolTable.getId());
            midCodeTable.addMidCode(new Declare(isGlobal, isFinal, value, size, values));
            midCodeTable.addVarInfo(value, size);
        }
        return null;
    }
}
