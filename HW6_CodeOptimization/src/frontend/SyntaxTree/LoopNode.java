package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.LabelTable.Label;
import midend.MidCode.*;

public class LoopNode extends StmtNode {
    private final SymbolTable symbolTable;
    private final ExpNode cond;
    private final StmtNode loopStmt;

    public LoopNode(SymbolTable symbolTable, ExpNode loopCond, StmtNode loopStmt) {
        this.symbolTable = symbolTable;
        this.cond = loopCond;
        this.loopStmt = loopStmt;
    }

    public void check(Token funcDefType) {
        if (loopStmt instanceof BlockNode) {
            ((BlockNode) loopStmt).check(funcDefType);
        } else if (loopStmt instanceof ReturnNode && ((ReturnNode) loopStmt).hasReturnValue()) {
            ErrorHandler.getInstance().addError(new Error("f", ((ReturnNode) loopStmt).getLine()));
        }
    }

    @Override
    public StmtNode simplify() {
        ExpNode simCond = cond.simplify();
        StmtNode simLoop = loopStmt.simplify();
        if (simCond instanceof NumberNode && ((NumberNode) simCond).getValue() == 0) {
            return new NopNode();
        }
        return new LoopNode(symbolTable, simCond, simLoop);
    }

    @Override
    public Value generateMidCode() {
        Label loopBeginLabel = new Label();
        Label loopEndLabel = new Label();
        Label stmtBeginLabel = new Label();
        midCodeTable.setLoop(loopBeginLabel, loopEndLabel);
        midCodeTable.markLoop(stmtBeginLabel);
        Nop loopBegin = new Nop();
        midCodeTable.addMidCode(loopBegin);
        StmtNode beginBranch = new BranchNode(symbolTable, cond, new NopNode(), new JumpNode(loopEndLabel)).simplify();
        beginBranch.generateMidCode();
        Nop stmtBegin = new Nop();
        midCodeTable.addMidCode(stmtBegin);
        loopStmt.generateMidCode();
        StmtNode endBranch = new BranchNode(symbolTable, cond, new JumpNode(stmtBeginLabel), null).simplify();
        endBranch.generateMidCode();
        Nop loopEnd = new Nop();
        midCodeTable.addMidCode(loopEnd);
        loopBeginLabel.setMidCode(loopBegin);
        loopEndLabel.setMidCode(loopEnd);
        stmtBeginLabel.setMidCode(stmtBegin);
        midCodeTable.unsetLoop();
        return null;
    }
}
