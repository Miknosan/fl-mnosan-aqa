package io.testautomation.demoqa.registration.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record RegistrationResult(Map<String, String> values) {
    public RegistrationResult {
        values = Map.copyOf(new LinkedHashMap<>(values));
    }

    public String value(String label) {
        return values.get(label);
    }
}
