package io.testautomation.core.reporting;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;
import java.util.Set;

import static io.qameta.allure.util.ResultsUtils.EPIC_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.FEATURE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.PACKAGE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.PARENT_SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.STORY_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.SUB_SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.TEST_CLASS_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.createEpicLabel;
import static io.qameta.allure.util.ResultsUtils.createFeatureLabel;
import static io.qameta.allure.util.ResultsUtils.createPackageLabel;
import static io.qameta.allure.util.ResultsUtils.createParentSuiteLabel;
import static io.qameta.allure.util.ResultsUtils.createStoryLabel;
import static io.qameta.allure.util.ResultsUtils.createSubSuiteLabel;
import static io.qameta.allure.util.ResultsUtils.createSuiteLabel;
import static io.qameta.allure.util.ResultsUtils.createTestClassLabel;

public abstract class QualityGateReportExtension implements BeforeEachCallback {
    private static final String QUALITY_GATES = "Framework Quality Gates";

    @Override
    public final void beforeEach(ExtensionContext context) {
        String domain = domainName();
        String group = qualityGateGroup(context.getRequiredTestClass());
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Quality gate domain name must not be blank");
        }
        if (group == null || group.isBlank()) {
            throw new IllegalStateException(
                    "Quality gate group must not be blank: " + context.getRequiredTestClass().getName());
        }
        Set<String> replacedLabels = Set.of(
                PARENT_SUITE_LABEL_NAME,
                SUITE_LABEL_NAME,
                SUB_SUITE_LABEL_NAME,
                EPIC_LABEL_NAME,
                FEATURE_LABEL_NAME,
                STORY_LABEL_NAME,
                PACKAGE_LABEL_NAME,
                TEST_CLASS_LABEL_NAME,
                EnvironmentReportLabel.NAME);
        Allure.getLifecycle().updateTestCase(result -> {
            result.getLabels().removeIf(label -> replacedLabels.contains(label.getName()));
            result.getLabels().addAll(List.of(
                    createParentSuiteLabel(domain),
                    createSuiteLabel(QUALITY_GATES),
                    createSubSuiteLabel(group),
                    createEpicLabel(domain),
                    createFeatureLabel(QUALITY_GATES),
                    createStoryLabel(group),
                    createPackageLabel(domain + "." + QUALITY_GATES),
                    createTestClassLabel(group),
                    EnvironmentReportLabel.create()));
            result.setTitlePath(List.of(domain, QUALITY_GATES, group));
        });
    }

    protected abstract String domainName();

    protected abstract String qualityGateGroup(Class<?> testClass);
}
