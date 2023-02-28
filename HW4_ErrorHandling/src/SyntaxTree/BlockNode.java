package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;

import java.util.LinkedList;

import static Lexer.TypeCode.INTTK;

public class BlockNode implements StmtNode {
    private final LinkedList<BlockItemNode> blockItemNodes = new LinkedList<>();
    private int endLine;

    public BlockNode() {
    }

    public void addBlockItemNode(BlockItemNode blockItemNode) {
        blockItemNodes.add(blockItemNode);
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public void check(Token funcDefType) {
        if (funcDefType.isType(INTTK)) {
            if (blockItemNodes.size() == 0) {
                ErrorHandler.getInstance().addError(new Error("g", endLine));
            } else if (!(blockItemNodes.getLast() instanceof ReturnNode)) {
                ErrorHandler.getInstance().addError(new Error("g", endLine));
            }
        } else {
            for (BlockItemNode blockItemNode : blockItemNodes) {
                if (blockItemNode instanceof BlockNode) {
                    ((BlockNode) blockItemNode).check(funcDefType);
                } else if (blockItemNode instanceof BranchNode) {
                    ((BranchNode) blockItemNode).check(funcDefType);
                } else if (blockItemNode instanceof LoopNode) {
                    ((LoopNode) blockItemNode).check(funcDefType);
                } else if (blockItemNode instanceof ReturnNode && ((ReturnNode) blockItemNode).hasReturnValue()) {
                    ErrorHandler.getInstance().addError(new Error("f", ((ReturnNode) blockItemNode).getLine()));
                }
            }
        }
    }
}
