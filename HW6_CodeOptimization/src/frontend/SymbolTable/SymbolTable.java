package frontend.SymbolTable;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SyntaxTree.DefNode;
import frontend.SyntaxTree.FuncDefNode;

import java.util.HashMap;

public class SymbolTable {
    private static int count = 0;
    private final int id;
    private final HashMap<String, DefNode> variables = new HashMap<>();
    private static final HashMap<String, FuncDefNode> functions = new HashMap<>();
    public SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.id = count++;
    }

    public int getId() {
        return id;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public DefNode getVariable(Token ident) {
        if (variables.containsKey(ident.getStringValue()) &&
                variables.get(ident.getStringValue()).getIdent().getLine() <= ident.getLine()) {
            return variables.get(ident.getStringValue());
        } else if (parent != null) {
            return parent.getVariable(ident);
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
