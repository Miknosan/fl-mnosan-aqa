package io.testautomation.hygraph.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Architecture")
class HygraphArchitectureTest {
    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("io.testautomation.hygraph");

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepGraphQlProtocolIndependentFromMovieCatalog() {
        noClasses()
                .that().resideInAPackage("..graphql..")
                .should().dependOnClassesThat().resideInAPackage("..moviecatalog..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepProductionClientsIndependentFromTestFrameworks() {
        noClasses()
                .that().resideInAPackage("..client..")
                .should().dependOnClassesThat().resideInAnyPackage("org.assertj..", "org.junit..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepTopLevelPackagesFreeOfCycles() {
        slices()
                .matching("io.testautomation.hygraph.(*)..")
                .should().beFreeOfCycles()
                .check(PRODUCTION_CLASSES);
    }
}
