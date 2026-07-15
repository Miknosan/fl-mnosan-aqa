package io.testautomation.hygraph.system;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseId;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.ExternalContract;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.core.classification.SystemTest;
import io.testautomation.core.classification.TestTags;
import io.testautomation.hygraph.framework.metadata.GraphQlPlatformFeature;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.HygraphFeature;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SystemTest
class HygraphTestMetadataTest {
    @Test
    @QaseIgnore
    void shouldEnforceRequiredMetadataAndUniqueQaseIdsForMovieCatalogScenarios() {
        List<Class<?>> testClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.hygraph.features.moviecatalog.scenarios")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();

        assertThat(testClasses).hasSize(6);
        assertThat(java.util.Arrays.stream(Hygraph.class.getAnnotationsByType(Tag.class)).map(Tag::value))
                .contains(TestTags.BUSINESS);
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
    void shouldKeepSystemTestsOutOfQaseReporting() {
        List<Class<?>> qualityTestClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.OnlyIncludeTests())
                .importPackages("io.testautomation.hygraph.system")
                .stream()
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Test"))
                .<Class<?>>map(JavaClass::reflect)
                .toList();

        assertThat(qualityTestClasses).isNotEmpty();
        for (Class<?> testClass : qualityTestClasses) {
            boolean systemTest = testClass.isAnnotationPresent(SystemTest.class);
            boolean externalContract = testClass.isAnnotationPresent(ExternalContract.class);
            assertThat(systemTest ^ externalContract).isTrue();
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    assertThat(method.isAnnotationPresent(QaseIgnore.class)).isTrue();
                    assertThat(hasTestPlan(method)).isFalse();
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
