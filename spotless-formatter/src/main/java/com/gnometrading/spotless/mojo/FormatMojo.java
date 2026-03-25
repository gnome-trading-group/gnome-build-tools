package com.gnometrading.spotless.mojo;

import java.io.File;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Runs {@code spotless:apply} followed by the closing-paren formatter in a single command.
 *
 * <p>Usage: {@code mvn group.gnometrading:gnome-spotless-formatter:format}
 */
@Mojo(name = "format", requiresProject = true)
public class FormatMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException {
        runSpotlessApply();
    }

    private void runSpotlessApply() throws MojoExecutionException {
        Invoker invoker = new DefaultInvoker();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(project.getFile());
        request.setGoals(List.of("com.diffplug.spotless:spotless-maven-plugin:apply"));
        request.setBatchMode(true);

        // Propagate the settings file so GitHub Packages auth is available
        File userSettings = session.getRequest().getUserSettingsFile();
        if (userSettings != null && userSettings.exists()) {
            request.setUserSettingsFile(userSettings);
        }

        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("spotless:apply failed with exit code " + result.getExitCode());
            }
        } catch (MavenInvocationException e) {
            throw new MojoExecutionException("Failed to invoke spotless:apply", e);
        }
    }
}
