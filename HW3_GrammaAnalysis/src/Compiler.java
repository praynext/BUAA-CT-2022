import Lexer.Lexer;
import Parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("testfile.txt"));
            BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
            Lexer lexer = new Lexer(in);
            Parser parser = new Parser(lexer, out);
            parser.parseCompUnit();
            in.close();
            out.close();
        } catch (Exception ignored) {
        }
    }
}
