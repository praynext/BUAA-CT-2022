package midend.MidCode;

import java.util.LinkedList;

public interface UseVal {
    LinkedList<Value> getUseVal();

    void replaceUseVal(Value oldVal, Value newVal);
}
