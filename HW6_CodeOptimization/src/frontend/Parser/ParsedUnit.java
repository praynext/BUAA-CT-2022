package frontend.Parser;

import frontend.ErrorHandler.Error;
import frontend.ErrorHandler.ErrorHandler;
import frontend.Lexer.Token;
import frontend.SymbolTable.SymbolTable;
import frontend.SyntaxTree.*;

import java.util.LinkedList;
import java.util.List;

public class ParsedUnit {
    private final String name;
    private final List<ParsedUnit> subUnits;
    private int index = -1;
    private static int loopDepth = 0;
    private ParsedUnit curUnit;
    private static SymbolTable curSymbolTable = new SymbolTable(null);

    public ParsedUnit(String name, List<ParsedUnit> subUnits) {
        this.name = name;
        this.subUnits = subUnits;
    }

    public ParsedUnit getUnit() {
        index++;
        return curUnit = subUnits.get(index);
    }

    public boolean getUnit(String... name) {
        index++;
        if (index != subUnits.size()) {
            curUnit = subUnits.get(index);
            for (String s : name) {
                if (curUnit.name.equals(s)) {
                    return true;
                }
            }
        }
        index--;
        return false;
    }

    public BlockItemNode toBlockItemNode() {
        if (name.equals("Stmt")) {
            return this.toStmtNode();
        } else {
            return this.toDeclNode();
        }
    }

    public BlockNode toBlockNode() {
        getUnit("LBRACE");
        LinkedList<BlockItemNode> blockItemNodes = new LinkedList<>();
        while (!getUnit("RBRACE")) {
            blockItemNodes.add(getUnit().toBlockItemNode());
        }
        return new BlockNode(curSymbolTable, blockItemNodes, curUnit.toToken().getLine());
    }

    public CompUnitNode toCompUnitNode() {
        LinkedList<DeclNode> declNodes = new LinkedList<>();
        LinkedList<FuncDefNode> funcDefNodes = new LinkedList<>();
        FuncDefNode mainFuncDefNode = null;
        for (ParsedUnit parsedUnit : subUnits) {
            switch (parsedUnit.name) {
                case "ConstDecl":
                case "VarDecl":
                    declNodes.add(parsedUnit.toDeclNode());
                    break;
                case "FuncDef":
                    funcDefNodes.add(parsedUnit.toFuncDefNode());
                    break;
                case "MainFuncDef":
                    mainFuncDefNode = parsedUnit.toFuncDefNode();
                    break;
            }
        }
        return new CompUnitNode(curSymbolTable, declNodes, funcDefNodes, mainFuncDefNode);
    }

    public DeclNode toDeclNode() {
        LinkedList<DefNode> defNodes = new LinkedList<>();
        for (ParsedUnit parsedUnit : subUnits) {
            if (parsedUnit.name.equals("ConstDef") || parsedUnit.name.equals("VarDef")) {
                defNodes.add(parsedUnit.toDefNode());
            }
        }
        return new DeclNode(curSymbolTable, name.equals("ConstDecl"), defNodes);
    }

    public DefNode toDefNode() {
        Token ident = getUnit().toToken();
        LinkedList<ExpNode> dimensions = new LinkedList<>();
        LinkedList<ExpNode> initValues = new LinkedList<>();
        DefNode defNode = new DefNode(curSymbolTable, name.equals("ConstDef"), ident, dimensions, initValues);
        while (getUnit("LBRACK")) {
            dimensions.add(getUnit().toExpNode());
            getUnit("RBRACK");
        }
        if (getUnit("ASSIGN")) {
            initValues.addAll(getUnit().toExpNodeList());
        }
        curSymbolTable.addVariable(defNode);
        return defNode;
    }

    public ExpNode toExpNode() {
        switch (name) {
            case "Cond":
            case "ConstExp":
            case "Exp":
                return getUnit().toExpNode();
            case "PrimaryExp":
                if (getUnit("LVal")) {
                    return curUnit.toLValNode();
                } else if (getUnit("Number")) {
                    return curUnit.toNumberNode();
                } else {
                    getUnit("LPARENT");
                    return getUnit().toExpNode();
                }
            case "UnaryExp":
                if (getUnit("PrimaryExp")) {
                    return curUnit.toExpNode();
                } else if (getUnit("IDENFR")) {
                    Token ident = curUnit.toToken();
                    getUnit("LPARENT");
                    LinkedList<ExpNode> args = new LinkedList<>();
                    FuncCallNode funcCallNode = new FuncCallNode(curSymbolTable, ident, args);
                    if (getUnit("FuncRParams")) {
                        args.addAll(curUnit.toExpNodeList());
                    }
                    funcCallNode.check();
                    return funcCallNode;
                } else {
                    return new UnaryExpNode(curSymbolTable, getUnit().toToken(), getUnit().toExpNode());
                }
            default:
                ExpNode leftExp = getUnit().toExpNode();
                if (getUnit("PLUS", "MINU", "MULT", "DIV", "MOD", "LSS", "LEQ", "GRE", "GEQ", "EQL", "NEQ", "AND", "OR")) {
                    return new BinaryExpNode(curSymbolTable, curUnit.toToken(), leftExp, getUnit().toExpNode());
                } else {
                    return leftExp;
                }
        }
    }

