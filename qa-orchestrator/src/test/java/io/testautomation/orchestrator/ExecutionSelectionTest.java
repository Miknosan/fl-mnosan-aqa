package io.testautomation.orchestrator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionSelectionTest {
    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("domains");
        System.clearProperty("plans");
        System.clearProperty("feature");
        System.clearProperty("environment");
        System.clearProperty("parallelism");
    }

    @Test
    void shouldCreateAValidatedEnvironmentSelection() {
        System.setProperty("domains", "booker");
        System.setProperty("plans", "smoke,regression");
        System.setProperty("environment", "Stage");

        ExecutionSelection selection = ExecutionSelection.fromSystemProperties();

        assertEquals("stage", selection.environment().value());
        assertEquals("business & (regression | smoke)", selection.tagExpression());
    }

    @Test
    void shouldRestrictFeatureSelectionToBusinessScenarios() {
        System.setProperty("domains", "demoqa");
        System.setProperty("plans", "regression");
        System.setProperty("feature", "web-tables");
        System.setProperty("environment", "dev");

        ExecutionSelection selection = ExecutionSelection.fromSystemProperties();

        assertEquals("business & (regression) & web-tables", selection.tagExpression());
    }

    @Test
    void shouldRequireAnExecutionEnvironment() {
        System.setProperty("domains", "booker");
        System.setProperty("plans", "smoke");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                ExecutionSelection::fromSystemProperties);

        assertTrue(exception.getMessage().contains("-Denvironment"));
    }
}
