import Lexer.Lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("testfile.txt"));
            BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
            Lexer lexer = new Lexer(in, out);
            lexer.run();
            in.close();
            out.close();
        } catch (Exception ignored) {
        }
    }
}
