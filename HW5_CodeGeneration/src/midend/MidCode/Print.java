package midend.MidCode;

public class Print implements MidCode {
    private final String formatString;

    public Print(String formatString) {
        this.formatString = formatString;
        MidCodeTable.getInstance().addMidCode(this);
    }

    public String getFormatString() {
        return formatString;
    }

    @Override
    public String toString() {
        return "PRINT " + formatString;
    }
}
