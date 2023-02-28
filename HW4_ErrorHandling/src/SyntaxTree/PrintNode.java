package SyntaxTree;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.Token;

import java.util.LinkedList;

public class PrintNode implements StmtNode {
    private Token formatString;
    private final LinkedList<ExpNode> args = new LinkedList<>();

    public PrintNode() {
    }

    public void setFormatString(Token formatString) {
        this.formatString = formatString;
    }

    public void addArg(ExpNode arg) {
        args.add(arg);
    }

    public void check() {
        int index = 0, count = 0;
        String string = formatString.getStringValue();
        String findString = "%d";
        while ((index = string.indexOf(findString, index)) != -1) {
            index += findString.length();
            count++;
        }
        if (count != args.size()) {
            ErrorHandler.getInstance().addError(new Error("l", formatString.getLine()));
        }
    }
}
