package midend.MidCode;

public class Exit implements MidCode {
    public Exit() {
        MidCodeTable.getInstance().addMidCode(this);
    }

    @Override
    public String toString() {
        return "EXIT";
    }
}
