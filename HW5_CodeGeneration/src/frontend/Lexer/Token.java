package frontend.Lexer;

import frontend.Parser.ParsedUnit;

import java.util.Collections;
import java.util.HashMap;

import static frontend.Lexer.TypeCode.*;
import static frontend.Lexer.TypeCode.BITAND;

public class Token extends ParsedUnit {
    private final TypeCode typeCode;
    private String stringValue;
    private int intValue;
    private final int line;
    private static final HashMap<TypeCode, String> code2str = new HashMap<>();

    static {
        code2str.put(BITAND, "bitand");
        code2str.put(MAINTK, "main");
        code2str.put(CONSTTK, "const");
        code2str.put(INTTK, "int");
        code2str.put(BREAKTK, "break");
        code2str.put(CONTINUETK, "continue");
        code2str.put(IFTK, "if");
        code2str.put(ELSETK, "else");
        code2str.put(WHILETK, "while");
        code2str.put(GETINTTK, "getint");
        code2str.put(PRINTFTK, "printf");
        code2str.put(RETURNTK, "return");
        code2str.put(VOIDTK, "void");
        code2str.put(AND, "&&");
        code2str.put(OR, "||");
        code2str.put(LEQ, "<=");
        code2str.put(GEQ, ">=");
        code2str.put(EQL, "==");
        code2str.put(NEQ, "!=");
        code2str.put(PLUS, "+");
        code2str.put(MINU, "-");
        code2str.put(MULT, "*");
        code2str.put(DIV, "/");
        code2str.put(MOD, "%");
        code2str.put(SEMICN, ";");
        code2str.put(COMMA, ",");
        code2str.put(LPARENT, "(");
        code2str.put(RPARENT, ")");
        code2str.put(LBRACK, "[");
        code2str.put(RBRACK, "]");
        code2str.put(LBRACE, "{");
        code2str.put(RBRACE, "}");
        code2str.put(NOT, "!");
        code2str.put(LSS, "<");
        code2str.put(GRE, ">");
        code2str.put(ASSIGN, "=");
    }

    public Token(TypeCode typeCode, int line) {
        super(String.valueOf(typeCode), Collections.emptyList());
        this.typeCode = typeCode;
        this.line = line;
    }

    public Token(TypeCode typeCode, String stringValue, int line) {
        super(String.valueOf(typeCode), Collections.emptyList());
        this.typeCode = typeCode;
        this.stringValue = stringValue;
        this.line = line;
    }

    public Token(TypeCode typeCode, int intValue, int line) {
        super(String.valueOf(typeCode), Collections.emptyList());
        this.typeCode = typeCode;
        this.intValue = intValue;
        this.line = line;
    }

    public TypeCode getTypeCode() {
        return typeCode;
    }

    public boolean isType(TypeCode typeCode) {
        return this.typeCode == typeCode;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        if (typeCode == IDENFR || typeCode == STRCON) {
            return typeCode + " " + stringValue;
        } else if (typeCode == INTCON) {
            return typeCode + " " + intValue;
        } else {
            return typeCode + " " + code2str.get(typeCode);
        }
    }
}
