package com.gnometrading.spotless.mojo;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Applies closing-paren-on-new-line formatting to Java source files in-place. */
@Mojo(name = "apply")
public class ApplyMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true, readonly = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}", required = true, readonly = true)
    private File testSourceDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        JavaFormatter formatter = new JavaFormatter(getLog());
        int count = 0;
        for (File dir : List.of(sourceDirectory, testSourceDirectory)) {
            count += formatter.applyToDirectory(dir);
        }
        if (count > 0) {
            getLog().info("gnome-formatter: reformatted " + count + " file(s).");
        }
    }
}
