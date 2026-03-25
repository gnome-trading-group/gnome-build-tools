package com.gnometrading.spotless;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Moves the closing parenthesis of a multi-line method or constructor
 * declaration onto its own line, aligned with the declaration's indentation.
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
public class ClosingParenNewLineStep {

    private static final Set<String> CONTROL_FLOW_KEYWORDS =
            Set.of("if", "else", "while", "for", "switch", "catch", "try");

    // Keywords that can precede a ( but are NOT method/constructor declarations
    private static final Set<String> NON_DECLARATION_KEYWORDS =
            Set.of("return", "throw", "new");

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
                                boolean endsWithBody = afterParen.startsWith("{") || afterParen.startsWith("throws");
                                // Also handle abstract/interface methods that end with ;
                                boolean endsWithSemi = afterParen.startsWith(";");
                                if (endsWithBody || endsWithSemi) {
                                    String openLineText = lines[openLine];
                                    String beforeOpenParen = openLineText.substring(0, openCol).trim();
                                    // For ; (abstract methods), require >= 2 tokens (returnType + name)
                                    // to avoid matching standalone method calls like someMethod(\n    arg);
                                    int minTokens = endsWithSemi ? 2 : 1;
                                    if (looksLikeDeclaration(beforeOpenParen, minTokens)) {
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

    /**
     * Returns true if the text before an opening {@code (} looks like a method or
     * constructor declaration rather than a method call or control-flow statement.
     *
     * <p>Heuristics: must have at least 2 tokens (return type + name), no {@code .}
     * (would be a call on an object), no {@code =} (would be an assignment), and
     * none of the tokens may be control-flow or other non-declaration keywords.
     */
    private boolean looksLikeDeclaration(String beforeOpenParen, int minTokens) {
        if (beforeOpenParen.contains(".") || beforeOpenParen.contains("=")) {
            return false;
        }
        String[] tokens = beforeOpenParen.split("\\s+");
        for (String token : tokens) {
            if (CONTROL_FLOW_KEYWORDS.contains(token) || NON_DECLARATION_KEYWORDS.contains(token)) {
                return false;
            }
        }
        return tokens.length >= minTokens;
    }

    private static int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }
}
