package io.testautomation.orchestrator;

import io.testautomation.core.config.ExecutionEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenCommandFactoryTest {
    @TempDir
    Path projectRoot;

    @Test
    void shouldPropagateEnvironmentAndResolvedProfile() throws IOException {
        Path profile = createProfile("stage");
        ExecutionSelection selection = selection("stage");

        List<String> command = MavenCommandFactory.create(projectRoot, selection);

        assertTrue(command.contains("-Dtest.environment=stage"));
        assertTrue(command.contains("-Dconfig.file=" + profile.toAbsolutePath().normalize()));
    }

    @Test
    void shouldFailBeforeExecutionForUnknownEnvironment() {
        ExecutionSelection selection = selection("preprod");

        assertThrows(IllegalStateException.class, () -> MavenCommandFactory.create(projectRoot, selection));
    }

    @Test
    void shouldFailBeforeExecutionForMismatchedProfileIdentity() throws IOException {
        Path profile = projectRoot.resolve("config/environments/stage.properties");
        Files.createDirectories(profile.getParent());
        Files.writeString(profile, "test.environment=dev\n");

        assertThrows(IllegalStateException.class, () -> MavenCommandFactory.create(projectRoot, selection("stage")));
    }

    private ExecutionSelection selection(String environment) {
        return new ExecutionSelection(
                Set.of(Domain.BOOKER),
                Set.of(TestPlan.SMOKE),
                "",
                new ExecutionEnvironment(environment),
                1);
    }

    private Path createProfile(String environment) throws IOException {
        Path profile = projectRoot.resolve("config/environments/" + environment + ".properties");
        Files.createDirectories(profile.getParent());
        Files.writeString(profile, "test.environment=" + environment + "\n");
        return profile;
    }
}
