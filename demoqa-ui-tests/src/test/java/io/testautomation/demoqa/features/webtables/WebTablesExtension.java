package io.testautomation.demoqa.features.webtables;

import io.testautomation.demoqa.webtables.data.WebTableRecordDataFactory;
import io.testautomation.demoqa.framework.lifecycle.DemoQaExtension;
import io.testautomation.demoqa.webtables.page.WebTablesPage;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class WebTablesExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == WebTablesPage.class || type == WebTableRecordDataFactory.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == WebTablesPage.class) {
            return new WebTablesPage(
                    DemoQaExtension.session(context).page(),
                    DemoQaExtension.config(context).baseUrl());
        }
        if (type == WebTableRecordDataFactory.class) {
            return new WebTableRecordDataFactory();
        }
        throw new ParameterResolutionException("Unsupported Web Tables parameter: " + type.getName());
    }
}
