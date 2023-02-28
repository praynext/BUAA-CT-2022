package midend.MidCode;

import midend.LabelTable.Label;

import java.util.LinkedList;

import static midend.MidCode.Branch.BranchOp.*;

public class Branch extends MidCode implements UseVal {
    public enum BranchOp {
        GT, GE, LT, LE, EQ, NE
    }

    private BranchOp branchOp;
    private Value leftValue;
    private Value rightValue;
    private Label branchLabel;

    public Branch(BranchOp branchOp, Value leftValue, Value rightValue, Label branchLabel) {
        this.branchOp = branchOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.branchLabel = branchLabel;
    }

    public BranchOp getBranchOp() {
        return branchOp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Label getBranchLabel() {
        return branchLabel;
    }

    public void setLabel(Label target) {
        this.branchLabel = target;
    }

    public void simplify() {
        if (leftValue instanceof Imm && rightValue instanceof Imm) {
            Imm leftImm = (Imm) leftValue;
            Imm rightImm = (Imm) rightValue;
            boolean result;
            switch (branchOp) {
                case GT:
                    result = leftImm.getValue() > rightImm.getValue();
                    break;
                case GE:
                    result = leftImm.getValue() >= rightImm.getValue();
                    break;
                case LT:
                    result = leftImm.getValue() < rightImm.getValue();
                    break;
                case LE:
                    result = leftImm.getValue() <= rightImm.getValue();
                    break;
                case EQ:
                    result = leftImm.getValue() == rightImm.getValue();
                    break;
                case NE:
                    result = leftImm.getValue() != rightImm.getValue();
                    break;
                default:
                    result = false;
            }
            if (result) {
                this.replaceBy(new Jump(branchLabel));
            } else {
                this.delete();
            }
        } else if (leftValue.equals(rightValue)) {
            switch (branchOp) {
                case GT:
                case LT:
                case NE:
                    this.replaceBy(new Jump(branchLabel));
                    break;
                case GE:
                case LE:
                case EQ:
                    this.delete();
                    break;
            }
        }
    }

    public void negative(Label label) {
        switch (branchOp) {
            case GT:
                branchOp = LE;
                break;
            case GE:
                branchOp = LT;
                break;
            case LT:
                branchOp = GE;
                break;
            case LE:
                branchOp = GT;
                break;
            case EQ:
                branchOp = NE;
                break;
            case NE:
                branchOp = EQ;
                break;
        }
        branchLabel = label;
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
        return "BRANCH " + branchLabel + " IF " + leftValue + " " + branchOp + " " + rightValue;
    }
}
