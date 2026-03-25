package com.gnometrading.spotless.mojo;

import com.gnometrading.spotless.ClosingParenNewLineStep;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Checks that closing parentheses of multi-line method/constructor declarations
 * are on their own line. Fails the build if any violations are found.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class CheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true, readonly = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}", required = true, readonly = true)
    private File testSourceDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        ClosingParenNewLineStep step = new ClosingParenNewLineStep();
        List<Path> violations = new ArrayList<>();
        for (File dir : List.of(sourceDirectory, testSourceDirectory)) {
            violations.addAll(findViolations(step, dir));
        }
        if (!violations.isEmpty()) {
            violations.forEach(p -> getLog().error("gnome-formatter: needs formatting: " + p));
            throw new MojoExecutionException(
                    violations.size()
                            + " file(s) have closing parentheses not on their own line."
                            + " Run 'mvn group.gnometrading:gnome-spotless-formatter:format' to fix.");
        }
    }

    private List<Path> findViolations(ClosingParenNewLineStep step, File dir) throws MojoExecutionException {
        if (!dir.exists()) {
            return List.of();
        }
        List<Path> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                String original = Files.readString(path);
                String formatted = step.apply(original);
                if (!original.equals(formatted)) {
                    violations.add(path);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to walk directory: " + dir, e);
        }
        return violations;
    }
}
