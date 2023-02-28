package SyntaxTree;

import Lexer.Token;

import java.util.LinkedList;

public class FuncDefNode {
    private final Token funcDefType;
    private Token ident;
    private LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
    private BlockNode blockNode;

    public FuncDefNode(Token funcDefType) {
        this.funcDefType = funcDefType;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setFuncFParams(LinkedList<FuncFParamNode> funcFParamNodes) {
        this.funcFParamNodes = funcFParamNodes;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public Token getFuncDefType() {
        return funcDefType;
    }

    public Token getIdent() {
        return ident;
    }

    public LinkedList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    public void check() {
        blockNode.check(funcDefType);
    }
}
