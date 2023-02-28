package ErrorHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

public class ErrorHandler {
    private static ErrorHandler ERROR_HANDLER = new ErrorHandler();
    private static final ErrorHandler STDERR = ERROR_HANDLER;
    private final LinkedList<Error> errorTable = new LinkedList<>();

    public ErrorHandler() {
    }

    public static ErrorHandler getInstance() {
        return ERROR_HANDLER;
    }

    public void change() {
        ERROR_HANDLER = new ErrorHandler();
    }

    public void back() {
        ERROR_HANDLER = STDERR;
    }

    public void addError(Error error) {
        errorTable.add(error);
    }

    public void log(BufferedWriter stderr) throws IOException {
        errorTable.sort(Comparator.comparingInt((Error o) -> o.getLine()));
        for (Error error : errorTable) {
            stderr.write(error.getLine() + " " + error.getErrorCode() + "\n");
        }
        stderr.close();
    }
}
