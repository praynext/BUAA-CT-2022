package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import static Lexer.TypeCode.INTTK;
import static SyntaxTree.ExpNode.Type.INT;
import static SyntaxTree.ExpNode.Type.VOID;

public class FuncCallNode implements ExpNode {
    private final Token ident;
    private LinkedList<ExpNode> args = new LinkedList<>();

    public FuncCallNode(Token ident) {
        this.ident = ident;
    }

    public void setArgs(LinkedList<ExpNode> args) {
        this.args = args;
    }

    public void check(SymbolTable symbolTable) {
        FuncDefNode funcDefNode;
        if ((funcDefNode = symbolTable.getFunction(ident.getStringValue())) == null) {
            ErrorHandler.getInstance().addError(new Error("c", ident.getLine()));
        } else {
            LinkedList<FuncFParamNode> funcFParamNodes = funcDefNode.getFuncFParamNodes();
            if (funcFParamNodes.size() != args.size()) {
                ErrorHandler.getInstance().addError(new Error("d", ident.getLine()));
            } else {
                for (int i = 0; i < args.size(); i++) {
                    if (!funcFParamNodes.get(i).matchType(args.get(i).getType(symbolTable))) {
                        ErrorHandler.getInstance().addError(new Error("e", ident.getLine()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Type getType(SymbolTable symbolTable) {
        FuncDefNode funcDefNode;
        if ((funcDefNode = symbolTable.getFunction(ident.getStringValue())) == null) {
            return null;
        } else {
            return funcDefNode.getFuncDefType().isType(INTTK) ? INT : VOID;
        }
    }
}
