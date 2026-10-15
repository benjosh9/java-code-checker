public class QualityIssue {
    public final int line;
    public final String type;
    public final String message;

    public QualityIssue(int line, String type, String message) {
        this.line = line;
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[Line " + line + "] [" + type + "] " + message;
    }
}
