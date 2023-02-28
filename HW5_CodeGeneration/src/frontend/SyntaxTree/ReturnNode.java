package frontend.SyntaxTree;

import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.Return;
import midend.MidCode.Value;

public class ReturnNode implements StmtNode {
    private final SymbolTable symbolTable;
    private final Token token;
    private ExpNode returnValue;

    public ReturnNode(SymbolTable symbolTable, Token token, ExpNode returnValue) {
        this.symbolTable = symbolTable;
        this.token = token;
        this.returnValue = returnValue;
    }

    public int getLine() {
        return token.getLine();
    }

    public boolean hasReturnValue() {
        return returnValue != null;
    }

    @Override
    public ReturnNode simplify() {
        if (returnValue != null) {
            returnValue = returnValue.simplify();
        }
        return this;
    }

    @Override
    public Value generateMidCode() {
        if (returnValue == null) {
            new Return();
        } else {
            new Return(returnValue.generateMidCode());
        }
        return null;
    }
}
