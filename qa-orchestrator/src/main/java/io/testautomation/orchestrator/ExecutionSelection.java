package io.testautomation.orchestrator;

import io.testautomation.core.config.ExecutionEnvironment;
import io.testautomation.core.classification.TestTags;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

record ExecutionSelection(
        Set<Domain> domains,
        Set<TestPlan> plans,
        String feature,
        ExecutionEnvironment environment,
        int parallelism) {
    private static final Pattern SAFE_VALUE = Pattern.compile("[a-z0-9][a-z0-9-]*");

    static ExecutionSelection fromSystemProperties() {
        Set<Domain> domains = parseDomains(required("domains"));
        Set<TestPlan> plans = parsePlans(required("plans"));
        String feature = optional("feature");
        ExecutionEnvironment environment = new ExecutionEnvironment(requiredEnvironment());
        int parallelism = Integer.parseInt(System.getProperty("parallelism", "1"));

        validateOptional("feature", feature);
        if (parallelism < 1 || parallelism > 8) {
            throw new IllegalArgumentException("parallelism must be between 1 and 8");
        }
        return new ExecutionSelection(domains, plans, feature, environment, parallelism);
    }

    List<String> modules() {
        return domains.stream().map(Domain::module).sorted().toList();
    }

    String tagExpression() {
        String plansExpression = plans.stream()
                .map(TestPlan::tag)
                .sorted()
                .collect(Collectors.joining(" | "));
        String businessPlanExpression = TestTags.BUSINESS + " & (" + plansExpression + ")";
        return feature.isBlank() ? businessPlanExpression : businessPlanExpression + " & " + feature;
    }

    private static Set<Domain> parseDomains(String value) {
        List<String> values = split(value);
        if (values.stream().map(item -> item.toLowerCase(Locale.ROOT)).anyMatch("all"::equals)) {
            if (values.size() != 1) {
                throw new IllegalArgumentException("Domain 'all' cannot be combined with explicit domains");
            }
            return EnumSet.allOf(Domain.class);
        }
        return values.stream().map(Domain::parse).collect(Collectors.toCollection(() -> EnumSet.noneOf(Domain.class)));
    }

    private static Set<TestPlan> parsePlans(String value) {
        return split(value).stream()
                .map(TestPlan::parse)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TestPlan.class)));
    }

    private static List<String> split(String value) {
        List<String> values = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Selection must contain at least one value");
        }
        return values;
    }

    private static String required(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required system property -D" + name);
        }
        return value;
    }

    private static String optional(String name) {
        return System.getProperty(name, "").trim().toLowerCase(Locale.ROOT);
    }

    private static String requiredEnvironment() {
        String value = System.getProperty("environment");
        if (value == null || value.isBlank()) {
            value = System.getenv(ExecutionEnvironment.ENVIRONMENT_VARIABLE);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing execution environment. Provide -Denvironment=<name> or "
                    + ExecutionEnvironment.ENVIRONMENT_VARIABLE);
        }
        return value;
    }

    private static void validateOptional(String name, String value) {
        if (!value.isBlank() && !SAFE_VALUE.matcher(value).matches()) {
            throw new IllegalArgumentException(name + " must match " + SAFE_VALUE.pattern());
        }
    }
}
