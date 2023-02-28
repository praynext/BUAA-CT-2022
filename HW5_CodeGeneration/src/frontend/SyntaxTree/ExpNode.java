package frontend.SyntaxTree;

public interface ExpNode extends StmtNode {
    enum Type {
        VOID, INT, BOOL, ARRAY, MATRIX
    }

    default void check() {
    }

    Type getType();

    ExpNode simplify();
}
