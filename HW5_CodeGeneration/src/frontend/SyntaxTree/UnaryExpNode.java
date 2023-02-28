package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.Lexer.TypeCode;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.HashMap;

import static frontend.Lexer.TypeCode.*;

public class UnaryExpNode implements ExpNode {
    private final SymbolTable symbolTable;
    private final Token unaryOp;
    private final ExpNode expNode;

    public UnaryExpNode(SymbolTable symbolTable, Token unaryOp, ExpNode expNode) {
        this.symbolTable = symbolTable;
        this.unaryOp = unaryOp;
        this.expNode = expNode;
    }

    public NumberNode operate(Token unaryOp, NumberNode expNode) {
        switch (unaryOp.getTypeCode()) {
            case PLUS:
                return new NumberNode(expNode.getValue());
            case MINU:
                return new NumberNode(-expNode.getValue());
            case NOT:
                return new NumberNode(expNode.getValue() == 0 ? 1 : 0);
            default:
                return null;
        }
    }

    @Override
    public Type getType() {
        return expNode.getType();
    }

    @Override
    public ExpNode simplify() {
        ExpNode simExp = expNode.simplify();
        if (simExp instanceof NumberNode) {
            return operate(unaryOp, (NumberNode) simExp);
        } else if (unaryOp.isType(PLUS)) {
            return simExp;
        } else if (simExp instanceof UnaryExpNode) {
            UnaryExpNode simUnary = (UnaryExpNode) simExp;
            if (simUnary.unaryOp.isType(MINU) && unaryOp.isType(MINU)) {
                return simUnary.expNode;
            } else if (simUnary.unaryOp.isType(NOT) && unaryOp.isType(NOT)) {
                return simUnary.expNode;
            }
        } else if (simExp instanceof BinaryExpNode) {
            BinaryExpNode simBinary = (BinaryExpNode) simExp;
            if (unaryOp.isType(MINU)) {
                if (simBinary.getLeftExp() instanceof NumberNode) {
                    NumberNode left = (NumberNode) simBinary.getLeftExp();
                    if (simBinary.getBinaryOp().isType(PLUS)) {
                        return new BinaryExpNode(symbolTable, new Token(MINU, 0),
                                new NumberNode(-left.getValue()), simBinary.getRightExp());
                    } else if (simBinary.getBinaryOp().isType(MINU)) {
                        return new BinaryExpNode(symbolTable, new Token(PLUS, 0),
                                new NumberNode(-left.getValue()), simBinary.getRightExp());
                    } else if (simBinary.getBinaryOp().isType(MULT)) {
                        return new BinaryExpNode(symbolTable, new Token(MULT, 0),
                                new NumberNode(-left.getValue()), simBinary.getRightExp());
                    }
                }
            } else if (unaryOp.isType(NOT)) {
                ExpNode left = new UnaryExpNode(symbolTable, unaryOp, simBinary.getLeftExp()).simplify();
                ExpNode right = new UnaryExpNode(symbolTable, unaryOp, simBinary.getRightExp()).simplify();
                return new BinaryExpNode(symbolTable, simBinary.getBinaryOp().isType(AND) ?
                        new Token(OR, 0) : new Token(AND, 0), left, right);
            }
        }
        return new UnaryExpNode(symbolTable, unaryOp, simExp);
    }

    @Override
    public Value generateMidCode() {
        HashMap<TypeCode, UnaryOperate.UnaryOp> map = new HashMap<TypeCode, UnaryOperate.UnaryOp>() {{
            put(PLUS, UnaryOperate.UnaryOp.POS);
            put(MINU, UnaryOperate.UnaryOp.NEG);
            put(NOT, UnaryOperate.UnaryOp.NOT);
        }};
        Value expValue = expNode.generateMidCode();
        Word value = new Word();
        new Assign(true, value, new UnaryOperate(map.get(unaryOp.getTypeCode()), expValue));
        return value;
    }
}
