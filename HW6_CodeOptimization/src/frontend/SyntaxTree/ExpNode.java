package frontend.SyntaxTree;

public abstract class ExpNode extends StmtNode {
    enum Type {
        VOID, INT, BOOL, ARRAY, MATRIX
    }

    abstract public Type getType();

    abstract public ExpNode simplify();
}
