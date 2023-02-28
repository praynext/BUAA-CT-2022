package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.LabelTable.Label;
import midend.MidCode.*;

public class LoopNode implements StmtNode {
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
    public LoopNode simplify() {
        ExpNode simCond = cond.simplify();
        StmtNode simLoop = loopStmt.simplify();
        StmtNode branchNode = new BranchNode(symbolTable, simCond, simLoop, new BreakNode(symbolTable)).simplify();
        return new LoopNode(symbolTable, new NumberNode(1), branchNode);
    }

    @Override
    public Value generateMidCode() {
        Label loopBeginLabel = new Label();
        Label loopEndLabel = new Label();
        MidCodeTable.getInstance().setLoop(loopBeginLabel, loopEndLabel);
        Nop loopBegin = new Nop();
        Value condValue = cond.generateMidCode();
        new Branch(Branch.BranchOp.EQ, condValue, new Imm(0), loopEndLabel);
        loopStmt.generateMidCode();
        new Jump(loopBeginLabel);
        Nop loopEnd = new Nop();
        loopBeginLabel.setMidCode(loopBegin);
        loopEndLabel.setMidCode(loopEnd);
        MidCodeTable.getInstance().unsetLoop();
        return null;
    }
}
