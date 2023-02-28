package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;

public class LoopNode implements StmtNode {
    private ExpNode cond;
    private StmtNode loopStmt;

    public LoopNode() {
    }

    public void setCond(ExpNode cond) {
        this.cond = cond;
    }

    public void setLoopStmt(StmtNode loopStmt) {
        this.loopStmt = loopStmt;
    }

    public void check(Token funcDefType) {
        if (loopStmt instanceof BlockNode) {
            ((BlockNode) loopStmt).check(funcDefType);
        } else if (loopStmt instanceof ReturnNode && ((ReturnNode) loopStmt).hasReturnValue()) {
            ErrorHandler.getInstance().addError(new Error("f", ((ReturnNode) loopStmt).getLine()));
        }
    }
}
