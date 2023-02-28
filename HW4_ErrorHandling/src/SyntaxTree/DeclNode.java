package SyntaxTree;

import Lexer.Token;

import java.util.LinkedList;

public class DeclNode implements BlockItemNode {
    private final Token declType;
    private final LinkedList<DefNode> defNodes = new LinkedList<>();

    public DeclNode(Token declType) {
        this.declType = declType;
    }

    public void addDefNode(DefNode defNode) {
        defNodes.add(defNode);
    }
}
