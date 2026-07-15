package io.testautomation.hygraph.framework.metadata;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@HygraphFeature("GraphQL Platform")
@Tag(HygraphTags.GRAPHQL_PLATFORM)
public @interface GraphQlPlatformFeature {
}
