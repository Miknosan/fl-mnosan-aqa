package io.testautomation.booker.system;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.booker.framework.metadata.BookerQuality;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@BookerQuality
@ReportGroup("Architecture")
@DisplayName("Booker architecture quality gates")
class BookerArchitectureTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("io.testautomation.booker");
    private static final JavaClasses TEST_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .importPackages("io.testautomation.booker");

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that API clients remain independent from test frameworks")
    void shouldKeepProductionClientsIndependentFromTestFrameworks() {
        noClasses()
                .that().resideInAPackage("..client..")
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
    @DisplayName("[Architecture] Verify that test data factories remain independent from API clients and workflows")
    void shouldKeepDataFactoriesIndependentFromApiClientsAndWorkflows() {
        noClasses()
                .that().resideInAPackage("..data..")
                .should().dependOnClassesThat().resideInAnyPackage("..client..", "..workflow..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Architecture] Verify that authentication remains independent from booking")
    void shouldKeepAuthenticationIndependentFromBooking() {
        noClasses()
                .that().resideInAPackage("..authentication..")
                .should().dependOnClassesThat().resideInAPackage("..booking..")
                .check(PRODUCTION_CLASSES);
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
                .matching("io.testautomation.booker.(*)..")
                .should().beFreeOfCycles()
                .check(PRODUCTION_CLASSES);
    }
}
