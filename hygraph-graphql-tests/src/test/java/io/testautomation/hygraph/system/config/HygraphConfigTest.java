package io.testautomation.hygraph.system.config;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.SystemTest;
import io.testautomation.hygraph.config.HygraphConfig;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SystemTest
class HygraphConfigTest {
    @Test
    @QaseIgnore
    void shouldAcceptAbsoluteHttpEndpointAndPositiveTimeout() {
        assertThatNoException().isThrownBy(
                () -> new HygraphConfig(URI.create("https://example.test/graphql"), Duration.ofSeconds(5)));
    }

    @Test
    @QaseIgnore
    void shouldRejectUnsupportedEndpointAndNonPositiveTimeout() {
        assertThatThrownBy(() -> new HygraphConfig(URI.create("file:///schema"), Duration.ofSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTP or HTTPS");
        assertThatThrownBy(() -> new HygraphConfig(URI.create("https://example.test/graphql"), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
