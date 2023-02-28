package frontend.SyntaxTree;

public abstract class BlockItemNode extends SyntaxNode {
    @Override
    abstract public BlockItemNode simplify();
}
