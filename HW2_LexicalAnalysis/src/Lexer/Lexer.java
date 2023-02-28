package Lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Lexer {
    private final BufferedReader in;
    private final BufferedWriter out;
    String line;
    int column;
    char ch;
    Boolean rowNote = false;
    Boolean multiNote = false;

    public Lexer(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public void run() throws IOException {
        while ((line = in.readLine()) != null) {
            rowNote = false;
            for (column = 0; column < line.length(); column++) {
                if (rowNote) {
                    break;
                } else if (multiNote) {
                    int end;
                    if ((end = line.indexOf("*/", column)) == -1) {
                        break;
                    } else {
                        column = end + 2;
                        multiNote = false;
                        if (column >= line.length()) {
                            break;
                        }
                    }
                }
                ch = line.charAt(column);
                if (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
                    continue;
                }
                if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_') {
                    parseIDENFR();
                } else if ('0' <= ch && ch <= '9') {
                    parseINT();
                } else if (ch == '\"') {
                    parseSTR();
                } else {
                    parseOTHER();
                }
            }
        }
    }

    public void parseIDENFR() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        while (++column < line.length()) {
            ch = line.charAt(column);
            if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_' || ('0' <= ch && ch <= '9')) {
                stringBuilder.append(ch);
            } else {
                column--;
                break;
            }
        }
        out.write(Global.getInstance().getCategoryCode(stringBuilder.toString()));
    }

    public void parseINT() throws IOException {
        int numBuilder = ch - '0';
        if (ch == '0') {
            out.write("INTCON 0\n");
        } else {
            while (++column < line.length()) {
                ch = line.charAt(column);
                if ('0' <= ch && ch <= '9') {
                    numBuilder *= 10;
                    numBuilder += ch - '0';
                } else {
                    column--;
                    break;
                }
            }
            out.write("INTCON " + numBuilder + "\n");
        }
    }

    public void parseSTR() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        while (++column < line.length()) {
            ch = line.charAt(column);
            if (ch != '\"') {
                stringBuilder.append(ch);
            } else {
                stringBuilder.append(ch);
                break;
            }
        }
        out.write("STRCON " + stringBuilder + "\n");
    }

    public void parseOTHER() throws IOException {
        switch (ch) {
            case '&':
            case '|':
                if (++column < line.length() && line.charAt(column) == ch) {
                    out.write(Global.getInstance().getCategoryCode(String.valueOf(ch) + ch));
                } else {
                    out.write(Global.getInstance().getCategoryCode(ch));
                    column--;
                }
                break;
            case '<':
            case '>':
            case '=':
            case '!':
                if (++column < line.length() && line.charAt(column) == '=') {
                    out.write(Global.getInstance().getCategoryCode(ch + "="));
                } else {
                    out.write(Global.getInstance().getCategoryCode(ch));
                    column--;
                }
                break;
            case '/':
                if (line.charAt(++column) == '/') {
                    rowNote = true;
                } else if (line.charAt(column) == '*') {
                    multiNote = true;
                } else {
                    out.write(Global.getInstance().getCategoryCode(ch));
                    column--;
                }
                break;
            default:
                out.write(Global.getInstance().getCategoryCode(ch));
        }
    }
}
