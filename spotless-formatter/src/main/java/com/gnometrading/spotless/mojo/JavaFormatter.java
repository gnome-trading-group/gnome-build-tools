package com.gnometrading.spotless.mojo;

import com.gnometrading.spotless.ClosingParenNewLineStep;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

class JavaFormatter {

    private final ClosingParenNewLineStep step = new ClosingParenNewLineStep();
    private final Log log;

    JavaFormatter(Log log) {
        this.log = log;
    }

    int applyToDirectory(File dir) throws MojoExecutionException {
        if (!dir.exists()) {
            return 0;
        }
        int count = 0;
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (applyToFile(path)) {
                    count++;
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to walk directory: " + dir, e);
        }
        return count;
    }

    private boolean applyToFile(Path path) throws MojoExecutionException {
        try {
            String original = Files.readString(path);
            String formatted = step.apply(original);
            if (!original.equals(formatted)) {
                Files.writeString(path, formatted);
                log.debug("gnome-formatter: reformatted " + path);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to format: " + path, e);
        }
    }
}
