package frontend.SyntaxTree;

public interface StmtNode extends BlockItemNode {
    @Override
    StmtNode simplify();
}
