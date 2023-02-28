package Parser;

import ErrorHandler.Error;
import ErrorHandler.ErrorHandler;
import Lexer.*;
import SymbolTable.SymbolTable;
import SyntaxTree.*;

import static Lexer.TypeCode.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class Parser {
    private final Lexer lexer;
    private int index = -1;
    private int loopDepth = 0;
    private Token curToken;
    private final LinkedList<Token> tokenList = new LinkedList<>();
    private SymbolTable curSymbolTable = new SymbolTable(null);
    private BufferedWriter output;
    private final BufferedWriter stdout;
    private final ErrorHandler errorHandler = ErrorHandler.getInstance();

    public Parser(Lexer lexer, BufferedWriter stdout) {
        this.lexer = lexer;
        this.output = stdout;
        this.stdout = stdout;
    }

    public void getToken() throws IOException {
        index++;
        if (tokenList.size() == index) {
            tokenList.add(lexer.getToken());
        }
        curToken = tokenList.get(index);
    }

    public boolean getToken(TypeCode typeCode) throws IOException {
        index++;
        if (tokenList.size() == index) {
            tokenList.add(lexer.getToken());
        }
        if (tokenList.get(index).isType(typeCode)) {
            curToken = tokenList.get(index);
            return true;
        } else {
            index--;
            return false;
        }
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

    public CompUnit parseCompUnit() throws IOException {
        CompUnit compUnit = new CompUnit();
        getToken();
        while (curToken.isType(CONSTTK) || curToken.isType(INTTK)) {
            if (curToken.isType(CONSTTK)) {
                strideRetract(1);
                compUnit.addDeclNode(parseConstDecl());
            } else if (!getToken(IDENFR)) {
                break;
            } else if (getToken(LPARENT)) {
                strideRetract(2);
                break;
            } else {
                strideRetract(2);
                compUnit.addDeclNode(parseVarDecl());
            }
            getToken();
        }
        while (curToken.isType(INTTK) || curToken.isType(VOIDTK)) {
            if (curToken.isType(VOIDTK)) {
                strideRetract(1);
                compUnit.addFuncDefNode(parseFuncDef());
            } else if (getToken(IDENFR)) {
                strideRetract(2);
                compUnit.addFuncDefNode(parseFuncDef());
            } else {
                break;
            }
            getToken();
        }
        strideRetract(1);
        compUnit.setMainFuncDefNode(parseMainFuncDef());
        output.write("<CompUnit>\n");
        output.close();
        return compUnit;
    }

    public DeclNode parseConstDecl() throws IOException {
        getToken(CONSTTK);
        DeclNode declNode = new DeclNode(curToken);
        output.write(curToken.toString());
        getToken(INTTK);
        output.write(curToken.toString());
        declNode.addDefNode(parseConstDef());
        while (getToken(COMMA)) {
            output.write(curToken.toString());
            declNode.addDefNode(parseConstDef());
        }
        if (getToken(SEMICN)) {
            output.write(curToken.toString());
        } else {
            errorHandler.addError(new Error("i", curToken.getLine()));
        }
        output.write("<ConstDecl>\n");
        return declNode;
    }

    public DefNode parseConstDef() throws IOException {
        getToken(IDENFR);
        DefNode defNode = new DefNode(curToken);
        defNode.setDeclType(CONSTTK);
        output.write(curToken.toString());
        while (getToken(LBRACK)) {
            output.write(curToken.toString());
            defNode.addDimension(parseConstExp());
            if (getToken(RBRACK)) {
                output.write(curToken.toString());
            } else {
                errorHandler.addError(new Error("k", curToken.getLine()));
            }
        }
        getToken(ASSIGN);
        output.write(curToken.toString());
        defNode.setInitValues(parseConstInitVal());
        curSymbolTable.addVariable(defNode);
        output.write("<ConstDef>\n");
        return defNode;
    }

    public LinkedList<ExpNode> parseConstInitVal() throws IOException {
        LinkedList<ExpNode> initValues = new LinkedList<>();
        if (!getToken(LBRACE)) {
            initValues.add(parseConstExp());
        } else {
            output.write(curToken.toString());
            if (!getToken(RBRACE)) {
                initValues.addAll(parseConstInitVal());
                while (getToken(COMMA)) {
                    output.write(curToken.toString());
                    initValues.addAll(parseConstInitVal());
                }
                getToken(RBRACE);
            }
            output.write(curToken.toString());
        }
        output.write("<ConstInitVal>\n");
        return initValues;
    }

    public DeclNode parseVarDecl() throws IOException {
        getToken(INTTK);
        DeclNode declNode = new DeclNode(curToken);
        output.write(curToken.toString());
        declNode.addDefNode(parseVarDef());
        while (getToken(COMMA)) {
            output.write(curToken.toString());
            declNode.addDefNode(parseVarDef());
        }
        if (getToken(SEMICN)) {
            output.write(curToken.toString());
        } else {
            errorHandler.addError(new Error("i", curToken.getLine()));
        }
        output.write("<VarDecl>\n");
        return declNode;
    }

    public DefNode parseVarDef() throws IOException {
        getToken(IDENFR);
        DefNode defNode = new DefNode(curToken);
        defNode.setDeclType(INTTK);
        output.write(curToken.toString());
        while (getToken(LBRACK)) {
            output.write(curToken.toString());
            defNode.addDimension(parseConstExp());
            if (getToken(RBRACK)) {
                output.write(curToken.toString());
            } else {
                errorHandler.addError(new Error("k", curToken.getLine()));
            }
        }
        if (getToken(ASSIGN)) {
            output.write(curToken.toString());
            defNode.setInitValues(parseInitVal());
        } else {
            defNode.setInitValues(0);
        }
        curSymbolTable.addVariable(defNode);
        output.write("<VarDef>\n");
        return defNode;
    }

    public LinkedList<ExpNode> parseInitVal() throws IOException {
        LinkedList<ExpNode> initValues = new LinkedList<>();
        if (!getToken(LBRACE)) {
            initValues.add(parseExp());
        } else {
            output.write(curToken.toString());
            if (!getToken(RBRACE)) {
                initValues.addAll(parseInitVal());
                while (getToken(COMMA)) {
                    output.write(curToken.toString());
                    initValues.addAll(parseInitVal());
                }
                getToken(RBRACE);
            }
            output.write(curToken.toString());
        }
        output.write("<InitVal>\n");
        return initValues;
    }

    public FuncDefNode parseFuncDef() throws IOException {
        FuncDefNode funcDefNode = new FuncDefNode(parseFuncType());
        getToken(IDENFR);
        funcDefNode.setIdent(curToken);
        output.write(curToken.toString());
        curSymbolTable.addFunction(funcDefNode);
        curSymbolTable = new SymbolTable(curSymbolTable);
        getToken(LPARENT);
        output.write(curToken.toString());
        if (getToken(LBRACE)) {
            strideRetract(1);
            errorHandler.addError(new Error("j", curToken.getLine()));
        } else if (!getToken(RPARENT)) {
            funcDefNode.setFuncFParams(parseFuncFParams());
            if (getToken(RPARENT)) {
                output.write(curToken.toString());
            } else {
                errorHandler.addError(new Error("j", curToken.getLine()));
            }
        } else {
            output.write(curToken.toString());
        }
        funcDefNode.setBlockNode(parseBlock());
        curSymbolTable = curSymbolTable.getParent();
        funcDefNode.check();
        output.write("<FuncDef>\n");
        return funcDefNode;
    }

    public FuncDefNode parseMainFuncDef() throws IOException {
        getToken(INTTK);
        FuncDefNode mainFuncDefNode = new FuncDefNode(curToken);
        output.write(curToken.toString());
        getToken(MAINTK);
        output.write(curToken.toString());
        getToken(LPARENT);
        output.write(curToken.toString());
        if (getToken(RPARENT)) {
            output.write(curToken.toString());
        } else {
            errorHandler.addError(new Error("j", curToken.getLine()));
        }
        curSymbolTable = new SymbolTable(curSymbolTable);
        mainFuncDefNode.setBlockNode(parseBlock());
        curSymbolTable = curSymbolTable.getParent();
        mainFuncDefNode.check();
        output.write("<MainFuncDef>\n");
        return mainFuncDefNode;
    }

    public Token parseFuncType() throws IOException {
        getToken();
        output.write(curToken.toString());
        output.write("<FuncType>\n");
        return curToken;
    }

    public LinkedList<FuncFParamNode> parseFuncFParams() throws IOException {
        LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
        funcFParamNodes.add(parseFuncFParam());
        while (getToken(COMMA)) {
            output.write(curToken.toString());
            funcFParamNodes.add(parseFuncFParam());
        }
        output.write("<FuncFParams>\n");
        return funcFParamNodes;
    }

    public FuncFParamNode parseFuncFParam() throws IOException {
        getToken(INTTK);
        output.write(curToken.toString());
        getToken(IDENFR);
        FuncFParamNode funcFParamNode = new FuncFParamNode(curToken);
        output.write(curToken.toString());
        if (getToken(LBRACK)) {
            output.write(curToken.toString());
            funcFParamNode.addDimension(new NumberNode(0));
            if (getToken(RBRACK)) {
                output.write(curToken.toString());
            } else {
                errorHandler.addError(new Error("k", curToken.getLine()));
            }
            while (getToken(LBRACK)) {
                output.write(curToken.toString());
                funcFParamNode.addDimension(parseConstExp());
                if (getToken(RBRACK)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("k", curToken.getLine()));
                }
            }
        }
        curSymbolTable.addVariable(funcFParamNode);
        output.write("<FuncFParam>\n");
        return funcFParamNode;
    }

    public BlockNode parseBlock() throws IOException {
        BlockNode blockNode = new BlockNode();
        getToken(LBRACE);
        output.write(curToken.toString());
        while (!getToken(RBRACE)) {
            blockNode.addBlockItemNode(parseBlockItem());
        }
        blockNode.setEndLine(curToken.getLine());
        output.write(curToken.toString());
        output.write("<Block>\n");
        return blockNode;
    }

    public BlockItemNode parseBlockItem() throws IOException {
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

    public StmtNode parseStmt() throws IOException {
        StmtNode stmtNode = new NopNode();
        getToken();
        switch (curToken.getTypeCode()) {
            case SEMICN:
                output.write(curToken.toString());
                break;
            case LBRACE:
                strideRetract(1);
                curSymbolTable = new SymbolTable(curSymbolTable);
                stmtNode = parseBlock();
                curSymbolTable = curSymbolTable.getParent();
                break;
            case IFTK:
                stmtNode = new BranchNode();
                output.write(curToken.toString());
                getToken(LPARENT);
                output.write(curToken.toString());
                ((BranchNode) stmtNode).setCond(parseCond());
                if (getToken(RPARENT)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("j", curToken.getLine()));
                }
                ((BranchNode) stmtNode).setThenStmt(parseStmt());
                if (getToken(ELSETK)) {
                    output.write(curToken.toString());
                    ((BranchNode) stmtNode).setElseStmt(parseStmt());
                }
                break;
            case WHILETK:
                stmtNode = new LoopNode();
                output.write(curToken.toString());
                getToken(LPARENT);
                output.write(curToken.toString());
                ((LoopNode) stmtNode).setCond(parseCond());
                if (getToken(RPARENT)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("j", curToken.getLine()));
                }
                loopDepth++;
                ((LoopNode) stmtNode).setLoopStmt(parseStmt());
                loopDepth--;
                break;
            case BREAKTK:
                stmtNode = new BreakNode();
                output.write(curToken.toString());
                if (loopDepth == 0) {
                    errorHandler.addError(new Error("m", curToken.getLine()));
                }
                if (getToken(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
                break;
            case CONTINUETK:
                stmtNode = new ContinueNode();
                output.write(curToken.toString());
                if (loopDepth == 0) {
                    errorHandler.addError(new Error("m", curToken.getLine()));
                }
                if (getToken(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
                break;
            case RETURNTK:
                stmtNode = new ReturnNode(curToken);
                output.write(curToken.toString());
                getToken();
                if (curToken.isType(PLUS) || curToken.isType(MINU) || curToken.isType(NOT) ||
                        curToken.isType(IDENFR) || curToken.isType(LPARENT) || curToken.isType(INTCON)) {
                    strideRetract(1);
                    ((ReturnNode) stmtNode).setReturnValue(parseExp());
                    getToken();
                }
                if (curToken.isType(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    strideRetract(1);
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
                break;
            case PRINTFTK:
                stmtNode = new PrintNode();
                output.write(curToken.toString());
                getToken(LPARENT);
                output.write(curToken.toString());
                getToken(STRCON);
                ((PrintNode) stmtNode).setFormatString(curToken);
                output.write(curToken.toString());
                while (getToken(COMMA)) {
                    output.write(curToken.toString());
                    ((PrintNode) stmtNode).addArg(parseExp());
                }
                if (getToken(RPARENT)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("j", curToken.getLine()));
                }
                if (getToken(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
                ((PrintNode) stmtNode).check();
                break;
            case IDENFR:
                int keyframe = index - 1;
                if (getToken(LPARENT)) {
                    keyframeRetract(keyframe);
                    parseExp();
                } else {
                    output = new BufferedWriter(new FileWriter("trash_bin"));
                    errorHandler.change();
                    parseLVal();
                    errorHandler.back();
                    output = stdout;
                    if (getToken(ASSIGN)) {
                        keyframeRetract(keyframe);
                        LValNode lValNode = parseLVal();
                        getToken(ASSIGN);
                        output.write(curToken.toString());
                        if (getToken(GETINTTK)) {
                            stmtNode = new GetIntNode(lValNode);
                            output.write(curToken.toString());
                            getToken(LPARENT);
                            output.write(curToken.toString());
                            if (getToken(RPARENT)) {
                                output.write(curToken.toString());
                            } else {
                                errorHandler.addError(new Error("j", curToken.getLine()));
                            }
                            ((GetIntNode) stmtNode).check(curSymbolTable);
                        } else {
                            stmtNode = new AssignNode();
                            ((AssignNode) stmtNode).setLValNode(lValNode);
                            ((AssignNode) stmtNode).setExpNode(parseExp());
                            ((AssignNode) stmtNode).check(curSymbolTable);
                        }
                    } else {
                        keyframeRetract(keyframe);
                        stmtNode = parseExp();
                    }
                }
                if (getToken(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
                break;
            default:
                strideRetract(1);
                stmtNode = parseExp();
                if (getToken(SEMICN)) {
                    output.write(curToken.toString());
                } else {
                    errorHandler.addError(new Error("i", curToken.getLine()));
                }
        }
        output.write("<Stmt>\n");
        return stmtNode;
    }

    public ExpNode parseExp() throws IOException {
        ExpNode expNode = parseAddExp();
        output.write("<Exp>\n");
        return expNode;
    }

    public ExpNode parseCond() throws IOException {
        ExpNode expNode = parseLOrExp();
        output.write("<Cond>\n");
        return expNode;
    }

    public LValNode parseLVal() throws IOException {
        LValNode lValNode = new LValNode();
        getToken(IDENFR);
        lValNode.setIdent(curToken);
        output.write(curToken.toString());
        while (getToken(LBRACK)) {
            output.write(curToken.toString());
            lValNode.addDimension(parseExp());
            if (getToken(RBRACK)) {
                output.write(curToken.toString());
            } else {
                errorHandler.addError(new Error("k", curToken.getLine()));
            }
        }
        lValNode.check(curSymbolTable);
        output.write("<LVal>\n");
        return lValNode;
    }

    public ExpNode parsePrimaryExp() throws IOException {
        ExpNode expNode;
        if (getToken(LPARENT)) {
            output.write(curToken.toString());
            expNode = parseExp();
            getToken(RPARENT);
            output.write(curToken.toString());
        } else if (getToken(INTCON)) {
            expNode = new NumberNode(curToken.getIntValue());
            output.write(curToken.toString());
            output.write("<Number>\n");
        } else {
            expNode = parseLVal();
        }
        output.write("<PrimaryExp>\n");
        return expNode;
    }

    public ExpNode parseUnaryExp() throws IOException {
        ExpNode expNode;
        getToken();
        if (curToken.isType(PLUS) || curToken.isType(MINU) || curToken.isType(NOT)) {
            expNode = new UnaryExpNode(curToken);
            output.write(curToken.toString());
            output.write("<UnaryOp>\n");
            ((UnaryExpNode) expNode).setExpNode(parseUnaryExp());
        } else if (curToken.isType(IDENFR)) {
            if (getToken(LPARENT)) {
                strideRetract(1);
                expNode = new FuncCallNode(curToken);
                output.write(curToken.toString());
                getToken(LPARENT);
                output.write(curToken.toString());
                getToken();
                if (curToken.isType(PLUS) || curToken.isType(MINU) || curToken.isType(NOT) ||
                        curToken.isType(IDENFR) || curToken.isType(LPARENT) || curToken.isType(INTCON)) {
                    strideRetract(1);
                    ((FuncCallNode) expNode).setArgs(parseFuncRParams());
                    getToken();
                }
                if (curToken.isType(RPARENT)) {
                    output.write(curToken.toString());
                } else {
                    strideRetract(1);
                    errorHandler.addError(new Error("j", curToken.getLine()));
                }
                ((FuncCallNode) expNode).check(curSymbolTable);
            } else {
                strideRetract(1);
                expNode = parsePrimaryExp();
            }
        } else {
            strideRetract(1);
            expNode = parsePrimaryExp();
        }
        output.write("<UnaryExp>\n");
        return expNode;
    }

    public LinkedList<ExpNode> parseFuncRParams() throws IOException {
        LinkedList<ExpNode> args = new LinkedList<>();
        args.add(parseExp());
        while (getToken(COMMA)) {
            output.write(curToken.toString());
            args.add(parseExp());
        }
        output.write("<FuncRParams>\n");
        return args;
    }

    public ExpNode parseMulExp() throws IOException {
        ExpNode expNode = parseUnaryExp();
        output.write("<MulExp>\n");
        getToken();
        while (curToken.isType(MULT) || curToken.isType(DIV) || curToken.isType(MOD)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseUnaryExp());
            expNode = binaryExpNode;
            output.write("<MulExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseAddExp() throws IOException {
        ExpNode expNode = parseMulExp();
        output.write("<AddExp>\n");
        getToken();
        while (curToken.isType(PLUS) || curToken.isType(MINU)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseMulExp());
            expNode = binaryExpNode;
            output.write("<AddExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseRelExp() throws IOException {
        ExpNode expNode = parseAddExp();
        output.write("<RelExp>\n");
        getToken();
        while (curToken.isType(LSS) || curToken.isType(GRE) ||
                curToken.isType(LEQ) || curToken.isType(GEQ)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseAddExp());
            expNode = binaryExpNode;
            output.write("<RelExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseEqExp() throws IOException {
        ExpNode expNode = parseRelExp();
        output.write("<EqExp>\n");
        getToken();
        while (curToken.isType(EQL) || curToken.isType(NEQ)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseRelExp());
            expNode = binaryExpNode;
            output.write("<EqExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseLAndExp() throws IOException {
        ExpNode expNode = parseEqExp();
        output.write("<LAndExp>\n");
        getToken();
        while (curToken.isType(AND)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseEqExp());
            expNode = binaryExpNode;
            output.write("<LAndExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseLOrExp() throws IOException {
        ExpNode expNode = parseLAndExp();
        output.write("<LOrExp>\n");
        getToken();
        while (curToken.isType(OR)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(curToken);
            output.write(curToken.toString());
            binaryExpNode.setRightExp(parseLAndExp());
            expNode = binaryExpNode;
            output.write("<LOrExp>\n");
            getToken();
        }
        strideRetract(1);
        return expNode;
    }

    public ExpNode parseConstExp() throws IOException {
        ExpNode expNode = parseAddExp();
        output.write("<ConstExp>\n");
        return expNode;
    }
}
