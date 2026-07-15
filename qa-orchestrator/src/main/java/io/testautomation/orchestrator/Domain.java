package io.testautomation.orchestrator;

import java.util.Locale;

enum Domain {
    BOOKER("booker-api-tests"),
    HYGRAPH("hygraph-graphql-tests"),
    DEMOQA("demoqa-ui-tests");

    private final String module;

    Domain(String module) {
        this.module = module;
    }

    String module() {
        return module;
    }

    static Domain parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported domain '" + value + "'. Expected booker, hygraph, demoqa, or all.", exception);
        }
    }
}
