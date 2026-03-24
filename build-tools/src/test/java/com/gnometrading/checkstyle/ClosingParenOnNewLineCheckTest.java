package com.gnometrading.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClosingParenOnNewLineCheckTest {

    @TempDir
    File tempDir;

    @Test
    void flagsMultiLineConstructorWithClosingParenOnLastParamLine() throws Exception {
        File file = writeJava(
                "package test;\n"
                        + "public class Foo {\n"
                        + "    public Foo(\n"
                        + "            int a,\n"
                        + "            int b) {\n"
                        + "    }\n"
                        + "}\n");

        List<String> violations = runCheck(file);
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).contains(ClosingParenOnNewLineCheck.MSG_KEY));
    }

    @Test
    void flagsMultiLineMethodWithClosingParenOnLastParamLine() throws Exception {
        File file = writeJava(
                "package test;\n"
                        + "public class Foo {\n"
                        + "    public void doSomething(\n"
                        + "            String first,\n"
                        + "            String second) {\n"
                        + "    }\n"
                        + "}\n");

        List<String> violations = runCheck(file);
        assertEquals(1, violations.size());
    }

    @Test
    void acceptsSingleLineDeclaration() throws Exception {
        File file = writeJava(
                "package test;\n"
                        + "public class Foo {\n"
                        + "    public void doSomething(String first, String second) {\n"
                        + "    }\n"
                        + "}\n");

        assertTrue(runCheck(file).isEmpty());
    }

    @Test
    void acceptsClosingParenOnOwnLine() throws Exception {
        File file = writeJava(
                "package test;\n"
                        + "public class Foo {\n"
                        + "    public Foo(\n"
                        + "            int a,\n"
                        + "            int b\n"
                        + "    ) {\n"
                        + "    }\n"
                        + "}\n");

        assertTrue(runCheck(file).isEmpty());
    }

    @Test
    void acceptsNoArgDeclaration() throws Exception {
        File file = writeJava(
                "package test;\n"
                        + "public class Foo {\n"
                        + "    public Foo() {\n"
                        + "    }\n"
                        + "}\n");

        assertTrue(runCheck(file).isEmpty());
    }

    private List<String> runCheck(File file) throws CheckstyleException {
        DefaultConfiguration checkConfig = new DefaultConfiguration(
                ClosingParenOnNewLineCheck.class.getName());

        DefaultConfiguration treeWalkerConfig = new DefaultConfiguration("TreeWalker");
        treeWalkerConfig.addChild(checkConfig);

        DefaultConfiguration checkerConfig = new DefaultConfiguration("Checker");
        checkerConfig.addProperty("charset", "UTF-8");
        checkerConfig.addChild(treeWalkerConfig);

        List<String> violations = new ArrayList<>();
        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(checkerConfig);
        checker.addListener(new AuditListener() {
            @Override
            public void auditStarted(AuditEvent event) {}

            @Override
            public void auditFinished(AuditEvent event) {}

            @Override
            public void fileStarted(AuditEvent event) {}

            @Override
            public void fileFinished(AuditEvent event) {}

            @Override
            public void addError(AuditEvent event) {
                violations.add(event.getMessage());
            }

            @Override
            public void addException(AuditEvent event, Throwable throwable) {}
        });
        checker.process(List.of(file));
        return violations;
    }

    private File writeJava(String content) throws IOException {
        File file = new File(tempDir, "Foo.java");
        Files.writeString(file.toPath(), content);
        return file;
    }
}
