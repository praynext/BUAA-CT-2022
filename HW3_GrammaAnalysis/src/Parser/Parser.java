package Parser;

import Lexer.Lexer;
import Lexer.Token;

import static Lexer.TypeCode.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;


public class Parser {
    private final Lexer lexer;
    private int index = -1;
    private Token curToken;
    private final LinkedList<Token> tokenList = new LinkedList<>();
    private final BufferedWriter out;

    public Parser(Lexer lexer, BufferedWriter out) {
        this.lexer = lexer;
        this.out = out;
    }

    public void getToken() throws IOException {
        index++;
        if (tokenList.size() == index) {
            tokenList.add(lexer.getToken());
        }
        curToken = tokenList.get(index);
    }

    public void retract(int stride) {
        index -= stride;
        if (index >= 0) {
            curToken = tokenList.get(index);
        }
    }

    public void parseCompUnit() throws IOException {
        getToken();
        while (curToken.typeCode == CONSTTK || curToken.typeCode == INTTK) {
            if (curToken.typeCode == CONSTTK) {
                retract(1);
                parseConstDecl();
            } else {
                getToken();
                if (curToken.typeCode != IDENFR) {
                    retract(1);
                    break;
                } else {
                    getToken();
                    if (curToken.typeCode == LPARENT) {
                        retract(2);
                        break;
                    } else {
                        retract(3);
                        parseVarDecl();
                    }
                }
            }
            getToken();
        }
        while (curToken.typeCode == INTTK || curToken.typeCode == VOIDTK) {
            if (curToken.typeCode == VOIDTK) {
                retract(1);
                parseFuncDef();
            } else {
                getToken();
                if (curToken.typeCode == IDENFR) {
                    retract(2);
                    parseFuncDef();
                } else {
                    retract(1);
                    break;
                }
            }
            getToken();
        }
        retract(1);
        parseMainFuncDef();
        out.write("<CompUnit>\n");
    }

    public void parseConstDecl() throws IOException {
        getToken();//const
        out.write(curToken.toString());
        getToken();//BType
        out.write(curToken.toString());
        parseConstDef();
        getToken();
        while (curToken.typeCode == COMMA) {
            out.write(curToken.toString());
            parseConstDef();
            getToken();
        }
        out.write(curToken.toString());//;
        out.write("<ConstDecl>\n");
    }

    public void parseConstDef() throws IOException {
        getToken();//Ident
        out.write(curToken.toString());
        getToken();
        while (curToken.typeCode == LBRACK) {
            out.write(curToken.toString());
            parseConstExp();
            getToken();//]
            out.write(curToken.toString());
            getToken();
        }
        out.write(curToken.toString());//=
        parseConstInitVal();
        out.write("<ConstDef>\n");
    }

    public void parseConstInitVal() throws IOException {
        getToken();
        if (curToken.typeCode != LBRACE) {
            retract(1);
            parseConstExp();
        } else {
            out.write(curToken.toString());
            getToken();
            if (curToken.typeCode != RBRACE) {
                retract(1);
                parseConstInitVal();
                getToken();
                while (curToken.typeCode == COMMA) {
                    out.write(curToken.toString());
                    parseConstInitVal();
                    getToken();
                }
            }
            out.write(curToken.toString());//}
        }
        out.write("<ConstInitVal>\n");
    }

    public void parseVarDecl() throws IOException {
        getToken();//BType
        out.write(curToken.toString());
        parseVarDef();
        getToken();
        while (curToken.typeCode == COMMA) {
            out.write(curToken.toString());
            parseVarDef();
            getToken();
        }
        out.write(curToken.toString());//;
        out.write("<VarDecl>\n");
    }

    public void parseVarDef() throws IOException {
        getToken();//Ident
        out.write(curToken.toString());
        getToken();
        while (curToken.typeCode == LBRACK) {
            out.write(curToken.toString());
            parseConstExp();
            getToken();//]
            out.write(curToken.toString());
            getToken();
        }
        if (curToken.typeCode == ASSIGN) {
            out.write(curToken.toString());
            parseInitVal();
        } else {
            retract(1);
        }
        out.write("<VarDef>\n");
    }

    public void parseInitVal() throws IOException {
        getToken();
        if (curToken.typeCode != LBRACE) {
            retract(1);
            parseExp();
        } else {
            out.write(curToken.toString());
            getToken();
            if (curToken.typeCode != RBRACE) {
                retract(1);
                parseInitVal();
                getToken();
                while (curToken.typeCode == COMMA) {
                    out.write(curToken.toString());
                    parseInitVal();
                    getToken();
                }
            }
            out.write(curToken.toString());//}
        }
        out.write("<InitVal>\n");
    }

