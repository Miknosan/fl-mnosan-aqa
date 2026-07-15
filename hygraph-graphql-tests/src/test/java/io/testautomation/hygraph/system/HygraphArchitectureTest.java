package io.testautomation.hygraph.system;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.SystemTest;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@SystemTest
class HygraphArchitectureTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("io.testautomation.hygraph");
    private static final JavaClasses TEST_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .importPackages("io.testautomation.hygraph");

    @Test
    @QaseIgnore
    void shouldKeepGraphQlProtocolIndependentFromMovieCatalog() {
        noClasses()
                .that().resideInAPackage("..graphql..")
                .should().dependOnClassesThat().resideInAPackage("..moviecatalog..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    void shouldKeepProductionClientsIndependentFromTestFrameworks() {
        noClasses()
                .that().resideInAPackage("..client..")
                .should().dependOnClassesThat().resideInAnyPackage("org.assertj..", "org.junit..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    void shouldKeepExecutableTestsInExplicitScenarioOrSystemPackages() {
        classes()
                .that().haveSimpleNameEndingWith("Test")
                .should().resideInAnyPackage("..features..scenarios..", "..system..")
                .check(TEST_CLASSES);
    }

    @Test
    @QaseIgnore
    void shouldKeepTopLevelPackagesFreeOfCycles() {
        slices()
                .matching("io.testautomation.hygraph.(*)..")
                .should().beFreeOfCycles()
                .check(PRODUCTION_CLASSES);
    }
}
