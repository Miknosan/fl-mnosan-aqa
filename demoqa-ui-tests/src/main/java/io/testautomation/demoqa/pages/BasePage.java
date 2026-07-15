package io.testautomation.demoqa.pages;

import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.Objects;

public abstract class BasePage {
    protected final Page page;
    private final URI baseUrl;

    protected BasePage(Page page, URI baseUrl) {
        this.page = Objects.requireNonNull(page, "page");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    }

    protected void navigate(String relativePath) {
        page.navigate(baseUrl.resolve(relativePath).toString());
    }

    protected void waitUntilLoaded() {
        page.waitForLoadState();
    }
}
