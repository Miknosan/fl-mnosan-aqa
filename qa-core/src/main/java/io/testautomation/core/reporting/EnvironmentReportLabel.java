package io.testautomation.core.reporting;

import io.qameta.allure.model.Label;
import io.testautomation.core.config.ExecutionEnvironment;

public final class EnvironmentReportLabel {
    public static final String NAME = "environment";

    private EnvironmentReportLabel() {
    }

    public static Label create() {
        return new Label()
                .setName(NAME)
                .setValue(ExecutionEnvironment.current().displayName());
    }
}
