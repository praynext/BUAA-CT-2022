package SyntaxTree;

import Lexer.Token;

public class ReturnNode implements StmtNode {
    private final Token token;
    private ExpNode returnValue = null;

    public ReturnNode(Token token) {
        this.token = token;
    }

    public void setReturnValue(ExpNode returnValue) {
        this.returnValue = returnValue;
    }

    public int getLine() {
        return token.getLine();
    }

    public boolean hasReturnValue() {
        return returnValue != null;
    }
}
