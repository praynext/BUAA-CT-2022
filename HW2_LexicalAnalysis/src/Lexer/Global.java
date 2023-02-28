package Lexer;

import java.util.HashMap;

import static Lexer.TypeCode.*;


public class Global {
    private static final Global instance = new Global();
    public HashMap<String, TypeCode> str2code = new HashMap<>();
    public HashMap<Character, TypeCode> char2code = new HashMap<>();

    private Global() {
        str2code.put("main", MAINTK);
        str2code.put("const", CONSTTK);
        str2code.put("int", INTTK);
        str2code.put("break", BREAKTK);
        str2code.put("continue", CONTINUETK);
        str2code.put("if", IFTK);
        str2code.put("else", ELSETK);
        str2code.put("while", WHILETK);
        str2code.put("getint", GETINTTK);
        str2code.put("printf", PRINTFTK);
        str2code.put("return", RETURNTK);
        str2code.put("void", VOIDTK);
        str2code.put("&&", AND);
        str2code.put("||", OR);
        str2code.put("<=", LEQ);
        str2code.put(">=", GEQ);
        str2code.put("==", EQL);
        str2code.put("!=", NEQ);
        char2code.put('+', PLUS);
        char2code.put('-', MINU);
        char2code.put('*', MULT);
        char2code.put('/', DIV);
        char2code.put('%', MOD);
        char2code.put(';', SEMICN);
        char2code.put(',', COMMA);
        char2code.put('(', LPARENT);
        char2code.put(')', RPARENT);
        char2code.put('[', LBRACK);
        char2code.put(']', RBRACK);
        char2code.put('{', LBRACE);
        char2code.put('}', RBRACE);
        char2code.put('!', NOT);
        char2code.put('<', LSS);
        char2code.put('>', GRE);
        char2code.put('=', ASSIGN);
    }

    public static Global getInstance() {
        return instance;
    }

    public String getCategoryCode(String str) {
        return str2code.getOrDefault(str, IDENFR) + " " + str + "\n";
    }

    public String getCategoryCode(char ch) {
        return char2code.get(ch) + " " + ch + "\n";
    }
}