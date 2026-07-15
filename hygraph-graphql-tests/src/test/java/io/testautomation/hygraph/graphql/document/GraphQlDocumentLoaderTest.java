package io.testautomation.hygraph.graphql.document;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("GraphQL documents")
class GraphQlDocumentLoaderTest {
    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldLoadAndCacheNonBlankGraphQlDocument() {
        String path = "graphql/movie-catalog/movies-page.graphql";

        String first = GraphQlDocumentLoader.load(path);
        String second = GraphQlDocumentLoader.load(path);

        assertThat(first).isNotBlank().isSameAs(second);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldRejectBlankAndMissingGraphQlDocumentPaths() {
        assertThatThrownBy(() -> GraphQlDocumentLoader.load(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
        assertThatThrownBy(() -> GraphQlDocumentLoader.load("graphql/missing.graphql"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }
}
