package io.testautomation.booker.system;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseId;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.booker.framework.metadata.Booker;
import io.testautomation.booker.framework.metadata.BookerFeature;
import io.testautomation.booker.framework.metadata.BookerQuality;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
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

@BookerQuality
@ReportGroup("Test Documentation Integrity")
@DisplayName("Booker test documentation integrity")
class BookerTestMetadataTest {
    private static final Map<Long, String> EXPECTED_CASES = Map.of(
            1L, "Verify that an authentication token is returned when valid credentials are submitted",
            3L, "Verify that authentication is rejected when the password is invalid",
            5L, "Verify that a booking is created when a valid complete payload is submitted",
            9L, "Verify that booking creation is rejected when a mandatory field is missing",
            13L, "Verify that a booking is returned when an existing booking ID is requested as JSON",
            15L, "Verify that a not-found response is returned when a non-existent booking ID is requested",
            19L, "Verify that bookings are filtered when matching first and last names are supplied",
            23L, "Verify that a booking is fully replaced when a valid payload and auth token are supplied",
            25L, "Verify that a booking remains unchanged when a full update is attempted without authorization",
            33L, "Verify that a booking is removed when deletion is requested with a valid auth token");

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Metadata] Verify that automated Booker scenarios remain synchronized with Qase")
    void shouldSynchronizeAutomatedScenariosWithBookerQaseCases() {
        List<Class<?>> testClasses = productTestClasses();
        assertThat(testClasses).isNotEmpty();

        Map<Long, String> discoveredCases = new LinkedHashMap<>();
        for (Class<?> testClass : testClasses) {
            requiredAnnotation(testClass, Booker.class);
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
        List<Class<?>> systemTestClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.booker.system")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();

        assertThat(systemTestClasses).isNotEmpty();
        for (Class<?> testClass : systemTestClasses) {
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
                        "io.testautomation.booker.features.authentication.scenarios",
                        "io.testautomation.booker.features.booking.scenarios")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();
    }

    private static List<Annotation> featureAnnotations(Class<?> testClass) {
        return Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(BookerFeature.class))
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
}
