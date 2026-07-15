package io.testautomation.hygraph.architecture;

import graphql.parser.Parser;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Source quality")
class SourceQualityTest {
    private static final Pattern INLINE_GRAPHQL =
            Pattern.compile(
                    "\"\\s*(query|mutation|fragment)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*(\\(|\\{)",
                    Pattern.CASE_INSENSITIVE);

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepJavaSourcesFreeOfCommentsDebtMarkersAndInlineGraphQl() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path sourceRoot : List.of(moduleDirectory().resolve("src/main/java"), moduleDirectory().resolve("src/test/java"))) {
            try (var paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !path.getFileName().toString().equals("SourceQualityTest.java"))
                        .forEach(path -> inspectJavaSource(path, violations));
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldKeepGraphQlResourcesNonBlankAndSyntacticallyValid() throws IOException {
        List<String> violations = new ArrayList<>();
        Path resources = moduleDirectory().resolve("src/main/resources/graphql");
        try (var paths = Files.walk(resources)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".graphql"))
                    .forEach(path -> inspectGraphQlDocument(path, violations));
        }
        assertThat(violations).isEmpty();
    }

    private static void inspectJavaSource(Path path, List<String> violations) {
        try {
            String source = Files.readString(path);
            List<String> forbiddenMarkers = List.of("TO" + "DO", "FIX" + "ME", "HA" + "CK");
            forbiddenMarkers.stream()
                    .filter(source::contains)
                    .forEach(marker -> violations.add(path + " contains forbidden marker " + marker));
            boolean hasLineComment = source.lines().anyMatch(line -> line.stripLeading().startsWith("/" + "/"));
            boolean hasBlockComment = source.contains("/" + "*") || source.contains("*" + "/");
            if (hasLineComment || hasBlockComment) {
                violations.add(path + " contains a source comment");
            }
            if (INLINE_GRAPHQL.matcher(source).find()) {
                violations.add(path + " contains an inline GraphQL document");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Java source: " + path, exception);
        }
    }

    private static void inspectGraphQlDocument(Path path, List<String> violations) {
        try {
            String document = Files.readString(path);
            if (document.isBlank()) {
                violations.add(path + " is blank");
            } else {
                Parser.parse(document);
            }
        } catch (RuntimeException | IOException exception) {
            violations.add(path + " is not a valid GraphQL document: " + exception.getMessage());
        }
    }

    private static Path moduleDirectory() {
        String directory = System.getProperty("hygraph.module.directory");
        if (directory == null || directory.isBlank()) {
            throw new IllegalStateException("Missing hygraph.module.directory system property");
        }
        return Path.of(directory);
    }
}
