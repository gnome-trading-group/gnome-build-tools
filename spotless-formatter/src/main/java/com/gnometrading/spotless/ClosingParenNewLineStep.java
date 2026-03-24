package com.gnometrading.spotless;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Spotless FormatterStep that moves the closing parenthesis of a multi-line
 * method or constructor declaration onto its own line, aligned with the
 * declaration's indentation.
 *
 * <p>Palantir Java Format places the {@code )} on the same line as the last
 * parameter. This step post-processes that output to produce:
 *
 * <pre>
 * public Foo(
 *         int a,
 *         int b
 * ) {
 * </pre>
 *
 * <p>Only applies when the {@code )} is followed by {@code {} or {@code throws},
 * which distinguishes method/constructor declarations from control-flow
 * constructs and method calls.
 */
public class ClosingParenNewLineStep implements FormatterFunc {

    private static final String NAME = "closingParenNewLine";
    private static final Set<String> CONTROL_FLOW_KEYWORDS =
            Set.of("if", "else", "while", "for", "switch", "catch", "try");

    public static FormatterStep create() {
        return FormatterStep.createNeverUpToDate(NAME, new ClosingParenNewLineStep());
    }

    @Override
    public String apply(String input) throws Exception {
        String[] lines = input.split("\n", -1);
        boolean[] needsTransform = new boolean[lines.length];
        int[] rparenColumn = new int[lines.length];
        int[] openLineIndent = new int[lines.length];

        // Stack entries: [lineIndex, indentOfLine, columnOfOpenParen]
        Deque<int[]> stack = new ArrayDeque<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int indent = countLeadingSpaces(line);

            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);

                // Skip line comments
                if (c == '/' && col + 1 < line.length() && line.charAt(col + 1) == '/') {
                    break;
                }

                // Skip string literals
                if (c == '"') {
                    col++;
                    while (col < line.length() && line.charAt(col) != '"') {
                        if (line.charAt(col) == '\\') col++;
                        col++;
                    }
                    continue;
                }

                // Skip char literals
                if (c == '\'') {
                    col++;
                    while (col < line.length() && line.charAt(col) != '\'') {
                        if (line.charAt(col) == '\\') col++;
                        col++;
                    }
                    continue;
                }

                if (c == '(') {
                    stack.push(new int[]{i, indent, col});
                } else if (c == ')') {
                    if (!stack.isEmpty()) {
                        int[] open = stack.pop();
                        int openLine = open[0];
                        int openIndent = open[1];
                        int openCol = open[2];

                        if (openLine != i) {
                            String beforeParen = line.substring(0, col).trim();
                            if (!beforeParen.isEmpty()) {
                                String afterParen = line.substring(col + 1).stripLeading();
                                if (afterParen.startsWith("{") || afterParen.startsWith("throws")) {
                                    // Only transform method/constructor declarations, not control flow
                                    String openLineText = lines[openLine];
                                    String beforeOpenParen = openLineText.substring(0, openCol).trim();
                                    String[] tokens = beforeOpenParen.split("\\s+");
                                    String lastToken = tokens.length > 0 ? tokens[tokens.length - 1] : "";
                                    if (!CONTROL_FLOW_KEYWORDS.contains(lastToken)) {
                                        needsTransform[i] = true;
                                        rparenColumn[i] = col;
                                        openLineIndent[i] = openIndent;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (needsTransform[i]) {
                int col = rparenColumn[i];
                result.append(lines[i], 0, col);
                result.append("\n");
                result.append(" ".repeat(openLineIndent[i]));
                result.append(lines[i].substring(col));
            } else {
                result.append(lines[i]);
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    private static int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }
}
