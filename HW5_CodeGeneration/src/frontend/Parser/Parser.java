package frontend.Parser;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.*;

import static frontend.Lexer.TypeCode.*;

import java.io.IOException;
import java.util.LinkedList;

public class Parser {
    private final Lexer lexer;
    private int index = -1;
    private Token curToken;
    private final LinkedList<Token> tokenList = new LinkedList<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void getToken() throws IOException {
        index++;
        if (tokenList.size() == index) {
            tokenList.add(lexer.getToken());
        }
        curToken = tokenList.get(index);
    }

    public boolean getToken(TypeCode... typeCodes) throws IOException {
        getToken();
        for (TypeCode typeCode : typeCodes) {
            if (curToken.isType(typeCode)) return true;
        }
        strideRetract(1);
        return false;
    }

    public void strideRetract(int stride) {
        index -= stride;
        if (index >= 0) {
            curToken = tokenList.get(index);
        }
    }

    public void keyframeRetract(int keyframe) {
        index = keyframe;
        if (index >= 0) {
            curToken = tokenList.get(index);
        }
    }

    public ParsedUnit parseCompUnit() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        for (int keyframe = index; getToken(CONSTTK, INTTK); keyframe = index) {
            if (curToken.isType(CONSTTK)) {
                keyframeRetract(keyframe);
                subUnits.add(parseConstDecl());
            } else if (!getToken(IDENFR)) {
                keyframeRetract(keyframe);
                break;
            } else if (getToken(LPARENT)) {
                keyframeRetract(keyframe);
                break;
            } else {
                keyframeRetract(keyframe);
                subUnits.add(parseVarDecl());
            }
        }
        for (int keyframe = index; getToken(INTTK, VOIDTK); keyframe = index) {
            if (curToken.isType(VOIDTK)) {
                keyframeRetract(keyframe);
                subUnits.add(parseFuncDef());
            } else if (getToken(IDENFR)) {
                keyframeRetract(keyframe);
                subUnits.add(parseFuncDef());
            } else {
                keyframeRetract(keyframe);
                break;
            }
        }
        subUnits.add(parseMainFuncDef());
        return new ParsedUnit("CompUnit", subUnits);
    }

    public ParsedUnit parseConstDecl() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(CONSTTK);
        subUnits.add(curToken);
        getToken(INTTK);
        subUnits.add(curToken);
        subUnits.add(parseConstDef());
        while (getToken(COMMA)) {
            subUnits.add(curToken);
            subUnits.add(parseConstDef());
        }
        if (getToken(SEMICN)) {
            subUnits.add(curToken);
        } else {
            ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
        }
        return new ParsedUnit("ConstDecl", subUnits);
    }

    public ParsedUnit parseConstDef() throws IOException {
        LinkedList<ParsedUnit> subUnits = parseDef();
        getToken(ASSIGN);
        subUnits.add(curToken);
        subUnits.add(parseConstInitVal());
        return new ParsedUnit("ConstDef", subUnits);
    }

    private LinkedList<ParsedUnit> parseDef() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(IDENFR);
        subUnits.add(curToken);
        while (getToken(LBRACK)) {
            subUnits.add(curToken);
            subUnits.add(parseConstExp());
            if (getToken(RBRACK)) {
                subUnits.add(curToken);
            } else {
                ErrorHandler.getInstance().addError(new Error("k", curToken.getLine()));
            }
        }
        return subUnits;
    }

    public ParsedUnit parseConstInitVal() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        if (!getToken(LBRACE)) {
            subUnits.add(parseConstExp());
        } else {
            subUnits.add(curToken);
            if (!getToken(RBRACE)) {
                subUnits.add(parseConstInitVal());
                while (getToken(COMMA)) {
                    subUnits.add(curToken);
                    subUnits.add(parseConstInitVal());
                }
                getToken(RBRACE);
            }
            subUnits.add(curToken);
        }
        return new ParsedUnit("ConstInitVal", subUnits);
    }

    public ParsedUnit parseVarDecl() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(INTTK);
        subUnits.add(curToken);
        subUnits.add(parseVarDef());
        while (getToken(COMMA)) {
            subUnits.add(curToken);
            subUnits.add(parseVarDef());
        }
        if (getToken(SEMICN)) {
            subUnits.add(curToken);
        } else {
            ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
        }
        return new ParsedUnit("VarDecl", subUnits);
    }

    public ParsedUnit parseVarDef() throws IOException {
        LinkedList<ParsedUnit> subUnits = parseDef();
        boolean flag = false;
        if (getToken(ASSIGN)) {
            subUnits.add(curToken);
            if (getToken(GETINTTK)) {
                flag = true;
                subUnits.add(curToken);
                getToken(LPARENT);
                subUnits.add(curToken);
                getToken(RPARENT);
                subUnits.add(curToken);
            } else {
                subUnits.add(parseInitVal());
            }
        }
        return new ParsedUnit(flag ? "Special" : "VarDef", subUnits);
    }

    public ParsedUnit parseInitVal() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        if (!getToken(LBRACE)) {
            subUnits.add(parseExp());
        } else {
            subUnits.add(curToken);
            if (!getToken(RBRACE)) {
                subUnits.add(parseInitVal());
                while (getToken(COMMA)) {
                    subUnits.add(curToken);
                    subUnits.add(parseInitVal());
                }
                getToken(RBRACE);
            }
            subUnits.add(curToken);
        }
        return new ParsedUnit("InitVal", subUnits);
    }

    public ParsedUnit parseFuncDef() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseFuncType());
        getToken(IDENFR);
        subUnits.add(curToken);
        getToken(LPARENT);
        subUnits.add(curToken);
        if (getToken(LBRACE)) {
            strideRetract(1);
            ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
        } else if (!getToken(RPARENT)) {
            subUnits.add(parseFuncFParams());
            if (getToken(RPARENT)) {
                subUnits.add(curToken);
            } else {
                ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
            }
        } else {
            subUnits.add(curToken);
        }
        subUnits.add(parseBlock());
        return new ParsedUnit("FuncDef", subUnits);
    }

    public ParsedUnit parseMainFuncDef() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(INTTK);
        subUnits.add(curToken);
        getToken(MAINTK);
        subUnits.add(curToken);
        getToken(LPARENT);
        subUnits.add(curToken);
        if (getToken(RPARENT)) {
            subUnits.add(curToken);
        } else {
            ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
        }
        subUnits.add(parseBlock());
        return new ParsedUnit("MainFuncDef", subUnits);
    }

    public ParsedUnit parseFuncType() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(INTTK, VOIDTK);
        subUnits.add(curToken);
        return new ParsedUnit("FuncType", subUnits);
    }

    public ParsedUnit parseFuncFParams() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseFuncFParam());
        while (getToken(COMMA)) {
            subUnits.add(curToken);
            subUnits.add(parseFuncFParam());
        }
        return new ParsedUnit("FuncFParams", subUnits);
    }

    public ParsedUnit parseFuncFParam() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(INTTK);
        subUnits.add(curToken);
        getToken(IDENFR);
        subUnits.add(curToken);
        if (getToken(LBRACK)) {
            subUnits.add(curToken);
            if (getToken(RBRACK)) {
                subUnits.add(curToken);
            } else {
                ErrorHandler.getInstance().addError(new Error("k", curToken.getLine()));
            }
            while (getToken(LBRACK)) {
                subUnits.add(curToken);
                subUnits.add(parseConstExp());
                if (getToken(RBRACK)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("k", curToken.getLine()));
                }
            }
        }
        return new ParsedUnit("FuncFParam", subUnits);
    }

    public ParsedUnit parseBlock() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(LBRACE);
        subUnits.add(curToken);
        while (!getToken(RBRACE)) {
            subUnits.add(parseBlockItem());
        }
        subUnits.add(curToken);
        return new ParsedUnit("Block", subUnits);
    }

    public ParsedUnit parseBlockItem() throws IOException {
        if (getToken(CONSTTK)) {
            strideRetract(1);
            return parseConstDecl();
        } else if (getToken(INTTK)) {
            strideRetract(1);
            return parseVarDecl();
        } else {
            return parseStmt();
        }
    }

    public ParsedUnit parseStmt() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken();
        switch (curToken.getTypeCode()) {
            case SEMICN:
                subUnits.add(curToken);
                break;
            case LBRACE:
                strideRetract(1);
                subUnits.add(parseBlock());
                break;
            case IFTK:
                subUnits.add(curToken);
                getToken(LPARENT);
                subUnits.add(curToken);
                subUnits.add(parseCond());
                if (getToken(RPARENT)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
                }
                subUnits.add(parseStmt());
                if (getToken(ELSETK)) {
                    subUnits.add(curToken);
                    subUnits.add(parseStmt());
                }
                break;
            case WHILETK:
                subUnits.add(curToken);
                getToken(LPARENT);
                subUnits.add(curToken);
                subUnits.add(parseCond());
                if (getToken(RPARENT)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
                }
                subUnits.add(parseStmt());
                break;
            case BREAKTK:
            case CONTINUETK:
                subUnits.add(curToken);
                if (getToken(SEMICN)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                }
                break;
            case RETURNTK:
                subUnits.add(curToken);
                int keyFrame = index;
                if (getToken(IDENFR) && !getToken(LPARENT)) {
                    strideRetract(1);
                    parseLVal();
                    if (getToken(ASSIGN)) {
                        keyframeRetract(keyFrame);
                        ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                        break;
                    }
                }
                keyframeRetract(keyFrame);
                if (getToken(PLUS, MINU, NOT, IDENFR, LPARENT, INTCON)) {
                    strideRetract(1);
                    subUnits.add(parseExp());
                }
                if (getToken(SEMICN)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                }
                break;
            case PRINTFTK:
                subUnits.add(curToken);
                getToken(LPARENT);
                subUnits.add(curToken);
                getToken(STRCON);
                subUnits.add(curToken);
                while (getToken(COMMA)) {
                    subUnits.add(curToken);
                    subUnits.add(parseExp());
                }
                if (getToken(RPARENT)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
                }
                if (getToken(SEMICN)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                }
                break;
            case IDENFR:
                int keyframe = index - 1;
                if (getToken(LPARENT)) {
                    keyframeRetract(keyframe);
                    subUnits.add(parseExp());
                } else {
                    parseLVal();
                    if (getToken(ASSIGN)) {
                        keyframeRetract(keyframe);
                        subUnits.add(parseLVal());
                        getToken(ASSIGN);
                        subUnits.add(curToken);
                        if (getToken(GETINTTK)) {
                            subUnits.add(curToken);
                            getToken(LPARENT);
                            subUnits.add(curToken);
                            if (getToken(RPARENT)) {
                                subUnits.add(curToken);
                            } else {
                                ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
                            }
                        } else {
                            subUnits.add(parseExp());
                        }
                    } else {
                        keyframeRetract(keyframe);
                        subUnits.add(parseExp());
                    }
                }
                if (getToken(SEMICN)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                }
                break;
            default:
                strideRetract(1);
                subUnits.add(parseExp());
                if (getToken(SEMICN)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("i", curToken.getLine()));
                }
        }
        return new ParsedUnit("Stmt", subUnits);
    }

    public ParsedUnit parseExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseAddExp());
        return new ParsedUnit("Exp", subUnits);
    }

    public ParsedUnit parseCond() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseLOrExp());
        return new ParsedUnit("Cond", subUnits);
    }

    public ParsedUnit parseLVal() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(IDENFR);
        subUnits.add(curToken);
        while (getToken(LBRACK)) {
            subUnits.add(curToken);
            subUnits.add(parseExp());
            if (getToken(RBRACK)) {
                subUnits.add(curToken);
            } else {
                ErrorHandler.getInstance().addError(new Error("k", curToken.getLine()));
            }
        }
        return new ParsedUnit("LVal", subUnits);
    }

    public ParsedUnit parsePrimaryExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        if (getToken(LPARENT)) {
            subUnits.add(curToken);
            subUnits.add(parseExp());
            getToken(RPARENT);
            subUnits.add(curToken);
        } else if (getToken(INTCON)) {
            strideRetract(1);
            subUnits.add(parseNumber());
        } else {
            subUnits.add(parseLVal());
        }
        return new ParsedUnit("PrimaryExp", subUnits);
    }

    public ParsedUnit parseNumber() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(INTCON);
        subUnits.add(curToken);
        return new ParsedUnit("Number", subUnits);
    }

    public ParsedUnit parseUnaryExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        if (getToken(PLUS, MINU, NOT)) {
            strideRetract(1);
            subUnits.add(parseUnaryOp());
            subUnits.add(parseUnaryExp());
        } else if (getToken(IDENFR)) {
            if (getToken(LPARENT)) {
                strideRetract(1);
                subUnits.add(curToken);
                getToken(LPARENT);
                subUnits.add(curToken);
                if (getToken(PLUS, MINU, NOT, IDENFR, LPARENT, INTCON)) {
                    strideRetract(1);
                    subUnits.add(parseFuncRParams());
                }
                if (getToken(RPARENT)) {
                    subUnits.add(curToken);
                } else {
                    ErrorHandler.getInstance().addError(new Error("j", curToken.getLine()));
                }
            } else {
                strideRetract(1);
                subUnits.add(parsePrimaryExp());
            }
        } else {
            subUnits.add(parsePrimaryExp());
        }
        return new ParsedUnit("UnaryExp", subUnits);
    }

    public ParsedUnit parseUnaryOp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        getToken(PLUS, MINU, NOT);
        subUnits.add(curToken);
        return new ParsedUnit("UnaryOp", subUnits);
    }

    public ParsedUnit parseFuncRParams() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseExp());
        while (getToken(COMMA)) {
            subUnits.add(curToken);
            subUnits.add(parseExp());
        }
        return new ParsedUnit("FuncRParams", subUnits);
    }

    public ParsedUnit parseMulExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseUnaryExp());
        while (getToken(MULT, DIV, MOD, BITAND)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("MulExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseUnaryExp());
        }
        return new ParsedUnit("MulExp", subUnits);
    }

    public ParsedUnit parseAddExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseMulExp());
        while (getToken(PLUS, MINU)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("AddExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseMulExp());
        }
        return new ParsedUnit("AddExp", subUnits);
    }

    public ParsedUnit parseRelExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseAddExp());
        while (getToken(LSS, GRE, LEQ, GEQ)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("RelExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseAddExp());
        }
        return new ParsedUnit("RelExp", subUnits);
    }

    public ParsedUnit parseEqExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseRelExp());
        while (getToken(EQL, NEQ)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("EqExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseRelExp());
        }
        return new ParsedUnit("EqExp", subUnits);
    }

    public ParsedUnit parseLAndExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseEqExp());
        while (getToken(AND)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("LAndExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseEqExp());
        }
        return new ParsedUnit("LAndExp", subUnits);
    }

    public ParsedUnit parseLOrExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseLAndExp());
        while (getToken(OR)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(subUnits);
            ParsedUnit preUnit = new ParsedUnit("LOrExp", preUnits);
            subUnits.clear();
            subUnits.add(preUnit);
            subUnits.add(curToken);
            subUnits.add(parseLAndExp());
        }
        return new ParsedUnit("LOrExp", subUnits);
    }

    public ParsedUnit parseConstExp() throws IOException {
        LinkedList<ParsedUnit> subUnits = new LinkedList<>();
        subUnits.add(parseAddExp());
        return new ParsedUnit("ConstExp", subUnits);
    }
}