    public void parseFuncDef() throws IOException {
        parseFuncType();
        getToken();//Ident
        out.write(curToken.toString());
        getToken();//(
        out.write(curToken.toString());
        getToken();
        if (curToken.typeCode != RPARENT) {
            retract(1);
            parseFuncFParams();
            getToken();
        }
        out.write(curToken.toString());//)
        parseBlock();
        out.write("<FuncDef>\n");
    }

    public void parseMainFuncDef() throws IOException {
        getToken();//int
        out.write(curToken.toString());
        getToken();//main
        out.write(curToken.toString());
        getToken();//(
        out.write(curToken.toString());
        getToken();//)
        out.write(curToken.toString());
        parseBlock();
        out.write("<MainFuncDef>\n");
    }

    public void parseFuncType() throws IOException {
        getToken();//
        out.write(curToken.toString());
        out.write("<FuncType>\n");
    }

    public void parseFuncFParams() throws IOException {
        parseFuncFParam();
        getToken();
        while (curToken.typeCode == COMMA) {
            out.write(curToken.toString());
            parseFuncFParam();
            getToken();
        }
        retract(1);
        out.write("<FuncFParams>\n");
    }

    public void parseFuncFParam() throws IOException {
        getToken();//BType
        out.write(curToken.toString());
        getToken();//Ident
        out.write(curToken.toString());
        getToken();
        if (curToken.typeCode == LBRACK) {
            out.write(curToken.toString());
            getToken();//]
            out.write(curToken.toString());
            getToken();
            while (curToken.typeCode == LBRACK) {
                out.write(curToken.toString());
                parseConstExp();
                getToken();//]
                out.write(curToken.toString());
                getToken();
            }
        }
        retract(1);
        out.write("<FuncFParam>\n");
    }

    public void parseBlock() throws IOException {
        getToken();//{
        out.write(curToken.toString());
        getToken();
        while (curToken.typeCode != RBRACE) {
            retract(1);
            parseBlockItem();
            getToken();
        }
        out.write(curToken.toString());//}
        out.write("<Block>\n");
    }

    public void parseBlockItem() throws IOException {
        getToken();
        if (curToken.typeCode == CONSTTK) {
            retract(1);
            parseConstDecl();
        } else if (curToken.typeCode == INTTK) {
            retract(1);
            parseVarDecl();
        } else {
            retract(1);
            parseStmt();
        }
    }

    public void parseStmt() throws IOException {
        getToken();
        switch (curToken.typeCode) {
            case SEMICN:
                out.write(curToken.toString());
                break;
            case LBRACE:
                retract(1);
                parseBlock();
                break;
            case IFTK:
                out.write(curToken.toString());
                getToken();//(
                out.write(curToken.toString());
                parseCond();
                getToken();//)
                out.write(curToken.toString());
                parseStmt();
                getToken();
                if (curToken.typeCode == ELSETK) {
                    out.write(curToken.toString());
                    parseStmt();
                } else {
                    retract(1);
                }
                break;
            case WHILETK:
                out.write(curToken.toString());
                getToken();//(
                out.write(curToken.toString());
                parseCond();
                getToken();//)
                out.write(curToken.toString());
                parseStmt();
                break;
            case BREAKTK:
            case CONTINUETK:
                out.write(curToken.toString());
                getToken();//;
                out.write(curToken.toString());
                break;
            case RETURNTK:
                out.write(curToken.toString());
                getToken();
                if (curToken.typeCode != SEMICN) {
                    retract(1);
                    parseExp();
                    getToken();
                }
                out.write(curToken.toString());
                break;
            case PRINTFTK:
                out.write(curToken.toString());
                getToken();//(
                out.write(curToken.toString());
                getToken();//STRCON
                out.write(curToken.toString());
                getToken();
                while (curToken.typeCode == COMMA) {
                    out.write(curToken.toString());
                    parseExp();
                    getToken();
                }
                out.write(curToken.toString());//)
                getToken();//;
                out.write(curToken.toString());
                break;
            default:
                int stride = 2;
                getToken();
                while (curToken.typeCode != ASSIGN && curToken.typeCode != SEMICN) {
                    stride++;
                    getToken();
                }
                if (curToken.typeCode == ASSIGN) {
                    retract(stride);
                    parseLVal();
                    getToken();//=
                    out.write(curToken.toString());
                    getToken();
                    if (curToken.typeCode == GETINTTK) {
                        out.write(curToken.toString());
                        getToken();//(
                        out.write(curToken.toString());
                        getToken();//)
                        out.write(curToken.toString());
                    } else {
                        retract(1);
                        parseExp();
                    }
                } else {
                    retract(stride);
                    parseExp();
                }
                getToken();//;
                out.write(curToken.toString());
        }
        out.write("<Stmt>\n");
    }

