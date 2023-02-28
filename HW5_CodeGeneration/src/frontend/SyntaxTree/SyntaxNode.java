package frontend.SyntaxTree;

import midend.MidCode.Value;

public interface SyntaxNode {
    SyntaxNode simplify();
    Value generateMidCode();
}
