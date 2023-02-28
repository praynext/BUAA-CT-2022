package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;

import static midend.MidCode.BinaryOperate.BinaryOp.ADD;
import static midend.MidCode.BinaryOperate.BinaryOp.MUL;

public class AssignNode extends StmtNode {
    private final SymbolTable symbolTable;
    private LValNode lValNode;
    private ExpNode expNode;

    public AssignNode(SymbolTable symbolTable, LValNode lValNode, ExpNode toExpNode) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
        this.expNode = toExpNode;
    }

    @Override
    public AssignNode simplify() {
        lValNode = lValNode.compute();
        expNode = expNode.simplify();
        return this;
    }

    @Override
    public Value generateMidCode() {
        DefNode defNode = symbolTable.getVariable(lValNode.getIdent()).simplify();
        int id = defNode.getId();
        LinkedList<ExpNode> dimensions = defNode.getDimensions();
        Value expValue = expNode.generateMidCode();
        if (dimensions.size() == 0) {
            Word value = new Word(lValNode.getIdent().getStringValue() + "@" + id);
            midCodeTable.addMidCode(new Move(false, value, expValue));
            return null;
        } else if (dimensions.size() == 1) {
            Value offset = lValNode.getDimensions().get(0).generateMidCode();
            Addr addr = new Addr();
            midCodeTable.addMidCode(new Assign(true, addr, new BinaryOperate(ADD,
                    new Addr(lValNode.getIdent().getStringValue() + "@" + id), offset)));
            midCodeTable.addVarInfo(addr, 1);
            midCodeTable.addMidCode(new Store(addr, expValue));
            return null;
        } else {
            Value rowValue = lValNode.getDimensions().get(0).generateMidCode();
            Word rowIndex = new Word();
            midCodeTable.addMidCode(new Assign(true, rowIndex, new BinaryOperate(MUL, rowValue,
                    new Imm(((NumberNode) defNode.getDimensions().get(1)).getValue()))));
            midCodeTable.addVarInfo(rowIndex, 1);
            Value colValue = lValNode.getDimensions().get(1).generateMidCode();
            Word colOffset = new Word();
            midCodeTable.addMidCode(new Assign(true, colOffset, new BinaryOperate(ADD, rowIndex, colValue)));
            midCodeTable.addVarInfo(colOffset, 1);
            Addr addr = new Addr();
            midCodeTable.addMidCode(new Assign(true, addr, new BinaryOperate(ADD,
                    new Addr(lValNode.getIdent().getStringValue() + "@" + id), colOffset)));
            midCodeTable.addVarInfo(addr, 1);
            midCodeTable.addMidCode(new Store(addr, expValue));
            return null;
        }
    }
}
