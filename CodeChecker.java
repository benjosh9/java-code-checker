import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeChecker {
    private final List<QualityIssue> issues = new ArrayList<>();
    private int totalLines;

    // A practical heuristic for common Java method declarations.
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^(?:(?:public|protected|private|static|final|synchronized|native|abstract|default|strictfp)\\s+)*" +
        "(?:<[^>]+>\\s*)?(?:[\\w.$<>?,\\[\\]\\s]+\\s+)?" +
        "([A-Za-z_$][\\w$]*)\\s*\\([^;]*\\)\\s*(?:throws\\s+[\\w.$,\\s]+)?\\s*\\{?\\s*$"
    );

    public void checkFile(String filePath) throws IOException {
        issues.clear();
        totalLines = 0;

        Path path = Path.of(filePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        totalLines = lines.size();

        boolean inBlockComment = false;
        boolean inMethod = false;
        boolean methodHasDocumentation = false;
        int methodStart = 0;
        int methodLineCount = 0;
        int methodBraceDepth = 0;
        String currentMethod = "";
        boolean pendingDocComment = false;

        for (int i = 0; i < lines.size(); i++) {
            String original = lines.get(i);
            String trimmed = original.trim();
            int lineNumber = i + 1;

            // Keep a code-only version for brace and rule checks.
            StringBuilder codeBuilder = new StringBuilder();
            boolean inString = false;
            boolean inChar = false;
            boolean escaped = false;
            boolean lineComment = false;

            for (int j = 0; j < original.length(); j++) {
                char c = original.charAt(j);
                char next = j + 1 < original.length() ? original.charAt(j + 1) : '\0';

                if (lineComment) {
                    break;
                }
                if (inBlockComment) {
                    if (c == '*' && next == '/') {
                        inBlockComment = false;
                        j++;
                    }
                    continue;
                }
                if (inString || inChar) {
                    codeBuilder.append(' ');
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (inString && c == '"') {
                        inString = false;
                    } else if (inChar && c == '\'') {
                        inChar = false;
                    }
                    continue;
                }
                if (c == '/' && next == '/') {
                    lineComment = true;
                    break;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    j++;
                    continue;
                }
                if (c == '"') {
                    inString = true;
                    codeBuilder.append(' ');
                } else if (c == '\'') {
                    inChar = true;
                    codeBuilder.append(' ');
                } else {
                    codeBuilder.append(c);
                }
            }

            String code = codeBuilder.toString().trim();

            // Track documentation comments for the next method declaration.
            if (trimmed.startsWith("/**")) {
                pendingDocComment = true;
            }
            if (trimmed.contains("*/") && pendingDocComment) {
                // pendingDocComment remains true until a declaration is encountered.
            }

            if (!code.isEmpty()) {
                Matcher methodMatcher = METHOD_PATTERN.matcher(code);
                boolean looksLikeMethod = !inMethod
                    && methodMatcher.matches()
                    && !code.startsWith("if ")
                    && !code.startsWith("for ")
                    && !code.startsWith("while ")
                    && !code.startsWith("switch ")
                    && !code.startsWith("catch ")
                    && !code.startsWith("try ")
                    && !code.startsWith("return ");

                if (looksLikeMethod && code.contains("(")) {
                    inMethod = true;
                    methodStart = lineNumber;
                    methodLineCount = 0;
                    methodBraceDepth = 0;
                    currentMethod = code;
                    methodHasDocumentation = pendingDocComment;
                    if (!methodHasDocumentation) {
                        issues.add(new QualityIssue(lineNumber, "Documentation",
                            "Consider adding Javadoc above method: " + currentMethod));
                    }
                    pendingDocComment = false;
                }

                if (code.contains("System.out.println")) {
                    issues.add(new QualityIssue(lineNumber, "Practice",
                        "Consider using a logging framework instead of System.out.println."));
                }

                if (Pattern.compile("\\b(int|String|double|float|long|boolean|char)\\s+[a-zA-Z_$][\\w$]*\\s*[=;,]")
                        .matcher(code).find()) {
                    Matcher variableMatcher = Pattern.compile(
                        "\\b(int|String|double|float|long|boolean|char)\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,]"
                    ).matcher(code);
                    while (variableMatcher.find()) {
                        String variableName = variableMatcher.group(2);
                        if (variableName.length() == 1 && !variableName.equals("i") &&
                            !variableName.equals("x") && !variableName.equals("y")) {
                            issues.add(new QualityIssue(lineNumber, "Naming",
                                "Single-letter variable '" + variableName + "' may be unclear; consider a descriptive name."));
                        }
                    }
                }

                if (original.length() > 120) {
                    issues.add(new QualityIssue(lineNumber, "Style",
                        "Line is " + original.length() + " characters long (recommended maximum: 120)."));
                }

                if (Pattern.compile("==\\s*true\\b|==\\s*false\\b").matcher(code).find()) {
                    issues.add(new QualityIssue(lineNumber, "Redundant",
                        "Avoid comparing a boolean with true/false; use the boolean expression directly."));
                }
            }

            // Count method lines using brace depth, ignoring braces inside strings/comments.
            if (inMethod) {
                methodLineCount++;
                for (int j = 0; j < code.length(); j++) {
                    char c = code.charAt(j);
                    if (c == '{') {
                        methodBraceDepth++;
                    } else if (c == '}') {
                        methodBraceDepth--;
                    }
                }

                if (methodBraceDepth <= 0 && code.contains("}")) {
                    if (methodLineCount > 30) {
                        issues.add(new QualityIssue(methodStart, "Method Length",
                            "Method is " + methodLineCount + " lines long (recommended maximum: 30): " + currentMethod));
                    }
                    inMethod = false;
                    methodLineCount = 0;
                    methodBraceDepth = 0;
                }
            }
        }

        if (totalLines > 300) {
            issues.add(new QualityIssue(1, "File Size",
                "File contains " + totalLines + " lines (recommended maximum: 300)."));
        }

        System.out.println("\n============== ISSUES ==============");
        if (issues.isEmpty()) {
            System.out.println("No issues detected by the current checks.");
        } else {
            for (QualityIssue issue : issues) {
                System.out.println(issue);
            }
        }
    }

    public void printSummary() {
        System.out.println("\n============== SUMMARY ==============");
        System.out.println("Total lines: " + totalLines);
        System.out.println("Issues found: " + issues.size());

        if (issues.isEmpty()) {
            System.out.println("Status: PASSED - No issues detected by these basic checks.");
        } else {
            System.out.println("Status: REVIEW RECOMMENDED");
            Map<String, Long> byType = new HashMap<>();
            for (QualityIssue issue : issues) {
                byType.put(issue.type, byType.getOrDefault(issue.type, 0L) + 1);
            }
            System.out.println("Breakdown by type: " + byType);
        }
        System.out.println("=====================================");
    }
}
