package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.Value;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static frontend.Lexer.TypeCode.INTTK;

public class BlockNode implements StmtNode {
    private final SymbolTable symbolTable;
    private LinkedList<BlockItemNode> blockItemNodes;
    private final int endLine;

    public BlockNode(SymbolTable symbolTable, LinkedList<BlockItemNode> blockItemNodes, int line) {
        this.symbolTable = symbolTable;
        this.blockItemNodes = blockItemNodes;
        this.endLine = line;
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

    public void complete() {
        if (blockItemNodes.size() == 0 || !(blockItemNodes.getLast() instanceof ReturnNode)) {
            blockItemNodes.add(new ReturnNode(symbolTable, null, null));
        }
    }

    @Override
    public BlockNode simplify() {
        blockItemNodes = blockItemNodes.stream().map(BlockItemNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public Value generateMidCode() {
        blockItemNodes.forEach(BlockItemNode::generateMidCode);
        return null;
    }
}
