package io.testautomation.demoqa.features.registration;

import io.testautomation.demoqa.registration.data.StudentRegistrationDataFactory;
import io.testautomation.demoqa.registration.page.StudentRegistrationPage;
import io.testautomation.demoqa.framework.lifecycle.DemoQaExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class RegistrationExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == StudentRegistrationPage.class || type == StudentRegistrationDataFactory.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == StudentRegistrationPage.class) {
            return new StudentRegistrationPage(
                    DemoQaExtension.session(context).page(),
                    DemoQaExtension.config(context).baseUrl());
        }
        if (type == StudentRegistrationDataFactory.class) {
            return new StudentRegistrationDataFactory();
        }
        throw new ParameterResolutionException("Unsupported registration parameter: " + type.getName());
    }
}
