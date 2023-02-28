package midend.MidCode;

import java.util.LinkedList;

import static midend.MidCode.BinaryOperate.BinaryOp.*;
import static midend.MidCode.UnaryOperate.UnaryOp.NEG;
import static midend.MidCode.UnaryOperate.UnaryOp.POS;

public class Assign extends MidCode implements DefVal, UseVal {
    private final boolean isTemp;
    private final Value leftValue;
    private final Operate rightValue;

    public Assign(boolean isTemp, Value leftValue, Operate rightValue) {
        this.isTemp = isTemp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Operate getRightValue() {
        return rightValue;
    }

    public Imm fullyCalculate(BinaryOperate.BinaryOp op, Imm leftValue, Imm rightValue) {
        switch (op) {
            case ADD:
                return new Imm(leftValue.getValue() + rightValue.getValue());
            case SUB:
                return new Imm(leftValue.getValue() - rightValue.getValue());
            case MUL:
                return new Imm(leftValue.getValue() * rightValue.getValue());
            case DIV:
                return new Imm(leftValue.getValue() / rightValue.getValue());
            case MOD:
                return new Imm(leftValue.getValue() % rightValue.getValue());
            case AND:
                return new Imm(leftValue.getValue() == 1 && rightValue.getValue() == 1 ? 1 : 0);
            case OR:
                return new Imm(leftValue.getValue() == 1 || rightValue.getValue() == 1 ? 1 : 0);
            case EQ:
                return new Imm(leftValue.getValue() == rightValue.getValue() ? 1 : 0);
            case NE:
                return new Imm(leftValue.getValue() != rightValue.getValue() ? 1 : 0);
            case LT:
                return new Imm(leftValue.getValue() < rightValue.getValue() ? 1 : 0);
            case GT:
                return new Imm(leftValue.getValue() > rightValue.getValue() ? 1 : 0);
            case LE:
                return new Imm(leftValue.getValue() <= rightValue.getValue() ? 1 : 0);
            case GE:
                return new Imm(leftValue.getValue() >= rightValue.getValue() ? 1 : 0);
            default:
                return null;
        }
    }

    public void simplify() {
        if (rightValue instanceof UnaryOperate) {
            UnaryOperate operate = (UnaryOperate) rightValue;
            if (operate.getValue() instanceof Imm) {
                Imm imm = (Imm) operate.getValue();
                switch (operate.getUnaryOp()) {
                    case POS:
                        replaceBy(new Move(isTemp, leftValue, imm));
                        break;
                    case NEG:
                        replaceBy(new Move(isTemp, leftValue, new Imm(-imm.getValue())));
                        break;
                    case NOT:
                        replaceBy(new Move(isTemp, leftValue, new Imm(imm.getValue() != 0 ? 0 : 1)));
                        break;
                }
            } else if (operate.getUnaryOp() == POS) {
                replaceBy(new Move(isTemp, leftValue, operate.getValue()));
            }
        } else {
            BinaryOperate operate = (BinaryOperate) rightValue;
            if (operate.getLeftValue() instanceof Imm && operate.getRightValue() instanceof Imm) {
                Imm leftImm = (Imm) operate.getLeftValue();
                Imm rightImm = (Imm) operate.getRightValue();
                replaceBy(new Move(isTemp, leftValue, fullyCalculate(operate.getBinaryOp(), leftImm, rightImm)));
            } else if (operate.getLeftValue() instanceof Imm) {
                Imm leftImm = (Imm) operate.getLeftValue();
                switch (operate.getBinaryOp()) {
                    case ADD:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, operate.getRightValue()));
                        }
                        break;
                    case SUB:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Assign(isTemp, leftValue, new UnaryOperate(NEG, operate.getRightValue())));
                        }
                        break;
                    case MUL:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        } else if (leftImm.getValue() == 1) {
                            replaceBy(new Move(isTemp, leftValue, operate.getRightValue()));
                        } else if (leftImm.getValue() == -1) {
                            replaceBy(new Assign(isTemp, leftValue, new UnaryOperate(NEG, operate.getRightValue())));
                        }
                        break;
                    case DIV:
                    case MOD:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        }
                        break;
                    case AND:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        } else {
                            replaceBy(new Move(isTemp, leftValue, operate.getRightValue()));
                        }
                        break;
                    case OR:
                        if (leftImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, operate.getRightValue()));
                        } else {
                            replaceBy(new Move(isTemp, leftValue, new Imm(1)));
                        }
                        break;
                }
            } else if (operate.getRightValue() instanceof Imm) {
                Imm rightImm = (Imm) operate.getRightValue();
                switch (operate.getBinaryOp()) {
                    case ADD:
                    case SUB:
                        if (rightImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, operate.getLeftValue()));
                        }
                        break;
                    case MUL:
                        if (rightImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        } else if (rightImm.getValue() == 1) {
                            replaceBy(new Move(isTemp, leftValue, operate.getLeftValue()));
                        } else if (rightImm.getValue() == -1) {
                            replaceBy(new Assign(isTemp, leftValue, new UnaryOperate(NEG, getLeftValue())));
                        }
                        break;
                    case DIV:
                        if (rightImm.getValue() == 1) {
                            replaceBy(new Move(isTemp, leftValue, operate.getLeftValue()));
                        } else if (rightImm.getValue() == -1) {
                            replaceBy(new Assign(isTemp, leftValue, new UnaryOperate(NEG, operate.getLeftValue())));
                        }
                        break;
                    case MOD:
                        if (rightImm.getValue() == 1 || rightImm.getValue() == -1) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        }
                        break;
                    case AND:
                        if (rightImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                        } else {
                            replaceBy(new Move(isTemp, leftValue, operate.getLeftValue()));
                        }
                        break;
                    case OR:
                        if (rightImm.getValue() == 0) {
                            replaceBy(new Move(isTemp, leftValue, operate.getLeftValue()));
                        } else {
                            replaceBy(new Move(isTemp, leftValue, new Imm(1)));
                        }
                        break;
                }
            } else if (operate.getLeftValue() == operate.getRightValue()) {
                if (operate.getBinaryOp() == EQ) {
                    replaceBy(new Move(isTemp, leftValue, new Imm(1)));
                } else if (operate.getBinaryOp() == NE) {
                    replaceBy(new Move(isTemp, leftValue, new Imm(0)));
                }
            }
        }
    }

    @Override
    public Value getDefVal() {
        return leftValue;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        return rightValue.getUseVal();
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        rightValue.replaceUseVal(oldVal, newVal);
    }

    @Override
    public String toString() {
        return leftValue + " <- " + rightValue;
    }
}
