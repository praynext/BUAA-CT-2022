package midend.MidCode;

import java.util.LinkedList;

public class BinaryOperate implements Operate {
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD, AND, OR, GT, GE, LT, LE, EQ, NE
    }

    private final BinaryOp binaryOp;
    private Value leftValue;
    private Value rightValue;

    public BinaryOperate(BinaryOp binaryOp, Value leftValue, Value rightValue) {
        this.binaryOp = binaryOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public BinaryOp getBinaryOp() {
        return binaryOp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Value getRightValue() {
        return rightValue;
    }

    @Override
    public LinkedList<Value> getUseVal() {
        LinkedList<Value> useVal = new LinkedList<>();
        useVal.add(leftValue);
        useVal.add(rightValue);
        return useVal;
    }

    @Override
    public void replaceUseVal(Value oldVal, Value newVal) {
        if (leftValue == oldVal) {
            leftValue = newVal;
        }
        if (rightValue == oldVal) {
            rightValue = newVal;
        }
    }

    @Override
    public String toString() {
        return leftValue.toString() + " " + binaryOp.toString() + " " + rightValue.toString();
    }
}
