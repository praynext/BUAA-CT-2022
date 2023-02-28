package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class DeclNode implements BlockItemNode {
    private final SymbolTable symbolTable;
    private final boolean isFinal;
    private LinkedList<DefNode> defNodes;

    public DeclNode(SymbolTable symbolTable, boolean isFinal, LinkedList<DefNode> defNodes) {
        this.symbolTable = symbolTable;
        this.isFinal = isFinal;
        this.defNodes = defNodes;
    }

    @Override
    public DeclNode simplify() {
        defNodes = defNodes.stream().map(DefNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public Value generateMidCode() {
        defNodes.forEach(DefNode::generateMidCode);
        return null;
    }
}
