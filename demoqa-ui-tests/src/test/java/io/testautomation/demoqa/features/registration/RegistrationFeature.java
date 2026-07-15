package io.testautomation.demoqa.features.registration;

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
@DemoQaFeature("Student Registration")
@Tag(DemoQaTags.REGISTRATION)
@ExtendWith(RegistrationExtension.class)
public @interface RegistrationFeature {
}
