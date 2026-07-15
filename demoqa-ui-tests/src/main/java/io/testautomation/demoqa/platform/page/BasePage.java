package io.testautomation.demoqa.platform.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.WaitForSelectorState;

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
        page.navigate(
                baseUrl.resolve(relativePath).toString(),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    protected void waitUntilVisible(String selector) {
        page.locator(selector).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }
}
