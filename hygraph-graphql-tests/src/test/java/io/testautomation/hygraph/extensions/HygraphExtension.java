package io.testautomation.hygraph.extensions;

import io.qameta.allure.Allure;
import io.testautomation.hygraph.classification.HygraphFeature;
import io.testautomation.hygraph.config.HygraphConfig;
import io.testautomation.hygraph.graphql.client.GraphQlClient;
import io.testautomation.hygraph.reporting.ReportGroup;
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

public final class HygraphExtension implements BeforeEachCallback, ParameterResolver {
    private static final String DOMAIN_NAME = "Hygraph GraphQL";
    private static final String GRAPHQL_CLIENT = "graphql-client";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(HygraphExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        configureAllureHierarchy(context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == GraphQlClient.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        if (!supportsParameter(parameterContext, context)) {
            throw new ParameterResolutionException(
                    "Unsupported Hygraph parameter: " + parameterContext.getParameter().getType().getName());
        }
        return graphQlClient(context);
    }

    static GraphQlClient graphQlClient(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                GRAPHQL_CLIENT,
                ignored -> new GraphQlClient(HygraphConfig.load()),
                GraphQlClient.class);
    }

    private static void configureAllureHierarchy(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        String featureName = featureName(testClass);
        ReportGroup reportGroup = testClass.getAnnotation(ReportGroup.class);
        if (reportGroup == null || reportGroup.value().isBlank()) {
            throw new IllegalStateException(
                    "Hygraph test class must declare a non-blank @ReportGroup: " + testClass.getName());
        }
        String group = reportGroup.value();
        Set<String> replacedLabels = Set.of(
                PARENT_SUITE_LABEL_NAME,
                SUITE_LABEL_NAME,
                SUB_SUITE_LABEL_NAME,
                EPIC_LABEL_NAME,
                FEATURE_LABEL_NAME,
                STORY_LABEL_NAME);

        Allure.getLifecycle().updateTestCase(result -> {
            result.getLabels().removeIf(label -> replacedLabels.contains(label.getName()));
            result.getLabels().addAll(List.of(
                    createParentSuiteLabel(DOMAIN_NAME),
                    createSuiteLabel(featureName),
                    createSubSuiteLabel(group),
                    createEpicLabel(DOMAIN_NAME),
                    createFeatureLabel(featureName),
                    createStoryLabel(group)));
            result.setTitlePath(List.of(DOMAIN_NAME, featureName, group));
        });
    }

    private static String featureName(Class<?> testClass) {
        List<Annotation> features = Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(HygraphFeature.class))
                .toList();
        if (features.size() != 1) {
            throw new IllegalStateException(
                    "Hygraph test class must declare exactly one @HygraphFeature annotation: "
                            + testClass.getName());
        }
        String name = features.get(0).annotationType().getAnnotation(HygraphFeature.class).value();
        if (name.isBlank()) {
            throw new IllegalStateException("Hygraph feature name must not be blank: " + testClass.getName());
        }
        return name;
    }
}
