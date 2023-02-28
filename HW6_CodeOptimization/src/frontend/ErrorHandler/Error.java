package frontend.ErrorHandler;

public class Error {
    private final String errorCode;
    private final int line;

    public Error(String errorCode, int line) {
        this.errorCode = errorCode;
        this.line = line;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getLine() {
        return line;
    }
}
