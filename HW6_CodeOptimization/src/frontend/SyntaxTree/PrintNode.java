package frontend.SyntaxTree;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import midend.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class PrintNode extends StmtNode {
    private final SymbolTable symbolTable;
    private final Token formatString;
    private LinkedList<ExpNode> args;

    public PrintNode(SymbolTable symbolTable, Token formatString, LinkedList<ExpNode> args) {
        this.symbolTable = symbolTable;
        this.formatString = formatString;
        this.args = args;
    }

    public void check() {
        int index = 0, count = 0;
        char ch;
        boolean error = false, escape = false, format = false;
        String string = formatString.getStringValue();
        while (++index < string.length()) {
            ch = string.charAt(index);
            if (escape) {
                if (ch != 'n' && !error) {
                    ErrorHandler.getInstance().addError(new Error("a", formatString.getLine()));
                    error = true;
                }
                escape = false;
            }
            if (format) {
                if (ch != 'd' && !error) {
                    ErrorHandler.getInstance().addError(new Error("a", formatString.getLine()));
                    error = true;
                } else if (ch == 'd') count++;
                format = false;
            }
            if (ch == '\"') {
                break;
            } else if (ch == 32 || ch == 33 || ch == 37 || (40 <= ch && ch <= 126)) {
                format = ch == '%';
                escape = ch == '\\';
            } else if (!error) {
                ErrorHandler.getInstance().addError(new Error("a", formatString.getLine()));
                error = true;
            }
        }
        if (count != args.size()) {
            ErrorHandler.getInstance().addError(new Error("l", formatString.getLine()));
        }
    }

    @Override
    public PrintNode simplify() {
        args = args.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    @Override
    public Value generateMidCode() {
        LinkedList<midend.MidCode.Value> values = new LinkedList<>();
        args.forEach(arg -> values.add(arg.generateMidCode()));
        values.forEach(value -> midCodeTable.addMidCode(new ArgPush(value)));
        midCodeTable.addMidCode(new Print(formatString.getStringValue()));
        return null;
    }
}
