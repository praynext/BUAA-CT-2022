import ErrorHandler.ErrorHandler;
import Lexer.Lexer;
import Parser.Parser;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        BufferedReader stdin = new BufferedReader(new FileReader("testfile.txt"));
        BufferedWriter stdout = new BufferedWriter(new FileWriter("output.txt"));
        BufferedWriter stderr = new BufferedWriter(new FileWriter("error.txt"));
        Lexer lexer = new Lexer(stdin);
        Parser parser = new Parser(lexer, stdout);
        parser.parseCompUnit();
        ErrorHandler.getInstance().log(stderr);
    }
}
