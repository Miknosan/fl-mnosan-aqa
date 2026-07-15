package io.testautomation.orchestrator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class QaOrchestrator {
    private QaOrchestrator() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ExecutionSelection selection = ExecutionSelection.fromSystemProperties();
        Path projectRoot = MavenCommandFactory.findProjectRoot(Path.of(System.getProperty("user.dir")));
        List<String> command = MavenCommandFactory.create(projectRoot, selection);

        System.out.println("Domains: " + selection.domains());
        System.out.println("Plans: " + selection.plans());
        System.out.println("Environment: " + selection.environment().displayName());
        System.out.println("JUnit tag expression: " + selection.tagExpression());
        System.out.println("Modules: " + selection.modules());

        int exitCode = new ProcessBuilder(command)
                .directory(projectRoot.toFile())
                .inheritIO()
                .start()
                .waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Selected test execution failed with exit code " + exitCode);
        }
    }
}
