package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static frontend.Lexer.TypeCode.INTTK;
import static frontend.SyntaxTree.ExpNode.Type.INT;
import static frontend.SyntaxTree.ExpNode.Type.VOID;

public class FuncCallNode extends ExpNode {
    private final SymbolTable symbolTable;
    private final Token ident;
    private LinkedList<ExpNode> args;

    public FuncCallNode(SymbolTable symbolTable, Token ident, LinkedList<ExpNode> args) {
        this.symbolTable = symbolTable;
        this.ident = ident;
        this.args = args;
    }

    public void check() {
        FuncDefNode funcDefNode;
        if ((funcDefNode = symbolTable.getFunction(ident.getStringValue())) == null) {
            ErrorHandler.getInstance().addError(new Error("c", ident.getLine()));
        } else {
            LinkedList<FuncFParamNode> funcFParamNodes = funcDefNode.getFuncFParamNodes();
            if (funcFParamNodes.size() != args.size()) {
                ErrorHandler.getInstance().addError(new Error("d", ident.getLine()));
            } else {
                for (int i = 0; i < args.size(); i++) {
                    if (!funcFParamNodes.get(i).matchType(args.get(i).getType())) {
                        ErrorHandler.getInstance().addError(new Error("e", ident.getLine()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Type getType() {
        FuncDefNode funcDefNode;
        if ((funcDefNode = symbolTable.getFunction(ident.getStringValue())) == null) {
            return null;
        } else {
            return funcDefNode.getFuncDefType().isType(INTTK) ? INT : VOID;
        }
    }

    @Override
    public FuncCallNode simplify() {
        args = args.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public Value generateMidCode() {
        LinkedList<Value> values = new LinkedList<>();
        args.forEach(arg -> values.add(arg.generateMidCode()));
        values.forEach(value -> midCodeTable.addMidCode(new ArgPush(value)));
        midCodeTable.addMidCode(new FuncCall(ident.getStringValue()));
        if (symbolTable.getFunction(ident.getStringValue()).getFuncDefType().isType(INTTK)) {
            Word value = new Word();
            midCodeTable.addMidCode(new Move(true, value, new Word("?")));
            midCodeTable.addVarInfo(value, 1);
            return value;
        }
        return null;
    }
}
