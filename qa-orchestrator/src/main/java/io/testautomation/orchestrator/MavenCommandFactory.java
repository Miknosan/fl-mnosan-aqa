package io.testautomation.orchestrator;

import io.testautomation.core.config.ExecutionEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class MavenCommandFactory {
    private MavenCommandFactory() {
    }

    static Path findProjectRoot(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("mvnw"))
                    && Files.isRegularFile(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root containing mvnw and pom.xml");
    }

    static List<String> create(Path projectRoot, ExecutionSelection selection) {
        Path environmentProfile = selection.environment().catalogProfile(projectRoot);
        List<String> command = new ArrayList<>();
        command.add(projectRoot.resolve(isWindows() ? "mvnw.cmd" : "mvnw").toString());
        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        command.add("--fail-at-end");
        command.add("-pl");
        command.add(String.join(",", selection.modules()));
        command.add("-am");
        command.add("test");
        command.add("-Dgroups=" + selection.tagExpression());
        command.add("-Djunit.jupiter.execution.parallel.enabled=" + (selection.parallelism() > 1));
        if (selection.parallelism() > 1) {
            command.add("-Djunit.jupiter.execution.parallel.config.strategy=fixed");
            command.add("-Djunit.jupiter.execution.parallel.config.fixed.parallelism=" + selection.parallelism());
        }
        command.add("-D" + ExecutionEnvironment.SYSTEM_PROPERTY + "=" + selection.environment().value());
        command.add("-D" + ExecutionEnvironment.CONFIG_FILE_PROPERTY + "=" + environmentProfile);
        return List.copyOf(command);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
