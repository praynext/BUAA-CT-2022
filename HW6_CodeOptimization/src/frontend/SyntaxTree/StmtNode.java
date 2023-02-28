package frontend.SyntaxTree;

public abstract class StmtNode extends BlockItemNode {
    @Override
    abstract public StmtNode simplify();
}
