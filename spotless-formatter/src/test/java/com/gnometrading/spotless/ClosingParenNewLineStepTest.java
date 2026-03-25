package com.gnometrading.spotless;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClosingParenNewLineStepTest {

    private final ClosingParenNewLineStep step = new ClosingParenNewLineStep();

    @Test
    void multiLineConstructor() throws Exception {
        String input =
                "public class Foo {\n"
                        + "    public Foo(\n"
                        + "            int a,\n"
                        + "            int b) {\n"
                        + "    }\n"
                        + "}\n";

        String expected =
                "public class Foo {\n"
                        + "    public Foo(\n"
                        + "            int a,\n"
                        + "            int b\n"
                        + "    ) {\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(expected, step.apply(input));
    }

    @Test
    void multiLineMethod() throws Exception {
        String input =
                "public class Foo {\n"
                        + "    public void doSomething(\n"
                        + "            String first,\n"
                        + "            String second) {\n"
                        + "    }\n"
                        + "}\n";

        String expected =
                "public class Foo {\n"
                        + "    public void doSomething(\n"
                        + "            String first,\n"
                        + "            String second\n"
                        + "    ) {\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(expected, step.apply(input));
    }

    @Test
    void multiLineMethodWithThrows() throws Exception {
        String input =
                "public class Foo {\n"
                        + "    public void doSomething(\n"
                        + "            String first,\n"
                        + "            String second) throws Exception {\n"
                        + "    }\n"
                        + "}\n";

        String expected =
                "public class Foo {\n"
                        + "    public void doSomething(\n"
                        + "            String first,\n"
                        + "            String second\n"
                        + "    ) throws Exception {\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(expected, step.apply(input));
    }

    @Test
    void singleLineMethodNotTransformed() throws Exception {
        String input =
                "public class Foo {\n"
                        + "    public void doSomething(String first, String second) {\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(input, step.apply(input));
    }

    @Test
    void multiLineIfNotTransformed() throws Exception {
        // Control flow constructs must not be affected
        String input =
                "public class Foo {\n"
                        + "    public void bar() {\n"
                        + "        if (conditionOne\n"
                        + "                && conditionTwo) {\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(input, step.apply(input));
    }

    @Test
    void multiLineMethodCallNotTransformed() throws Exception {
        // Method calls end with ); not ) { so they must not be touched
        String input =
                "public class Foo {\n"
                        + "    public void bar() {\n"
                        + "        someMethod(\n"
                        + "                arg1,\n"
                        + "                arg2);\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(input, step.apply(input));
    }

    @Test
    void alreadyCorrectlyFormattedNotChanged() throws Exception {
        String input =
                "public class Foo {\n"
                        + "    public Foo(\n"
                        + "            int a,\n"
                        + "            int b\n"
                        + "    ) {\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(input, step.apply(input));
    }

    @Test
    void multiLineInterfaceMethod() throws Exception {
        String input =
                "public interface Foo {\n"
                        + "    void doSomething(\n"
                        + "            String first,\n"
                        + "            String second);\n"
                        + "}\n";

        String expected =
                "public interface Foo {\n"
                        + "    void doSomething(\n"
                        + "            String first,\n"
                        + "            String second\n"
                        + "    );\n"
                        + "}\n";

        assertEquals(expected, step.apply(input));
    }

    @Test
    void multiLineMethodCallNotTransformedForSemi() throws Exception {
        // A plain method call ending with ); must not be touched
        String input =
                "public class Foo {\n"
                        + "    public void bar() {\n"
                        + "        someMethod(\n"
                        + "                arg1,\n"
                        + "                arg2);\n"
                        + "    }\n"
                        + "}\n";

        assertEquals(input, step.apply(input));
    }

    @Test
    void noOpOnEmptyInput() throws Exception {
        assertEquals("", step.apply(""));
    }
}
