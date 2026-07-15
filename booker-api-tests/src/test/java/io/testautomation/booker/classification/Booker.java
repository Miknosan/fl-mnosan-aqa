package io.testautomation.booker.classification;

import io.testautomation.booker.extensions.BookerExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("booker")
@ExtendWith(BookerExtension.class)
public @interface Booker {
}
