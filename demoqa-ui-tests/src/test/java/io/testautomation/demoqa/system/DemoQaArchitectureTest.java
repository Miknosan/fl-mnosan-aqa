package io.testautomation.demoqa.system;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.demoqa.framework.metadata.DemoQaQuality;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DemoQaQuality
@ReportGroup("Architecture")
@DisplayName("DemoQA architecture quality gates")
class DemoQaArchitectureTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("io.testautomation.demoqa");
    private static final JavaClasses TEST_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .importPackages("io.testautomation.demoqa");

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that pages and components remain independent from test frameworks")
    void shouldKeepPageObjectsAndComponentsIndependentFromTestFrameworks() {
        noClasses()
                .that().resideInAnyPackage("..page..", "..component..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.junit..",
                        "org.assertj..",
                        "io.qase..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that test data factories remain independent from pages and components")
    void shouldKeepDataFactoriesIndependentFromPageObjects() {
        noClasses()
                .that().resideInAPackage("..data..")
                .should().dependOnClassesThat().resideInAnyPackage("..page..", "..component..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that the platform layer remains independent from business features")
    void shouldKeepPlatformIndependentFromBusinessFeatures() {
        noClasses()
                .that().resideInAPackage("..platform..")
                .should().dependOnClassesThat().resideInAnyPackage("..registration..", "..webtables..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that business features remain independent from each other")
    void shouldKeepBusinessFeaturesIndependentFromEachOther() {
        noClasses()
                .that().resideInAPackage("..registration..")
                .should().dependOnClassesThat().resideInAPackage("..webtables..")
                .check(PRODUCTION_CLASSES);
        noClasses()
                .that().resideInAPackage("..webtables..")
                .should().dependOnClassesThat().resideInAPackage("..registration..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that product tests remain independent from the raw Playwright API")
    void shouldKeepProductTestsIndependentFromRawPlaywrightApi() {
        noClasses()
                .that().haveSimpleNameEndingWith("Test")
                .and().resideOutsideOfPackage("..system..")
                .should().dependOnClassesThat().resideInAPackage("com.microsoft.playwright..")
                .check(TEST_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that executable tests remain in scenario or system packages")
    void shouldKeepExecutableTestsInExplicitScenarioOrSystemPackages() {
        classes()
                .that().haveSimpleNameEndingWith("Test")
                .should().resideInAnyPackage("..features..scenarios..", "..system..")
                .check(TEST_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that top-level packages remain free of dependency cycles")
    void shouldKeepTopLevelPackagesFreeOfCycles() {
        slices()
                .matching("io.testautomation.demoqa.(*)..")
                .should().beFreeOfCycles()
                .check(PRODUCTION_CLASSES);
    }
}
