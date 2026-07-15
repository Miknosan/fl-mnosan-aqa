package io.testautomation.hygraph.system.graphql.document;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.SystemTest;
import io.testautomation.hygraph.graphql.document.GraphQlDocumentLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SystemTest
class GraphQlDocumentLoaderTest {
    @Test
    @QaseIgnore
    void shouldLoadAndCacheNonBlankGraphQlDocument() {
        String path = "graphql/movie-catalog/movies-page.graphql";

        String first = GraphQlDocumentLoader.load(path);
        String second = GraphQlDocumentLoader.load(path);

        assertThat(first).isNotBlank().isSameAs(second);
    }

    @Test
    @QaseIgnore
    void shouldRejectBlankAndMissingGraphQlDocumentPaths() {
        assertThatThrownBy(() -> GraphQlDocumentLoader.load(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
        assertThatThrownBy(() -> GraphQlDocumentLoader.load("graphql/missing.graphql"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }
}
