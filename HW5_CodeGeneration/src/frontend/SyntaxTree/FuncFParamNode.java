package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;

import java.util.LinkedList;
import java.util.stream.Collectors;

import frontend.SyntaxTree.ExpNode.Type;
import midend.MidCode.*;

import static frontend.SyntaxTree.ExpNode.Type.*;

public class FuncFParamNode extends DefNode {
    private final Type type;

    public FuncFParamNode(SymbolTable symbolTable, Token ident, LinkedList<ExpNode> dimensions) {
        super(symbolTable, false, ident, dimensions, null);
        this.type = dimensions.size() == 2 ? MATRIX : dimensions.size() == 1 ? ARRAY : INT;
    }

    public boolean matchType(Type type) {
        return this.type == type;
    }

    @Override
    public FuncFParamNode simplify() {
        LinkedList<ExpNode> simDimensions = super.getDimensions().stream().
                map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return new FuncFParamNode(symbolTable, ident, simDimensions);
    }

    @Override
    public Value generateMidCode() {
        if (super.getDimensions().size() == 0) {
            new ParaGet(new Word(ident.getStringValue() + "@" + symbolTable.getId()));
        } else {
            new ParaGet(new Addr(ident.getStringValue() + "@" + symbolTable.getId()));
        }
        return null;
    }
}
