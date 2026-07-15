package io.testautomation.demoqa.registration.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.testautomation.demoqa.registration.model.RegistrationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RegistrationResultModal {
    private final Locator modal;
    private final Locator rows;

    public RegistrationResultModal(Page page) {
        modal = page.locator(".modal-content");
        rows = modal.locator("tbody tr");
    }

    public boolean isVisible() {
        return modal.isVisible();
    }

    public RegistrationResult result() {
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        Map<String, String> values = new LinkedHashMap<>();
        for (Locator row : rows.all()) {
            List<String> cells = row.locator("td").allTextContents();
            if (cells.size() == 2) {
                values.put(cells.get(0).trim(), cells.get(1).trim());
            }
        }
        return new RegistrationResult(values);
    }

    public String title() {
        return modal.locator("#example-modal-sizes-title-lg").innerText().trim();
    }
}
