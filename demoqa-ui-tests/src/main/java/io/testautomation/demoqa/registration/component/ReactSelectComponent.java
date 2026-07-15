package io.testautomation.demoqa.registration.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class ReactSelectComponent {
    private final Locator input;
    private final Page page;

    public ReactSelectComponent(Page page, String inputSelector) {
        this.page = page;
        input = page.locator(inputSelector);
    }

    public void select(String value) {
        input.fill(value);
        page.getByText(value, new Page.GetByTextOptions().setExact(true)).click();
    }
}
