package io.testautomation.core.http;

import io.testautomation.core.json.ObjectMapperFactory;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory;
import io.restassured.specification.RequestSpecification;

import java.net.URI;
import java.time.Duration;

import static io.restassured.config.RestAssuredConfig.config;

public final class RestSpecFactory {
    private RestSpecFactory() {
    }

    public static RequestSpecification create(URI baseUri, Duration timeout) {
        Jackson2ObjectMapperFactory mapperFactory = (type, charset) -> ObjectMapperFactory.get();
        int timeoutMillis = Math.toIntExact(timeout.toMillis());

        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .setAccept("application/json")
                .setConfig(config()
                        .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                                .jackson2ObjectMapperFactory(mapperFactory))
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", timeoutMillis)
                                .setParam("http.socket.timeout", timeoutMillis)))
                .addFilter(new AllureRestAssured())
                .build();
    }
}
