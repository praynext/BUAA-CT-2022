package midend.MidCode;

public class Nop implements MidCode {
    public Nop() {
        MidCodeTable.getInstance().addMidCode(this);
    }

    @Override
    public String toString() {
        return "NOP";
    }
}
