package io.testautomation.demoqa.framework.lifecycle;

import io.qameta.allure.Allure;
import io.testautomation.demoqa.platform.config.DemoQaConfig;
import io.testautomation.demoqa.platform.playwright.PlaywrightSession;
import io.testautomation.demoqa.framework.metadata.DemoQaFeature;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.core.reporting.EnvironmentReportLabel;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.qameta.allure.util.ResultsUtils.EPIC_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.FEATURE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.PARENT_SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.STORY_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.SUB_SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.SUITE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.createEpicLabel;
import static io.qameta.allure.util.ResultsUtils.createFeatureLabel;
import static io.qameta.allure.util.ResultsUtils.createParentSuiteLabel;
import static io.qameta.allure.util.ResultsUtils.createStoryLabel;
import static io.qameta.allure.util.ResultsUtils.createSubSuiteLabel;
import static io.qameta.allure.util.ResultsUtils.createSuiteLabel;

public final class DemoQaExtension implements BeforeEachCallback, AfterTestExecutionCallback, AfterEachCallback,
        TestExecutionExceptionHandler, InvocationInterceptor {
    private static final String DOMAIN_NAME = "DemoQA UI";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(DemoQaExtension.class);
    private static final String CONFIG = "demoqa-config";
    private static final String SESSION = "demoqa-session";
    private static final String FAILURE_EVIDENCE_CAPTURED = "failure-evidence-captured";

    @Override
    public void beforeEach(ExtensionContext context) {
        configureAllureHierarchy(context);
        DemoQaConfig config = DemoQaConfig.load();
        context.getStore(NAMESPACE).put(CONFIG, config);
        context.getStore(NAMESPACE).put(SESSION, PlaywrightSession.open(config));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        captureFailureEvidence(context);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (context.getStore(NAMESPACE).get(FAILURE_EVIDENCE_CAPTURED) == null) {
            captureFailureEvidence(context);
        }
        throw throwable;
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        invokeWithFailureEvidence(invocation, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        invokeWithFailureEvidence(invocation, extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PlaywrightSession session = context.getStore(NAMESPACE).remove(SESSION, PlaywrightSession.class);
        if (session == null) {
            return;
        }
        try {
            if (context.getExecutionException().isPresent()
                    && context.getStore(NAMESPACE).get(FAILURE_EVIDENCE_CAPTURED) == null) {
                captureFailureEvidence(context, session);
            } else if (context.getExecutionException().isEmpty()) {
                session.discardTrace();
            }
        } finally {
            context.getStore(NAMESPACE).remove(CONFIG);
            context.getStore(NAMESPACE).remove(FAILURE_EVIDENCE_CAPTURED);
            session.close();
        }
    }

    private static void captureFailureEvidence(ExtensionContext context) {
        captureFailureEvidence(context, session(context));
    }

    private static void captureFailureEvidence(ExtensionContext context, PlaywrightSession session) {
        context.getStore(NAMESPACE).put(FAILURE_EVIDENCE_CAPTURED, true);
        DemoQaConfig config = config(context);
        String artifactName = context.getRequiredTestClass().getSimpleName()
                + "-" + context.getRequiredTestMethod().getName()
                + "-" + Integer.toUnsignedString(context.getUniqueId().hashCode());
        Path screenshotDirectory = config.artifactsDirectory().resolve("screenshots");
        Path traceDirectory = config.artifactsDirectory().resolve("traces");
        try {
            Files.createDirectories(screenshotDirectory);
            Files.createDirectories(traceDirectory);
            byte[] screenshot = session.screenshot(screenshotDirectory.resolve(artifactName + ".png"));
            Allure.addAttachment(
                    "UI failure screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png");
            if (config.traceEnabled()) {
                Path trace = traceDirectory.resolve(artifactName + ".zip");
                session.saveTrace(trace);
                try (var traceContent = Files.newInputStream(trace)) {
                    Allure.addAttachment(
                            "UI failure trace",
                            "application/zip",
                            traceContent,
                            ".zip");
                }
            }
            List<String> browserErrors = session.browserErrors();
            if (!browserErrors.isEmpty()) {
                Allure.addAttachment("Browser errors", "text/plain", String.join(System.lineSeparator(), browserErrors));
            }
        } catch (IOException | RuntimeException exception) {
            try {
                Files.writeString(
                        config.artifactsDirectory().resolve(artifactName + ".error.txt"),
                        exception.toString());
            } catch (IOException ignored) {
                exception.addSuppressed(ignored);
            }
            context.publishReportEntry("ui-artifact-error", exception.getMessage());
        }
    }

    private static void invokeWithFailureEvidence(
            InvocationInterceptor.Invocation<Void> invocation,
            ExtensionContext context) throws Throwable {
        try {
            invocation.proceed();
        } catch (Throwable throwable) {
            if (context.getStore(NAMESPACE).get(FAILURE_EVIDENCE_CAPTURED) == null) {
                captureFailureEvidence(context);
            }
            throw throwable;
        }
    }

    public static PlaywrightSession session(ExtensionContext context) {
        PlaywrightSession session = context.getStore(NAMESPACE).get(SESSION, PlaywrightSession.class);
        if (session == null) {
            throw new IllegalStateException("DemoQA UI session is not initialized");
        }
        return session;
    }

    public static DemoQaConfig config(ExtensionContext context) {
        DemoQaConfig config = context.getStore(NAMESPACE).get(CONFIG, DemoQaConfig.class);
        if (config == null) {
            throw new IllegalStateException("DemoQA configuration is not initialized");
        }
        return config;
    }

    private static void configureAllureHierarchy(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        String feature = featureName(testClass);
        ReportGroup reportGroup = testClass.getAnnotation(ReportGroup.class);
        if (reportGroup == null || reportGroup.value().isBlank()) {
            throw new IllegalStateException(
                    "DemoQA test class must declare a non-blank @ReportGroup: " + testClass.getName());
        }
        String group = reportGroup.value();
        Set<String> replacedLabels = Set.of(
                PARENT_SUITE_LABEL_NAME,
                SUITE_LABEL_NAME,
                SUB_SUITE_LABEL_NAME,
                EPIC_LABEL_NAME,
                FEATURE_LABEL_NAME,
                STORY_LABEL_NAME,
                EnvironmentReportLabel.NAME);
        Allure.getLifecycle().updateTestCase(result -> {
            result.getLabels().removeIf(label -> replacedLabels.contains(label.getName()));
            result.getLabels().addAll(List.of(
                    createParentSuiteLabel(DOMAIN_NAME),
                    createSuiteLabel(feature),
                    createSubSuiteLabel(group),
                    createEpicLabel(DOMAIN_NAME),
                    createFeatureLabel(feature),
                    createStoryLabel(group),
                    EnvironmentReportLabel.create()));
            result.setTitlePath(List.of(DOMAIN_NAME, feature, group));
        });
    }

    private static String featureName(Class<?> testClass) {
        List<java.lang.annotation.Annotation> features = Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(DemoQaFeature.class))
                .toList();
        if (features.size() != 1) {
            throw new IllegalStateException(
                    "DemoQA test class must declare exactly one @DemoQaFeature annotation: " + testClass.getName());
        }
        String value = features.get(0).annotationType().getAnnotation(DemoQaFeature.class).value();
        if (value.isBlank()) {
            throw new IllegalStateException("DemoQA feature name must not be blank: " + testClass.getName());
        }
        return value;
    }
}
