package io.testautomation.booker.system;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.booker.framework.metadata.BookerQuality;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@BookerQuality
@ReportGroup("Source quality")
@DisplayName("Booker source quality gates")
class SourceQualityTest {
    @Test
    @QaseIgnore
    @Smoke
    @Regression
    @DisplayName("[Source Quality] Verify that Java sources remain free of comments and debt markers")
    void shouldKeepJavaSourcesFreeOfCommentsAndDebtMarkers() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path sourceRoot : List.of(
                moduleDirectory().resolve("src/main/java"),
                moduleDirectory().resolve("src/test/java"))) {
            try (var paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !path.getFileName().toString().equals("SourceQualityTest.java"))
                        .forEach(path -> inspectJavaSource(path, violations));
            }
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
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Java source: " + path, exception);
        }
    }

    private static Path moduleDirectory() {
        String directory = System.getProperty("booker.module.directory");
        if (directory == null || directory.isBlank()) {
            throw new IllegalStateException("Missing booker.module.directory system property");
        }
        return Path.of(directory);
    }
}
