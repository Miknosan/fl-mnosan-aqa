package io.testautomation.demoqa.framework.metadata;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DemoQaFeature("UI Platform")
@Tag(DemoQaTags.UI_PLATFORM)
public @interface UiPlatformFeature {
}
