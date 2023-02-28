package SyntaxTree;

import java.util.LinkedList;

public class CompUnit {
    private final LinkedList<DeclNode> declNodes = new LinkedList<>();
    private final LinkedList<FuncDefNode> funcDefNodes = new LinkedList<>();
    private FuncDefNode mainFuncDefNode;

    public CompUnit() {
    }

    public void addDeclNode(DeclNode declNode) {
        declNodes.add(declNode);
    }

    public void addFuncDefNode(FuncDefNode funcDefNode) {
        funcDefNodes.add(funcDefNode);
    }

    public void setMainFuncDefNode(FuncDefNode mainFuncDefNode) {
        this.mainFuncDefNode = mainFuncDefNode;
    }
}
