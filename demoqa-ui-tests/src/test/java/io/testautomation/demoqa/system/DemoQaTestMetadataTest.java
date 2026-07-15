package io.testautomation.demoqa.system;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseId;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.DemoQaFeature;
import io.testautomation.demoqa.framework.metadata.DemoQaQuality;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DemoQaQuality
@ReportGroup("Test Documentation Integrity")
@DisplayName("DemoQA test documentation integrity")
class DemoQaTestMetadataTest {
    private static final Map<Long, String> EXPECTED_CASES = expectedCases();

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Metadata] Verify that automated DemoQA scenarios remain synchronized with Qase")
    void shouldSynchronizeAutomatedScenariosWithDemoQaQaseCases() {
        List<Class<?>> testClasses = productTestClasses();
        assertThat(testClasses).isNotEmpty();

        Map<Long, String> discoveredCases = new LinkedHashMap<>();
        for (Class<?> testClass : testClasses) {
            requiredAnnotation(testClass, DemoQa.class);
            ReportGroup reportGroup = requiredAnnotation(testClass, ReportGroup.class);
            assertThat(reportGroup.value()).isNotBlank();
            assertThat(featureAnnotations(testClass)).hasSize(1);
            for (Method method : testClass.getDeclaredMethods()) {
                if (isTestMethod(method)) {
                    assertThat(hasTestPlan(method)).isTrue();
                    long qaseId = requiredAnnotation(method, QaseId.class).value();
                    String title = requiredAnnotation(method, DisplayName.class).value();
                    assertThat(discoveredCases).doesNotContainKey(qaseId);
                    discoveredCases.put(qaseId, title);
                }
            }
        }
        assertThat(discoveredCases).containsExactlyInAnyOrderEntriesOf(EXPECTED_CASES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Metadata] Verify that system tests remain excluded from Qase reporting")
    void shouldKeepSystemTestsOutOfQaseReporting() {
        List<Class<?>> qualityTestClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.demoqa.system")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();

        assertThat(qualityTestClasses).isNotEmpty();
        for (Class<?> testClass : qualityTestClasses) {
            for (Method method : testClass.getDeclaredMethods()) {
                if (isTestMethod(method)) {
                    assertThat(method.isAnnotationPresent(QaseIgnore.class)).isTrue();
                }
            }
        }
    }

    private static List<Class<?>> productTestClasses() {
        return new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages(
                        "io.testautomation.demoqa.features.registration.scenarios",
                        "io.testautomation.demoqa.features.webtables.scenarios")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();
    }

    private static List<Annotation> featureAnnotations(Class<?> testClass) {
        return Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(DemoQaFeature.class))
                .toList();
    }

    private static <A extends Annotation> A requiredAnnotation(
            AnnotatedElement element,
            Class<A> annotationType) {
        return Optional.ofNullable(element.getAnnotation(annotationType))
                .orElseThrow(() -> new AssertionError(
                        "Missing @" + annotationType.getSimpleName() + " on " + element));
    }

    private static boolean isTestMethod(Method method) {
        return method.isAnnotationPresent(Test.class) || method.isAnnotationPresent(ParameterizedTest.class);
    }

    private static boolean hasTestPlan(Method method) {
        return method.isAnnotationPresent(Smoke.class) || method.isAnnotationPresent(Regression.class);
    }

    private static Map<Long, String> expectedCases() {
        return Map.of(
                44L, "[UI] Verify that the student registration is completed when all supported fields contain valid data",
                45L, "[UI] Verify that the student registration is blocked when a required field is missing",
                46L, "[UI] Verify that the student registration is blocked when the email format is invalid",
                47L, "[UI] Verify that the student registration is blocked when the mobile number has fewer than ten digits",
                48L, "[UI] Verify that a web table record is added when the registration form contains valid data",
                49L, "[UI] Verify that a web table record is updated when valid changes are submitted for an existing record",
                50L, "[UI] Verify that a web table record is removed when delete is selected for an existing record",
                51L, "[UI] Verify that web table results are filtered when a matching search value is entered",
                52L, "[UI] Verify that web table records are ordered when a sortable column header is selected");
    }
}
