package io.testautomation.orchestrator;

import java.util.Locale;

enum TestPlan {
    SMOKE("smoke"),
    REGRESSION("regression"),
    SANITY("sanity");

    private final String tag;

    TestPlan(String tag) {
        this.tag = tag;
    }

    String tag() {
        return tag;
    }

    static TestPlan parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported test plan '" + value + "'. Expected smoke, regression, or sanity.", exception);
        }
    }
}
