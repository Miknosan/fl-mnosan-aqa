package io.testautomation.demoqa.framework.metadata;

import io.testautomation.demoqa.framework.lifecycle.DemoQaExtension;
import io.testautomation.core.classification.TestTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(DemoQaTags.DOMAIN)
@Tag(TestTags.BUSINESS)
@ExtendWith(DemoQaExtension.class)
public @interface DemoQa {
}
