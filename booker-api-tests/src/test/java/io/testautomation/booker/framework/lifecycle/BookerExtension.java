package io.testautomation.booker.framework.lifecycle;

import io.testautomation.booker.authentication.TokenProvider;
import io.testautomation.booker.authentication.client.AuthenticationClient;
import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.workflow.BookingWorkflow;
import io.testautomation.booker.config.BookerConfig;
import io.testautomation.booker.framework.metadata.BookerFeature;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.http.RestSpecFactory;
import io.testautomation.core.reporting.EnvironmentReportLabel;
import io.qameta.allure.Allure;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.Annotation;
import java.util.Arrays;
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

public final class BookerExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    private static final String DOMAIN_NAME = "Booker REST API";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(BookerExtension.class);
    private static final String INFRASTRUCTURE = "infrastructure";
    private static final String WORKFLOW = "workflow";

    @Override
    public void beforeEach(ExtensionContext context) {
        configureAllureHierarchy(context);
        Infrastructure infrastructure = infrastructure(context);
        context.getStore(NAMESPACE).put(WORKFLOW,
                new BookingWorkflow(infrastructure.bookingClient(), infrastructure.tokenProvider()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        BookingWorkflow workflow = context.getStore(NAMESPACE).remove(WORKFLOW, BookingWorkflow.class);
        if (workflow != null) {
            workflow.cleanup();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == AuthenticationClient.class
                || type == BookingClient.class
                || type == TokenProvider.class
                || type == BookingWorkflow.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        Infrastructure infrastructure = infrastructure(context);
        Class<?> type = parameterContext.getParameter().getType();
        if (type == AuthenticationClient.class) {
            return infrastructure.authenticationClient();
        }
        if (type == BookingClient.class) {
            return infrastructure.bookingClient();
        }
        if (type == TokenProvider.class) {
            return infrastructure.tokenProvider();
        }
        if (type == BookingWorkflow.class) {
            return context.getStore(NAMESPACE).get(WORKFLOW, BookingWorkflow.class);
        }
        throw new ParameterResolutionException("Unsupported Booker parameter: " + type.getName());
    }

    private static Infrastructure infrastructure(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                INFRASTRUCTURE, ignored -> createInfrastructure(), Infrastructure.class);
    }

    private static void configureAllureHierarchy(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        String feature = featureName(testClass);
        ReportGroup reportGroup = testClass.getAnnotation(ReportGroup.class);
        if (reportGroup == null || reportGroup.value().isBlank()) {
            throw new IllegalStateException("Booker test class must declare a non-blank @ReportGroup: "
                    + testClass.getName());
        }
        String group = reportGroup.value();
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
                    createParentSuiteLabel(DOMAIN_NAME),
                    createSuiteLabel(feature),
                    createSubSuiteLabel(group),
                    createEpicLabel(DOMAIN_NAME),
                    createFeatureLabel(feature),
                    createStoryLabel(group),
                    createPackageLabel(DOMAIN_NAME + "." + feature),
                    createTestClassLabel(group),
                    EnvironmentReportLabel.create()));
            result.setTitlePath(List.of(DOMAIN_NAME, feature, group));
        });
    }

    private static String featureName(Class<?> testClass) {
        List<Annotation> features = Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(BookerFeature.class))
                .toList();
        if (features.size() != 1) {
            throw new IllegalStateException(
                    "Booker test class must declare exactly one @BookerFeature annotation: "
                            + testClass.getName());
        }
        String name = features.get(0).annotationType().getAnnotation(BookerFeature.class).value();
        if (name.isBlank()) {
            throw new IllegalStateException("Booker feature name must not be blank: " + testClass.getName());
        }
        return name;
    }

    private static Infrastructure createInfrastructure() {
        BookerConfig config = BookerConfig.load();
        RequestSpecification specification = RestSpecFactory.create(config.baseUrl(), config.timeout());
        AuthenticationClient authenticationClient = new AuthenticationClient(specification);
        BookingClient bookingClient = new BookingClient(specification);
        TokenProvider tokenProvider = new TokenProvider(authenticationClient, config);
        return new Infrastructure(authenticationClient, bookingClient, tokenProvider);
    }

    private record Infrastructure(
            AuthenticationClient authenticationClient,
            BookingClient bookingClient,
            TokenProvider tokenProvider) {
    }
}
