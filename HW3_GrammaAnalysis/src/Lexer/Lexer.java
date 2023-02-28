package Lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import static Lexer.TypeCode.*;

public class Lexer {
    private final BufferedReader in;
    private String line;
    private int column;
    private char ch;
    private Boolean rowNote = false;
    private Boolean multiNote = false;
    private Token curToken;
    public HashMap<String, TypeCode> str2code = new HashMap<>();
    public HashMap<Character, TypeCode> char2code = new HashMap<>();

    public Lexer(BufferedReader in) throws IOException {
        this.in = in;
        line = in.readLine();
        column = 0;
        initMap();
    }

    private void initMap() {
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

    public boolean isWord(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_';
    }

    public boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    public void switchLine() throws IOException {
        rowNote = false;
        column = 0;
        line = in.readLine();
    }

    public Token getToken() throws IOException {
        if (line == null) {
            curToken = new Token(TypeCode.TYPE_EOF);
            return curToken;
        } else {
            while (column < line.length()) {
                if (rowNote) {
                    switchLine();
                    return getToken();
                } else if (multiNote) {
                    int end;
                    if ((end = line.indexOf("*/", column)) == -1) {
                        switchLine();
                        return getToken();
                    } else {
                        column = end + 2;
                        multiNote = false;
                        if (column >= line.length()) {
                            switchLine();
                            return getToken();
                        }
                    }
                }
                ch = line.charAt(column);
                if (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
                    column++;
                } else if (isWord(ch)) {
                    parseIDENFR();
                    return curToken;
                } else if (isDigit(ch)) {
                    parseINT();
                    return curToken;
                } else if (ch == '\"') {
                    parseSTR();
                    return curToken;
                } else if (parseOTHER()) {
                    return curToken;
                }
            }
            switchLine();
            return getToken();
        }
    }

    public void parseIDENFR() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        while (++column < line.length()) {
            ch = line.charAt(column);
            if (isDigit(ch) || isWord(ch)) {
                stringBuilder.append(ch);
            } else {
                break;
            }
        }
        curToken = new Token(str2code.getOrDefault(stringBuilder.toString(), IDENFR), stringBuilder.toString());
    }

    public void parseINT() {
        if (ch == '0') {
            column++;
            curToken = new Token(INTCON, 0);
        } else {
            int numBuilder = ch - '0';
            while (++column < line.length()) {
                ch = line.charAt(column);
                if (isDigit(ch)) {
                    numBuilder *= 10;
                    numBuilder += ch - '0';
                } else {
                    break;
                }
            }
            curToken = new Token(INTCON, numBuilder);
        }
    }

    public void parseSTR() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        while (++column < line.length()) {
            ch = line.charAt(column);
            if (ch != '\"') {
                stringBuilder.append(ch);
            } else {
                column++;
                stringBuilder.append(ch);
                break;
            }
        }
        curToken = new Token(STRCON, stringBuilder.toString());
    }

    public Boolean parseOTHER() {
        switch (ch) {
            case '&':
            case '|':
                if (++column < line.length() && line.charAt(column) == ch) {
                    column++;
                    curToken = new Token(str2code.get(String.valueOf(ch) + ch));
                } else {
                    curToken = new Token(char2code.get(ch));
                }
                return true;
            case '<':
            case '>':
            case '=':
            case '!':
                if (++column < line.length() && line.charAt(column) == '=') {
                    column++;
                    curToken = new Token(str2code.get(ch + "="));
                } else {
                    curToken = new Token(char2code.get(ch));
                }
                return true;
            case '/':
                if (line.charAt(++column) == '/') {
                    column++;
                    rowNote = true;
                } else if (line.charAt(column) == '*') {
                    column++;
                    multiNote = true;
                } else {
                    curToken = new Token(char2code.get(ch));
                    return true;
                }
                return false;
            default:
                column++;
                curToken = new Token(char2code.getOrDefault(ch, TYPE_UNDEFINED));
                return true;
        }
    }
}
