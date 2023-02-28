package frontend.ErrorHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class ErrorHandler {
    private static final ErrorHandler STDERR = new ErrorHandler();
    private final HashMap<Integer, Error> errorMap = new HashMap<>();

    public static ErrorHandler getInstance() {
        return STDERR;
    }

    public void addError(Error error) {
        errorMap.putIfAbsent(error.getLine(), error);
    }

    public void log(BufferedWriter stderr) throws IOException {
        errorMap.keySet().stream().sorted().forEach(line -> {
            try {
                stderr.write(line + " " + errorMap.get(line).getErrorCode() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        stderr.close();
    }
}
