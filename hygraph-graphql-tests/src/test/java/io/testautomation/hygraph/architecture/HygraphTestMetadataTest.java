package io.testautomation.hygraph.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseId;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.classification.HygraphFeature;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Test metadata")
class HygraphTestMetadataTest {
    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldEnforceRequiredMetadataAndUniqueQaseIdsForMovieCatalogScenarios() {
        List<Class<?>> testClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.hygraph.moviecatalog")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .map(JavaClass::reflect)
                .toList();

        assertThat(testClasses).hasSize(6);
        Set<Long> qaseIds = new HashSet<>();
        for (Class<?> testClass : testClasses) {
            assertThat(testClass.isAnnotationPresent(Hygraph.class)).isTrue();
            assertThat(testClass.isAnnotationPresent(ReportGroup.class)).isTrue();
            assertThat(testClass.getAnnotation(ReportGroup.class).value()).isNotBlank();
            assertThat(featureAnnotations(testClass)).hasSize(1);
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    assertThat(hasTestPlan(method)).isTrue();
                    assertThat(method.isAnnotationPresent(QaseId.class)).isTrue();
                    assertThat(qaseIds.add(method.getAnnotation(QaseId.class).value())).isTrue();
                }
            }
        }
        assertThat(qaseIds).containsExactlyInAnyOrder(38L, 39L, 40L, 41L, 42L, 43L);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepPlatformQualityTestsOutOfQaseReporting() {
        List<Class<?>> qualityTestClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.hygraph")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .filter(javaClass -> !javaClass.getPackageName().startsWith("io.testautomation.hygraph.moviecatalog"))
                .map(JavaClass::reflect)
                .toList();

        assertThat(qualityTestClasses).isNotEmpty();
        for (Class<?> testClass : qualityTestClasses) {
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    assertThat(method.isAnnotationPresent(QaseIgnore.class)).isTrue();
                }
            }
        }
    }

    private static List<Class<? extends java.lang.annotation.Annotation>> featureAnnotations(Class<?> testClass) {
        return java.util.Arrays.stream(testClass.getAnnotations())
                .map(java.lang.annotation.Annotation::annotationType)
                .filter(annotation -> annotation.isAnnotationPresent(HygraphFeature.class))
                .toList();
    }

    private static boolean hasTestPlan(Method method) {
        return method.isAnnotationPresent(Smoke.class) || method.isAnnotationPresent(Regression.class);
    }
}
