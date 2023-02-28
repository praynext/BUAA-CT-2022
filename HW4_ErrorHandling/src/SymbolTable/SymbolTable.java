package SymbolTable;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;
import SyntaxTree.DefNode;
import SyntaxTree.FuncDefNode;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, DefNode> variables = new HashMap<>();
    private static final HashMap<String, FuncDefNode> functions = new HashMap<>();
    public SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public DefNode getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.getVariable(name);
        } else {
            return null;
        }
    }

    public FuncDefNode getFunction(String name) {
        return functions.get(name);
    }

    public void addVariable(DefNode defNode) {
        Token ident = defNode.getIdent();
        if (variables.containsKey(ident.getStringValue()) ||
                (parent == null && functions.containsKey(ident.getStringValue()))) {
            ErrorHandler.getInstance().addError(new Error("b", ident.getLine()));
        } else {
            variables.put(ident.getStringValue(), defNode);
        }
    }

    public void addFunction(FuncDefNode funcDefNode) {
        Token ident = funcDefNode.getIdent();
        if ((parent == null && variables.containsKey(ident.getStringValue())) ||
                functions.containsKey(ident.getStringValue())) {
            ErrorHandler.getInstance().addError(new Error("b", ident.getLine()));
        } else {
            functions.put(ident.getStringValue(), funcDefNode);
        }
    }
}
