import backend.Translator;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Lexer;
import frontend.Parser.ParsedUnit;
import frontend.Parser.Parser;
import frontend.SyntaxTree.CompUnitNode;
import midend.MidCode.MidCodeTable;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        BufferedReader stdin = new BufferedReader(new FileReader("testfile.txt"));
        BufferedWriter stdout = new BufferedWriter(new FileWriter("output.txt"));
        BufferedWriter stderr = new BufferedWriter(new FileWriter("error.txt"));
        Lexer lexer = new Lexer(stdin);
        Parser parser = new Parser(lexer);
        ParsedUnit compUnit = parser.parseCompUnit();
        stdout.write(compUnit.toString());
        stdout.close();
        CompUnitNode compUnitNode = compUnit.toCompUnitNode();
        ErrorHandler.getInstance().log(stderr);
        System.out.println("session1");
        compUnitNode = compUnitNode.simplify();
        System.out.println("session2");
        compUnitNode.generateMidCode();
        System.out.println("session3");
        MidCodeTable.getInstance().simplify();
        System.out.println("session4");
        BufferedWriter mid = new BufferedWriter(new FileWriter("midcode.txt"));
        mid.write(MidCodeTable.getInstance().toString());
        mid.close();
        Translator.getInstance().translate();
        BufferedWriter mips = new BufferedWriter(new FileWriter("mips.txt"));
        mips.write(Translator.getInstance().toString());
        mips.close();
    }
}
