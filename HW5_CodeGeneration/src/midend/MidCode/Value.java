package midend.MidCode;

public abstract class Value {
    public static int tempCount = 0;

    public abstract String getIdent();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            return toString().equals(obj.toString());
        } else {
            return false;
        }
    }
    @Override public int hashCode()
    {
        return toString().hashCode();
    }
}