    public LinkedList<ExpNode> toExpNodeList() {
        LinkedList<ExpNode> expNodes = new LinkedList<>();
        if (name.equals("ConstInitVal")) {
            subUnits.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("ConstExp")) {
                    expNodes.add(parsedUnit.toExpNode());
                } else if (parsedUnit.name.equals("ConstInitVal")) {
                    expNodes.addAll(parsedUnit.toExpNodeList());
                }
            });
        } else if (name.equals("InitVal")) {
            subUnits.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("Exp")) {
                    expNodes.add(parsedUnit.toExpNode());
                } else if (parsedUnit.name.equals("InitVal")) {
                    expNodes.addAll(parsedUnit.toExpNodeList());
                }
            });
        } else {
            subUnits.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("Exp")) {
                    expNodes.add(parsedUnit.toExpNode());
                }
            });
        }
        return expNodes;
    }

    public FuncDefNode toFuncDefNode() {
        Token funcDefType = getUnit().toToken();
        Token ident = getUnit().toToken();
        LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
        FuncDefNode funcDefNode = new FuncDefNode(curSymbolTable, funcDefType, ident, funcFParamNodes);
        curSymbolTable.addFunction(funcDefNode);
        curSymbolTable = new SymbolTable(curSymbolTable);
        getUnit("LPARENT");
        if (getUnit("FuncFParams")) {
            curUnit.subUnits.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("FuncFParam")) {
                    funcFParamNodes.add(parsedUnit.toFuncFParamNode());
                }
            });
        }
        getUnit("RPARENT");
        funcDefNode.setBlockNode(getUnit().toBlockNode());
        curSymbolTable = curSymbolTable.getParent();
        funcDefNode.check();
        return funcDefNode;
    }

    public FuncFParamNode toFuncFParamNode() {
        getUnit("INTTK");
        Token ident = getUnit().toToken();
        LinkedList<ExpNode> dimensions = new LinkedList<>();
        FuncFParamNode funcFParamNode = new FuncFParamNode(curSymbolTable, ident, dimensions);
        if (getUnit("LBRACK")) {
            dimensions.add(new NumberNode(0));
            getUnit("RBRACK");
            while (getUnit("LBRACK")) {
                dimensions.add(getUnit().toExpNode());
                getUnit("RBRACK");
            }
        }
        curSymbolTable.addVariable(funcFParamNode);
        return funcFParamNode;
    }

    public LValNode toLValNode() {
        Token ident = getUnit().toToken();
        LinkedList<ExpNode> dimensions = new LinkedList<>();
        LValNode lValNode = new LValNode(curSymbolTable, ident, dimensions);
        while (getUnit("LBRACK")) {
            dimensions.add(getUnit().toExpNode());
            getUnit("RBRACK");
        }
        lValNode.checkC();
        return lValNode;
    }

    private NumberNode toNumberNode() {
        return new NumberNode(getUnit().toToken().getIntValue());
    }

    public StmtNode toStmtNode() {
        switch (getUnit().name) {
            case "LVal":
                LValNode lValNode = curUnit.toLValNode();
                lValNode.checkH();
                getUnit("ASSIGN");
                if (getUnit("Exp")) {
                    return new AssignNode(curSymbolTable, lValNode, curUnit.toExpNode());
                } else {
                    return new GetIntNode(curSymbolTable, lValNode);
                }
            case "Exp":
                return curUnit.toExpNode();
            case "Block":
                curSymbolTable = new SymbolTable(curSymbolTable);
                BlockNode blockNode = curUnit.toBlockNode();
                curSymbolTable = curSymbolTable.getParent();
                return blockNode;
            case "IFTK":
                getUnit("LPARENT");
                ExpNode branchCond = getUnit().toExpNode();
                getUnit("RPARENT");
                StmtNode thenStmt = getUnit().toStmtNode();
                StmtNode elseStmt = null;
                if (getUnit("ELSETK")) {
                    elseStmt = getUnit().toStmtNode();
                }
                return new BranchNode(curSymbolTable, branchCond, thenStmt, elseStmt);
            case "WHILETK":
                getUnit("LPARENT");
                ExpNode loopCond = getUnit().toExpNode();
                getUnit("RPARENT");
                loopDepth++;
                StmtNode loopStmt = getUnit().toStmtNode();
                loopDepth--;
                return new LoopNode(curSymbolTable, loopCond, loopStmt);
            case "BREAKTK":
                if (loopDepth == 0) {
                    ErrorHandler.getInstance().addError(new Error("m", curUnit.toToken().getLine()));
                }
                return new BreakNode(curSymbolTable);
            case "CONTINUETK":
                if (loopDepth == 0) {
                    ErrorHandler.getInstance().addError(new Error("m", curUnit.toToken().getLine()));
                }
                return new ContinueNode(curSymbolTable);
            case "RETURNTK":
                Token returnToken = curUnit.toToken();
                if (getUnit("Exp")) {
                    return new ReturnNode(curSymbolTable, returnToken, curUnit.toExpNode());
                } else {
                    return new ReturnNode(curSymbolTable, returnToken, null);
                }
            case "PRINTFTK":
                getUnit("LPARENT");
                Token formatString = getUnit().toToken();
                LinkedList<ExpNode> args = new LinkedList<>();
                PrintNode printNode = new PrintNode(curSymbolTable, formatString, args);
                while (getUnit("COMMA")) {
                    args.add(getUnit().toExpNode());
                }
                printNode.check();
                return printNode;
            default:
                return new NopNode();
        }
    }

    public Token toToken() {
        if (name.equals("FuncType") || name.equals("UnaryOp")) return (Token) getUnit();
        else return (Token) this;
    }

    @Override
    public String toString() {
        return subUnits.stream().map(ParsedUnit::toString).reduce((s1, s2) -> s1 + "\n" + s2).orElse("")
                + "\n<" + name + ">";
    }
}
