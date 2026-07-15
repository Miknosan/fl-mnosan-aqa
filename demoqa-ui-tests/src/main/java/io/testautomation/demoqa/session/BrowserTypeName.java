package io.testautomation.demoqa.session;

import java.util.Locale;

public enum BrowserTypeName {
    CHROMIUM,
    FIREFOX,
    WEBKIT;

    public static BrowserTypeName from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + value + "'. Expected chromium, firefox, or webkit.", exception);
        }
    }
}
