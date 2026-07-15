package io.testautomation.demoqa.features.webtables;

import io.testautomation.demoqa.framework.metadata.DemoQaFeature;
import io.testautomation.demoqa.framework.metadata.DemoQaTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DemoQaFeature("Web Tables")
@Tag(DemoQaTags.WEB_TABLES)
@ExtendWith(WebTablesExtension.class)
public @interface WebTablesFeature {
}
