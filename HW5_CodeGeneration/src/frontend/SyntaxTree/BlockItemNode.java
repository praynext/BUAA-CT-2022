package frontend.SyntaxTree;

public interface BlockItemNode extends SyntaxNode {
    @Override
    BlockItemNode simplify();
}
