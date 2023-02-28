package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.Lexer.TypeCode;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.HashMap;

import static frontend.Lexer.TypeCode.*;
import static frontend.SyntaxTree.ExpNode.Type.BOOL;
import static frontend.SyntaxTree.ExpNode.Type.INT;

public class BinaryExpNode implements ExpNode {
    private final SymbolTable symbolTable;
    private final Token binaryOp;
    private final ExpNode leftExp;
    private final ExpNode rightExp;

    public BinaryExpNode(SymbolTable symbolTable, Token binaryOp, ExpNode leftExp, ExpNode rightExp) {
        this.symbolTable = symbolTable;
        this.binaryOp = binaryOp;
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    public Token getBinaryOp() {
        return binaryOp;
    }

    public ExpNode getLeftExp() {
        return leftExp;
    }

    public ExpNode getRightExp() {
        return rightExp;
    }

    public NumberNode fullyCalculate(Token binaryOp, NumberNode leftExp, NumberNode rightExp) {
        switch (binaryOp.getTypeCode()) {
            case PLUS:
                return new NumberNode(leftExp.getValue() + rightExp.getValue());
            case MINU:
                return new NumberNode(leftExp.getValue() - rightExp.getValue());
            case MULT:
                return new NumberNode(leftExp.getValue() * rightExp.getValue());
            case DIV:
                return new NumberNode(leftExp.getValue() / rightExp.getValue());
            case MOD:
                return new NumberNode(leftExp.getValue() % rightExp.getValue());
            case GRE:
                return new NumberNode(leftExp.getValue() > rightExp.getValue() ? 1 : 0);
            case GEQ:
                return new NumberNode(leftExp.getValue() >= rightExp.getValue() ? 1 : 0);
            case LSS:
                return new NumberNode(leftExp.getValue() < rightExp.getValue() ? 1 : 0);
            case LEQ:
                return new NumberNode(leftExp.getValue() <= rightExp.getValue() ? 1 : 0);
            case EQL:
                return new NumberNode(leftExp.getValue() == rightExp.getValue() ? 1 : 0);
            case NEQ:
                return new NumberNode(leftExp.getValue() != rightExp.getValue() ? 1 : 0);
            case AND:
                return new NumberNode(leftExp.getValue() != 0 && rightExp.getValue() != 0 ? 1 : 0);
            case OR:
                return new NumberNode(leftExp.getValue() != 0 || rightExp.getValue() != 0 ? 1 : 0);
            default:
                return null;
        }
    }

    public ExpNode partialCalculate(Token binaryOp, ExpNode leftExp, ExpNode rightExp) {
        if (leftExp instanceof NumberNode) {
            NumberNode left = (NumberNode) leftExp;
            switch (binaryOp.getTypeCode()) {
                case PLUS:
                    if (left.getValue() == 0) {
                        return rightExp;
                    } else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if ((right.binaryOp.getTypeCode() == PLUS || right.binaryOp.getTypeCode() == MINU) && right.leftExp instanceof NumberNode) {
                            return new BinaryExpNode(symbolTable, right.binaryOp, new NumberNode(left.getValue() +
                                    ((NumberNode) right.leftExp).getValue()), right.rightExp).simplify();
                        }
                    }
                    return new BinaryExpNode(symbolTable, binaryOp, leftExp, rightExp);
                case MINU:
                    if (left.getValue() == 0) {
                        return new UnaryExpNode(symbolTable, new Token(MINU, 0), rightExp).simplify();
                    } else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if ((right.binaryOp.getTypeCode() == PLUS || right.binaryOp.getTypeCode() == MINU) && right.leftExp instanceof NumberNode) {
                            return new BinaryExpNode(symbolTable, right.binaryOp, new NumberNode(left.getValue() -
                                    ((NumberNode) right.leftExp).getValue()), right.rightExp).simplify();
                        }
                    }
                    return new BinaryExpNode(symbolTable, binaryOp, leftExp, rightExp);
                case MULT:
                    if (left.getValue() == 0) {
                        return new NumberNode(0);
                    } else if (left.getValue() == 1) {
                        return rightExp;
                    } else if (left.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Token(MINU, 0), rightExp).simplify();
                    } else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if (right.leftExp instanceof NumberNode) {
                            if (right.binaryOp.getTypeCode() == MULT) {
                                return new BinaryExpNode(symbolTable, right.binaryOp, new NumberNode(left.getValue() *
                                        ((NumberNode) right.leftExp).getValue()), right.rightExp).simplify();
                            } else if (right.binaryOp.getTypeCode() == PLUS || right.binaryOp.getTypeCode() == MINU) {
                                return new BinaryExpNode(symbolTable, right.binaryOp,
                                        new NumberNode(left.getValue() * ((NumberNode) right.leftExp).getValue()),
                                        new BinaryExpNode(symbolTable, binaryOp, left, right.rightExp).simplify()).simplify();
                            }
                        }
                    }
                    return new BinaryExpNode(symbolTable, binaryOp, left, rightExp);
                case DIV:
                case MOD:
                    return left.getValue() == 0 ? new NumberNode(0) :
                            new BinaryExpNode(symbolTable, binaryOp, left, rightExp);
                case AND:
                    return left.getValue() == 0 ? new NumberNode(0) : rightExp;
                case OR:
                    return left.getValue() != 0 ? new NumberNode(1) : rightExp;
                default:
                    return new BinaryExpNode(symbolTable, binaryOp, leftExp, rightExp);
            }
        } else {
            NumberNode right = (NumberNode) rightExp;
            switch (binaryOp.getTypeCode()) {
                case PLUS:
                    if (right.getValue() == 0) {
                        return leftExp;
                    } else {
                        return new BinaryExpNode(symbolTable, binaryOp, right, leftExp).simplify();
                    }
                case MINU:
                    if (right.getValue() == 0) {
                        return leftExp;
                    } else {
                        return new BinaryExpNode(symbolTable, new Token(PLUS, 0),
                                new NumberNode(-((NumberNode) rightExp).getValue()), leftExp).simplify();
                    }
                case MULT:
                    if (right.getValue() == 0) {
                        return new NumberNode(0);
                    } else if (right.getValue() == 1) {
                        return leftExp;
                    } else if (right.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Token(MINU, 0), leftExp).simplify();
                    } else {
                        return new BinaryExpNode(symbolTable, binaryOp, right, leftExp).simplify();
                    }
                case DIV:
                    if (right.getValue() == 1) {
                        return leftExp;
                    } else if (right.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Token(MINU, 0), leftExp).simplify();
                    } else {
                        return new BinaryExpNode(symbolTable, binaryOp, leftExp, right);
                    }
                case MOD:
                    return right.getValue() == 1 || right.getValue() == -1 ? new NumberNode(0) :
                            new BinaryExpNode(symbolTable, binaryOp, leftExp, right);
                case AND:
                    return right.getValue() == 0 ? new NumberNode(0) : leftExp;
                case OR:
                    return right.getValue() != 0 ? new NumberNode(1) : leftExp;
                default:
                    return new BinaryExpNode(symbolTable, binaryOp, leftExp, rightExp);
            }
        }
    }

    @Override
    public Type getType() {
        if (binaryOp.isType(PLUS) || binaryOp.isType(MINU) || binaryOp.isType(MULT) || binaryOp.isType(DIV) || binaryOp.isType(MOD) || binaryOp.isType(BITAND)) {
            return INT;
        } else {
            return BOOL;
        }
    }

    @Override
    public ExpNode simplify() {
        if (binaryOp.isType(BITAND)) {
            return this;
        }
        ExpNode simLeft = leftExp.simplify();
        ExpNode simRight = rightExp.simplify();
        if (simLeft instanceof NumberNode && simRight instanceof NumberNode) {
            return fullyCalculate(binaryOp, (NumberNode) simLeft, (NumberNode) simRight);
        } else if (simLeft instanceof NumberNode || simRight instanceof NumberNode) {
            return partialCalculate(binaryOp, simLeft, simRight);
        }
        return new BinaryExpNode(symbolTable, binaryOp, simLeft, simRight);
    }

    @Override
    public Value generateMidCode() {
        HashMap<TypeCode, BinaryOperate.BinaryOp> map = new HashMap<TypeCode, BinaryOperate.BinaryOp>() {{
            put(BITAND, BinaryOperate.BinaryOp.BITAND);
            put(PLUS, BinaryOperate.BinaryOp.ADD);
            put(MINU, BinaryOperate.BinaryOp.SUB);
            put(MULT, BinaryOperate.BinaryOp.MUL);
            put(DIV, BinaryOperate.BinaryOp.DIV);
            put(MOD, BinaryOperate.BinaryOp.MOD);
            put(GRE, BinaryOperate.BinaryOp.GT);
            put(GEQ, BinaryOperate.BinaryOp.GE);
            put(LSS, BinaryOperate.BinaryOp.LT);
            put(LEQ, BinaryOperate.BinaryOp.LE);
            put(EQL, BinaryOperate.BinaryOp.EQ);
            put(NEQ, BinaryOperate.BinaryOp.NE);
            put(AND, BinaryOperate.BinaryOp.AND);
            put(OR, BinaryOperate.BinaryOp.OR);
        }};
        Value leftValue = leftExp.generateMidCode();
        Value rightValue = rightExp.generateMidCode();
        Value value = (leftValue instanceof Addr || rightValue instanceof Addr) ? new Addr() : new Word();
        new Assign(true, value, new BinaryOperate(map.get(binaryOp.getTypeCode()), leftValue, rightValue));
        return value;
    }
}
