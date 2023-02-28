package SyntaxTree;

import Lexer.Token;

import static SyntaxTree.ExpNode.Type.*;

public class FuncFParamNode extends DefNode {
    public FuncFParamNode(Token ident) {
        super(ident);
    }

    public boolean matchType(ExpNode.Type type) {
        if (dimensions.size() == 2) {
            return type == MATRIX;
        } else if (dimensions.size() == 1) {
            return type == ARRAY;
        } else {
            return type == INT;
        }
    }
}
