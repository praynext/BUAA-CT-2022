package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.LabelTable.Label;
import midend.MidCode.FuncEntry;
import midend.MidCode.Value;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static frontend.Lexer.TypeCode.VOIDTK;

public class FuncDefNode extends SyntaxNode {
    private final SymbolTable symbolTable;
    private final Token funcDefType;
    private final Token ident;
    private LinkedList<FuncFParamNode> funcFParamNodes;
    private BlockNode blockNode;

    public FuncDefNode(SymbolTable symbolTable, Token funcDefType, Token ident, LinkedList<FuncFParamNode> funcFParamNodes) {
        this.symbolTable = symbolTable;
        this.funcDefType = funcDefType;
        this.ident = ident;
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

    @Override
    public FuncDefNode simplify() {
        funcFParamNodes = funcFParamNodes.stream().map(FuncFParamNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        blockNode = blockNode.simplify();
        if (funcDefType.isType(VOIDTK)) {
            blockNode.complete();
        }
        return this;
    }

    @Override
    public Value generateMidCode() {
        Label entryLabel = new Label(ident.getStringValue());
        FuncEntry funcEntry = new FuncEntry(entryLabel);
        midCodeTable.addMidCode(funcEntry);
        midCodeTable.setCurFunc(entryLabel.getLabelName());
        funcFParamNodes.forEach(FuncFParamNode::generateMidCode);
        blockNode.generateMidCode();
        entryLabel.setMidCode(funcEntry);
        return null;
    }
}