    public void parseExp() throws IOException {
        parseAddExp();
        out.write("<Exp>\n");
    }

    public void parseCond() throws IOException {
        parseLOrExp();
        out.write("<Cond>\n");
    }

    public void parseLVal() throws IOException {
        getToken();//Ident
        out.write(curToken.toString());
        getToken();
        while (curToken.typeCode == LBRACK) {
            out.write(curToken.toString());
            parseExp();
            getToken();//]
            out.write(curToken.toString());
            getToken();
        }
        retract(1);
        out.write("<LVal>\n");
    }

    public void parsePrimaryExp() throws IOException {
        getToken();
        if (curToken.typeCode == LPARENT) {
            out.write(curToken.toString());
            parseExp();
            getToken();//)
            out.write(curToken.toString());
        } else if (curToken.typeCode == INTCON) {
            out.write(curToken.toString());
            out.write("<Number>\n");
        } else {
            retract(1);
            parseLVal();
        }
        out.write("<PrimaryExp>\n");
    }

    public void parseUnaryExp() throws IOException {
        getToken();
        if (curToken.typeCode == PLUS || curToken.typeCode == MINU || curToken.typeCode == NOT) {
            out.write(curToken.toString());
            out.write("<UnaryOp>\n");
            parseUnaryExp();
        } else if (curToken.typeCode == IDENFR) {
            getToken();
            if (curToken.typeCode == LPARENT) {
                retract(1);
                out.write(curToken.toString());// Ident
                getToken();//(
                out.write(curToken.toString());
                getToken();
                if (curToken.typeCode != RPARENT) {
                    retract(1);
                    parseFuncRParams();
                    getToken();
                }
                out.write(curToken.toString());//)
            } else {
                retract(2);
                parsePrimaryExp();
            }
        } else {
            retract(1);
            parsePrimaryExp();
        }
        out.write("<UnaryExp>\n");
    }

    public void parseFuncRParams() throws IOException {
        parseExp();
        getToken();
        while (curToken.typeCode == COMMA) {
            out.write(curToken.toString());
            parseExp();
            getToken();
        }
        retract(1);
        out.write("<FuncRParams>\n");
    }

    public void parseMulExp() throws IOException {
        parseUnaryExp();
        out.write("<MulExp>\n");
        getToken();
        while (curToken.typeCode == MULT || curToken.typeCode == DIV || curToken.typeCode == MOD) {
            out.write(curToken.toString());
            parseUnaryExp();
            out.write("<MulExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseAddExp() throws IOException {
        parseMulExp();
        out.write("<AddExp>\n");
        getToken();
        while (curToken.typeCode == PLUS || curToken.typeCode == MINU) {
            out.write(curToken.toString());
            parseMulExp();
            out.write("<AddExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseRelExp() throws IOException {
        parseAddExp();
        out.write("<RelExp>\n");
        getToken();
        while (curToken.typeCode == LSS || curToken.typeCode == GRE ||
                curToken.typeCode == LEQ || curToken.typeCode == GEQ) {
            out.write(curToken.toString());
            parseAddExp();
            out.write("<RelExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseEqExp() throws IOException {
        parseRelExp();
        out.write("<EqExp>\n");
        getToken();
        while (curToken.typeCode == EQL || curToken.typeCode == NEQ) {
            out.write(curToken.toString());
            parseRelExp();
            out.write("<EqExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseLAndExp() throws IOException {
        parseEqExp();
        out.write("<LAndExp>\n");
        getToken();
        while (curToken.typeCode == AND) {
            out.write(curToken.toString());
            parseEqExp();
            out.write("<LAndExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseLOrExp() throws IOException {
        parseLAndExp();
        out.write("<LOrExp>\n");
        getToken();
        while (curToken.typeCode == OR) {
            out.write(curToken.toString());
            parseLAndExp();
            out.write("<LOrExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseConstExp() throws IOException {
        parseAddExp();
        out.write("<ConstExp>\n");
    }
}
