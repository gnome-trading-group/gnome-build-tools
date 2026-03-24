package com.gnometrading.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle rule that enforces the closing parenthesis of a multi-line
 * method or constructor parameter list to be on its own line.
 *
 * <p>Flags violations like:
 * <pre>
 * public Foo(
 *         int a,
 *         int b) {   // violation: ) must be on its own line
 * </pre>
 *
 * <p>Expects:
 * <pre>
 * public Foo(
 *         int a,
 *         int b
 * ) {
 * </pre>
 */
public class ClosingParenOnNewLineCheck extends AbstractCheck {

    static final String MSG_KEY =
            "Closing parenthesis of multi-line parameter list must be on its own line.";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF};
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST lparen = ast.findFirstToken(TokenTypes.LPAREN);
        DetailAST rparen = ast.findFirstToken(TokenTypes.RPAREN);
        DetailAST parameters = ast.findFirstToken(TokenTypes.PARAMETERS);

        if (lparen == null || rparen == null || parameters == null) {
            return;
        }

        // Only applies to multi-line parameter lists
        if (lparen.getLineNo() == rparen.getLineNo()) {
            return;
        }

        // Find the last PARAMETER_DEF in the parameter list
        DetailAST lastParam = null;
        DetailAST child = parameters.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.PARAMETER_DEF) {
                lastParam = child;
            }
            child = child.getNextSibling();
        }

        if (lastParam == null) {
            return;
        }

        // RPAREN must be on a different line than the last token of the last parameter
        int lastParamLine = getLastLine(lastParam);
        if (rparen.getLineNo() == lastParamLine) {
            log(rparen, MSG_KEY);
        }
    }

    private static int getLastLine(DetailAST ast) {
        int maxLine = ast.getLineNo();
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            maxLine = Math.max(maxLine, getLastLine(child));
            child = child.getNextSibling();
        }
        return maxLine;
    }
}
