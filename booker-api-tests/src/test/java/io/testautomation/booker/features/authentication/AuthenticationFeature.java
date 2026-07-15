package io.testautomation.booker.features.authentication;

import io.testautomation.booker.framework.metadata.BookerFeature;
import io.testautomation.booker.framework.metadata.BookerTags;
import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@BookerFeature("Authentication")
@Tag(BookerTags.AUTHENTICATION)
public @interface AuthenticationFeature {
}
