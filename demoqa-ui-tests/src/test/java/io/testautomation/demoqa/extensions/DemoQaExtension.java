package io.testautomation.demoqa.extensions;

import com.microsoft.playwright.Page;
import io.testautomation.demoqa.config.DemoQaConfig;
import io.testautomation.demoqa.session.UiSession;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DemoQaExtension implements BeforeEachCallback, AfterTestExecutionCallback,
        AfterEachCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(DemoQaExtension.class);
    private static final String SESSION = "demoqa-session";

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(NAMESPACE).put(SESSION, UiSession.open(DemoQaConfig.load()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        UiSession session = session(context);
        Path directory = DemoQaConfig.load().artifactsDirectory().resolve("screenshots");
        Path screenshot = directory.resolve(context.getUniqueId().replaceAll("[^a-zA-Z0-9._-]", "_") + ".png");
        try {
            Files.createDirectories(directory);
            byte[] bytes = session.screenshot(screenshot);
            Allure.addAttachment("UI failure screenshot", "image/png", new ByteArrayInputStream(bytes), ".png");
        } catch (IOException | RuntimeException exception) {
            context.publishReportEntry("screenshot-error", exception.getMessage());
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        UiSession session = context.getStore(NAMESPACE).remove(SESSION, UiSession.class);
        if (session != null) {
            session.close();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == Page.class || type == UiSession.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        UiSession session = session(context);
        return parameterContext.getParameter().getType() == Page.class ? session.page() : session;
    }

    private static UiSession session(ExtensionContext context) {
        UiSession session = context.getStore(NAMESPACE).get(SESSION, UiSession.class);
        if (session == null) {
            throw new IllegalStateException("UI session is not initialized");
        }
        return session;
    }
}
