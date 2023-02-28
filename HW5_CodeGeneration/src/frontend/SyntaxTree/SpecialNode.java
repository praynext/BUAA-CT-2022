package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;

public class SpecialNode extends DefNode {

    public SpecialNode(SymbolTable symbolTable, boolean isFinal, Token ident, LinkedList<ExpNode> dimensions, LinkedList<ExpNode> initValues) {
        super(symbolTable, isFinal, ident, dimensions, initValues);
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
            new Declare(isGlobal, isFinal, value, size, values);
        } else {
            Addr value = new Addr(ident.getStringValue() + "@" + symbolTable.getId());
            new Declare(isGlobal, isFinal, value, size, values);
        }
        new IntGet();
        Word value = new Word(ident.getStringValue() + "@" + getId());
        new Move(false, value, new Word("?"));
        return null;
    }
}
