package midend.MidCode;

import midend.LabelTable.Label;

public class Branch implements MidCode {
    public enum BranchOp {
        GT, GE, LT, LE, EQ, NE
    }

    private final BranchOp branchOp;
    private final Value leftValue;
    private final Value rightValue;
    private Label branchLabel;

    public Branch(BranchOp branchOp, Value leftValue, Value rightValue, Label branchLabel) {
        this.branchOp = branchOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.branchLabel = branchLabel;
        MidCodeTable.getInstance().addMidCode(this);
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

    @Override
    public String toString() {
        return "BRANCH " + branchLabel + " IF " + leftValue + " " + branchOp + " " + rightValue;
    }
}
