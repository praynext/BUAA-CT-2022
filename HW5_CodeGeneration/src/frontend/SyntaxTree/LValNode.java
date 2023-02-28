package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static frontend.SyntaxTree.ExpNode.Type.*;
import static midend.MidCode.BinaryOperate.BinaryOp.ADD;
import static midend.MidCode.BinaryOperate.BinaryOp.MUL;

public class LValNode implements ExpNode {
    private final SymbolTable symbolTable;
    private final Token ident;
    private LinkedList<ExpNode> dimensions;

    public LValNode(SymbolTable symbolTable, Token ident, LinkedList<ExpNode> dimensions) {
        this.symbolTable = symbolTable;
        this.ident = ident;
        this.dimensions = dimensions;
    }

    public Token getIdent() {
        return ident;
    }

    public LinkedList<ExpNode> getDimensions() {
        return dimensions;
    }

    public void checkC() {
        if (symbolTable.getVariable(ident) == null) {
            ErrorHandler.getInstance().addError(new Error("c", ident.getLine()));
        }
    }

    public void checkH() {
        DefNode variable;
        if ((variable = symbolTable.getVariable(ident)) != null) {
            if (variable.isFinal()) {
                ErrorHandler.getInstance().addError(new Error("h", ident.getLine()));
            }
        }
    }

    @Override
    public void check() {
        checkC();
    }

    @Override
    public Type getType() {
        if (symbolTable.getVariable(ident) == null) {
            return null;
        }
        int dimension = symbolTable.getVariable(ident).getDimensions().size();
        if (dimensions.size() == 0) {
            return dimension == 0 ? INT : dimension == 1 ? ARRAY : MATRIX;
        } else if (dimensions.size() == 1) {
            return dimension == 1 ? INT : ARRAY;
        } else {
            return INT;
        }
    }

    public LValNode compute() {
        dimensions = dimensions.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public ExpNode simplify() {
        dimensions = dimensions.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        if (dimensions.stream().allMatch(item -> item instanceof NumberNode)) {
            DefNode defNode = symbolTable.getVariable(ident);
            if (defNode.isFinal() && dimensions.size() == defNode.getDimensions().size()) {
                return defNode.getValue(dimensions);
            }
        }
        return this;
    }

    @Override
    public Value generateMidCode() {
        DefNode defNode = symbolTable.getVariable(ident).simplify();
        int id = defNode.getId();
        LinkedList<ExpNode> dimensions = defNode.getDimensions();
        if (dimensions.size() == 0) {
            Word value = new Word();
            new Move(true, value, new Word(ident.getStringValue() + "@" + id));
            return value;
        } else if (dimensions.size() == 1) {
            if (this.dimensions.size() == 0) {
                Addr value = new Addr();
                new Move(true, value, new Addr(ident.getStringValue() + "@" + id));
                return value;
            } else {
                Value offsetValue = this.dimensions.get(0).generateMidCode();
                Addr addr = new Addr();
                new Assign(true, addr, new BinaryOperate(ADD,
                        new Addr(ident.getStringValue() + "@" + id), offsetValue));
                Word value = new Word();
                new Load(true, value, addr);
                return value;
            }
        } else {
            if (this.dimensions.size() == 0) {
                Addr value = new Addr();
                new Move(true, value, new Addr(ident.getStringValue() + "@" + id));
                return value;
            } else if (this.dimensions.size() == 1) {
                Value offsetValue = this.dimensions.get(0).generateMidCode();
                Word offset = new Word();
                new Assign(true, offset, new BinaryOperate(MUL,
                        offsetValue, new Imm(((NumberNode) dimensions.get(1)).getValue())));
                Addr value = new Addr();
                new Assign(true, value, new BinaryOperate(ADD,
                        new Addr(ident.getStringValue() + "@" + id), offset));
                return value;
            } else {
                Value rowOffsetValue = this.dimensions.get(0).generateMidCode();
                Word rowOffset = new Word();
                new Assign(true, rowOffset, new BinaryOperate(MUL,
                        rowOffsetValue, new Imm(((NumberNode) dimensions.get(1)).getValue())));
                Value colOffsetValue = this.dimensions.get(1).generateMidCode();
                Word colIndex = new Word();
                new Assign(true, colIndex, new BinaryOperate(ADD, colOffsetValue, rowOffset));
                Addr addr = new Addr();
                new Assign(true, addr, new BinaryOperate(ADD,
                        new Addr(ident.getStringValue() + "@" + id), colIndex));
                Word value = new Word();
                new Load(true, value, addr);
                return value;
            }
        }
    }
}
