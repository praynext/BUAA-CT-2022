package midend.MidCode;

public class Print extends MidCode {
    private final String formatString;

    public Print(String formatString) {
        this.formatString = formatString;
    }

    public String getFormatString() {
        return formatString;
    }

    @Override
    public String toString() {
        return "PRINT " + formatString;
    }
}
