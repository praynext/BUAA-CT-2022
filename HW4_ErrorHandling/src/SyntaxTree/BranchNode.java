package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;

public class BranchNode implements StmtNode {
    private ExpNode cond;
    private StmtNode thenStmt;
    private StmtNode elseStmt = null;

    public BranchNode() {
    }

    public void setCond(ExpNode cond) {
        this.cond = cond;
    }

    public void setThenStmt(StmtNode thenStmt) {
        this.thenStmt = thenStmt;
    }

    public void setElseStmt(StmtNode elseStmt) {
        this.elseStmt = elseStmt;
    }

    public void check(Token funcDefType) {
        if (thenStmt instanceof BlockNode) {
            ((BlockNode) thenStmt).check(funcDefType);
        } else if (thenStmt instanceof ReturnNode && ((ReturnNode) thenStmt).hasReturnValue()) {
            ErrorHandler.getInstance().addError(new Error("f", ((ReturnNode) thenStmt).getLine()));
        }
        if (elseStmt instanceof BlockNode) {
            ((BlockNode) elseStmt).check(funcDefType);
        } else if (elseStmt instanceof ReturnNode && ((ReturnNode) elseStmt).hasReturnValue()) {
            ErrorHandler.getInstance().addError(new Error("f", ((ReturnNode) elseStmt).getLine()));
        }
    }
}
