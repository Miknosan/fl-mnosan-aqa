package io.testautomation.demoqa.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.testautomation.demoqa.config.DemoQaConfig;

import java.nio.file.Path;
import java.util.Objects;

public final class UiSession implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private boolean closed;

    private UiSession(Playwright playwright, Browser browser, BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    public static UiSession open(DemoQaConfig config) {
        Objects.requireNonNull(config, "config");
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.headless()).setSlowMo(config.slowMotionMillis());
        Browser browser = switch (BrowserTypeName.from(config.browser())) {
            case CHROMIUM -> playwright.chromium().launch(options);
            case FIREFOX -> playwright.firefox().launch(options);
            case WEBKIT -> playwright.webkit().launch(options);
        };
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(config.viewportWidth(), config.viewportHeight()));
        context.setDefaultTimeout(config.timeout().toMillis());
        context.setDefaultNavigationTimeout(config.timeout().toMillis());
        return new UiSession(playwright, browser, context, context.newPage());
    }

    public Page page() {
        ensureOpen();
        return page;
    }

    public byte[] screenshot(Path path) {
        ensureOpen();
        Page.ScreenshotOptions options = new Page.ScreenshotOptions().setFullPage(true);
        if (path != null) {
            options.setPath(path);
        }
        return page.screenshot(options);
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                context.close();
            } finally {
                try {
                    browser.close();
                } finally {
                    playwright.close();
                }
            }
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("UI session is already closed");
        }
    }
}
