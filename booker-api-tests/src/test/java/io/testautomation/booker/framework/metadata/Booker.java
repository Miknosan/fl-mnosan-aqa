package io.testautomation.booker.framework.metadata;

import io.testautomation.booker.framework.lifecycle.BookerExtension;
import io.testautomation.core.classification.TestTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(BookerTags.DOMAIN)
@Tag(TestTags.BUSINESS)
@ExtendWith(BookerExtension.class)
public @interface Booker {
}
