package io.testautomation.hygraph.framework.metadata;

import io.testautomation.hygraph.framework.lifecycle.HygraphExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(HygraphTags.DOMAIN)
@ExtendWith(HygraphExtension.class)
public @interface Hygraph {
}
