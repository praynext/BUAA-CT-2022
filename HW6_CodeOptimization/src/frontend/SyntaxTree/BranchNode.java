package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;
import midend.LabelTable.Label;

import static frontend.Lexer.TypeCode.AND;
import static frontend.Lexer.TypeCode.OR;
import static midend.MidCode.Branch.BranchOp.EQ;

public class BranchNode extends StmtNode {
    private final SymbolTable symbolTable;
    private final ExpNode cond;
    private final StmtNode thenStmt;
    private final StmtNode elseStmt;

    public BranchNode(SymbolTable symbolTable, ExpNode cond, StmtNode thenStmt, StmtNode elseStmt) {
        this.symbolTable = symbolTable;
        this.cond = cond;
        this.thenStmt = thenStmt;
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

    @Override
    public StmtNode simplify() {
        ExpNode simCond = cond.simplify();
        StmtNode simThen = thenStmt.simplify();
        StmtNode simElse = elseStmt == null ? null : elseStmt.simplify();
        if (simCond instanceof NumberNode) {
            return ((NumberNode) simCond).getValue() == 0 ? simElse == null ? new NopNode() : simElse : simThen;
        } else if (simCond instanceof BinaryExpNode) {
            if (((BinaryExpNode) simCond).getBinaryOp().isType(AND)) {
                StmtNode newThen = new BranchNode(symbolTable, ((BinaryExpNode) simCond).getRightExp(), simThen, simElse).simplify();
                return new BranchNode(symbolTable, ((BinaryExpNode) simCond).getLeftExp(), newThen, simElse).simplify();
            } else if (((BinaryExpNode) simCond).getBinaryOp().isType(OR)) {
                StmtNode newElse = new BranchNode(symbolTable, ((BinaryExpNode) simCond).getRightExp(), simThen, simElse).simplify();
                return new BranchNode(symbolTable, ((BinaryExpNode) simCond).getLeftExp(), simThen, newElse).simplify();
            }
        }
        return new BranchNode(symbolTable, simCond, simThen, simElse);
    }

    @Override
    public Value generateMidCode() {
        Label thenEndLabel = new Label();
        Value condValue = cond.generateMidCode();
        midCodeTable.addMidCode(new Branch(EQ, condValue, new Imm(0), thenEndLabel));
        thenStmt.generateMidCode();
        if (elseStmt == null) {
            Nop thenEnd = new Nop();
            midCodeTable.addMidCode(thenEnd);
            thenEndLabel.setMidCode(thenEnd);
        } else {
            Label elseEndLabel = new Label();
            midCodeTable.addMidCode(new Jump(elseEndLabel));
            Nop thenEnd = new Nop();
            midCodeTable.addMidCode(thenEnd);
            elseStmt.generateMidCode();
            Nop elseEnd = new Nop();
            midCodeTable.addMidCode(elseEnd);
            thenEndLabel.setMidCode(thenEnd);
            elseEndLabel.setMidCode(elseEnd);
        }
        return null;
    }
}
