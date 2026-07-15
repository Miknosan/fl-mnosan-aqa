package io.testautomation.demoqa.platform.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.testautomation.demoqa.platform.config.DemoQaConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class PlaywrightSession implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final boolean tracingEnabled;
    private boolean closed;
    private boolean tracing;

    private PlaywrightSession(Playwright playwright, Browser browser, BrowserContext context, Page page,
                              boolean tracingEnabled) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
        this.tracingEnabled = tracingEnabled;
        this.tracing = tracingEnabled;
    }

    public static PlaywrightSession open(DemoQaConfig config) {
        Objects.requireNonNull(config, "config");
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.headless()).setSlowMo(config.slowMotionMillis());
        Browser browser = switch (BrowserEngine.from(config.browser())) {
            case CHROMIUM -> playwright.chromium().launch(options);
            case FIREFOX -> playwright.firefox().launch(options);
            case WEBKIT -> playwright.webkit().launch(options);
        };
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(config.viewportWidth(), config.viewportHeight())
                .setLocale(config.locale())
                .setTimezoneId(config.timezone())
                .setAcceptDownloads(true));
        context.setDefaultTimeout(config.timeout().toMillis());
        context.setDefaultNavigationTimeout(config.timeout().toMillis());
        if (config.blockThirdPartyContent()) {
            context.route(url -> isBlockedThirdPartyUrl(url), route -> route.abort());
        }
        if (config.traceEnabled()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }
        return new PlaywrightSession(playwright, browser, context, context.newPage(), config.traceEnabled());
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

    public List<String> browserErrors() {
        ensureOpen();
        List<String> consoleErrors = page.consoleMessages().stream()
                .filter(message -> "error".equalsIgnoreCase(message.type()))
                .map(message -> "console: " + message.text())
                .toList();
        List<String> pageErrors = page.pageErrors().stream()
                .map(message -> "page: " + message)
                .toList();
        return java.util.stream.Stream.concat(consoleErrors.stream(), pageErrors.stream()).toList();
    }

    public void saveTrace(Path path) {
        ensureOpen();
        if (tracing) {
            context.tracing().stop(new Tracing.StopOptions().setPath(path));
            tracing = false;
        }
    }

    public void discardTrace() {
        ensureOpen();
        if (tracing) {
            context.tracing().stop();
            tracing = false;
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                if (tracingEnabled && tracing) {
                    context.tracing().stop();
                    tracing = false;
                }
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

    private static boolean isBlockedThirdPartyUrl(String url) {
        return List.of(
                        "doubleclick.net",
                        "googlesyndication.com",
                        "google-analytics.com",
                        "adservice.google.com")
                .stream()
                .anyMatch(url::contains);
    }
}
