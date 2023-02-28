package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;

import static midend.MidCode.BinaryOperate.BinaryOp.ADD;
import static midend.MidCode.BinaryOperate.BinaryOp.MUL;

public class GetIntNode implements StmtNode {
    private final SymbolTable symbolTable;
    private LValNode lValNode;

    public GetIntNode(SymbolTable symbolTable, LValNode lValNode) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
    }

    @Override
    public GetIntNode simplify() {
        lValNode = lValNode.compute();
        return this;
    }

    @Override
    public Value generateMidCode() {
        DefNode defNode = symbolTable.getVariable(lValNode.getIdent()).simplify();
        int id = defNode.getId();
        LinkedList<ExpNode> dimensions = defNode.getDimensions();
        new IntGet();
        if (dimensions.size() == 0) {
            Word value = new Word(lValNode.getIdent().getStringValue() + "@" + id);
            new Move(false, value, new Word("?"));
            return null;
        } else if (dimensions.size() == 1) {
            Value offset = lValNode.getDimensions().get(0).generateMidCode();
            Addr addr = new Addr();
            new Assign(true, addr, new BinaryOperate(ADD,
                    new Addr(lValNode.getIdent().getStringValue() + "@" + id), offset));
            new Store(addr, new Word("?"));
            return null;
        } else {
            Value rowValue = lValNode.getDimensions().get(0).generateMidCode();
            Word rowIndex = new Word();
            new Assign(true, rowIndex, new BinaryOperate(MUL, rowValue,
                    new Imm(((NumberNode) defNode.getDimensions().get(1)).getValue())));
            Value colValue = lValNode.getDimensions().get(1).generateMidCode();
            Word colOffset = new Word();
            new Assign(true, colOffset, new BinaryOperate(ADD, rowIndex, colValue));
            Addr addr = new Addr();
            new Assign(true, addr, new BinaryOperate(ADD,
                    new Addr(lValNode.getIdent().getStringValue() + "@" + id), colOffset));
            new Store(addr, new Word("?"));
            return null;
        }
    }
}
