import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String target = args.length > 0 ? args[0] : "TestSample.java";

        System.out.println("=== Java Code Quality Checker v1.1 by benjosh9 ===");
        System.out.println("Target: " + target);

        File file = new File(target);
        if (!file.isFile()) {
            System.err.println("File not found: " + target);
            System.err.println("Usage: java Main <path-to-java-file>");
            return;
        }

        CodeChecker checker = new CodeChecker();
        try {
            checker.checkFile(target);
            checker.printSummary();
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
        }
    }
}
