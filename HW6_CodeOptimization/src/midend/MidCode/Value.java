package midend.MidCode;

import java.util.LinkedList;
import java.util.Objects;

public abstract class Value {
    public static int tempCount = 0;
    protected final String ident;

    public Value() {
        this.ident = String.valueOf(tempCount++);
    }

    public Value(String ident) {
        this.ident = ident;
    }

    public abstract String getIdent();

    public boolean isGlobal() {
        return toString().endsWith("@0");
    }

    public boolean isReturn() {
        return toString().equals("$?");
    }

    public boolean isRunTimeInvariant(MidCode defMidCode, MidCode midCode) {
        if (defMidCode instanceof Load) {
            return false;
        }
        LinkedList<Value> useValList = new LinkedList<>();
        if (defMidCode instanceof UseVal) {
            useValList.addAll(((UseVal) defMidCode).getUseVal());
        }
        MidCode curCode = defMidCode.getNext();
        while (curCode != midCode) {
            if (curCode instanceof DefVal && useValList.contains(((DefVal) curCode).getDefVal())) {
                return false;
            }
            curCode = curCode.getNext();
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value = (Value) o;

        return Objects.equals(ident, value.ident);
    }

    @Override
    public int hashCode() {
        return ident != null ? ident.hashCode() : 0;
    }
}
