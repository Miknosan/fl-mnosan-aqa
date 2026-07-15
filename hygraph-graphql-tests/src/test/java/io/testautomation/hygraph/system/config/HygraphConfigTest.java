package io.testautomation.hygraph.system.config;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.config.HygraphConfig;
import io.testautomation.hygraph.framework.metadata.GraphQlPlatformFeature;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Configuration contract")
class HygraphConfigTest {
    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldAcceptAbsoluteHttpEndpointAndPositiveTimeout() {
        assertThatNoException().isThrownBy(
                () -> new HygraphConfig(URI.create("https://example.test/graphql"), Duration.ofSeconds(5)));
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldRejectUnsupportedEndpointAndNonPositiveTimeout() {
        assertThatThrownBy(() -> new HygraphConfig(URI.create("file:///schema"), Duration.ofSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTP or HTTPS");
        assertThatThrownBy(() -> new HygraphConfig(URI.create("https://example.test/graphql"), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
