package frontend.SyntaxTree;

import midend.MidCode.Imm;
import midend.MidCode.Value;

import static frontend.SyntaxTree.ExpNode.Type.INT;

public class NumberNode implements ExpNode {
    private final int value;

    public NumberNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return INT;
    }

    @Override
    public NumberNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        return new Imm(value);
    }
}
