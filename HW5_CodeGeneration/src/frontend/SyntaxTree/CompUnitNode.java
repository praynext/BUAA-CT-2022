package frontend.SyntaxTree;

import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class CompUnitNode implements SyntaxNode {
    private final SymbolTable symbolTable;
    private LinkedList<DeclNode> declNodes;
    private LinkedList<FuncDefNode> funcDefNodes;
    private FuncDefNode mainFuncDefNode;

    public CompUnitNode(SymbolTable symbolTable, LinkedList<DeclNode> declNodes,
                        LinkedList<FuncDefNode> funcDefNodes, FuncDefNode mainFuncDefNode) {
        this.symbolTable = symbolTable;
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    @Override
    public CompUnitNode simplify() {
        declNodes = declNodes.stream().map(DeclNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        funcDefNodes = funcDefNodes.stream().map(FuncDefNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        mainFuncDefNode = mainFuncDefNode.simplify();
        return this;
    }

    @Override
    public Value generateMidCode() {
        declNodes.forEach(DeclNode::generateMidCode);
        MidCodeTable.getInstance().setCurFunc("main");
        new FuncCall("main");
        new Exit();
        mainFuncDefNode.generateMidCode();
        funcDefNodes.forEach(FuncDefNode::generateMidCode);
        return null;
    }
}
