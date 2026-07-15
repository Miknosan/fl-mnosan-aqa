package io.testautomation.demoqa.platform.playwright;

import java.util.Locale;

public enum BrowserEngine {
    CHROMIUM,
    FIREFOX,
    WEBKIT;

    public static BrowserEngine from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + value + "'. Expected chromium, firefox, or webkit.", exception);
        }
    }
}
