package io.testautomation.demoqa.webtables.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.testautomation.demoqa.webtables.model.WebTableRecord;

public final class RecordFormModal {
    private final Locator modal;
    private final Locator firstName;
    private final Locator lastName;
    private final Locator email;
    private final Locator age;
    private final Locator salary;
    private final Locator department;
    private final Locator submit;

    public RecordFormModal(Page page) {
        modal = page.locator(".modal-content");
        firstName = modal.locator("#firstName");
        lastName = modal.locator("#lastName");
        email = modal.locator("#userEmail");
        age = modal.locator("#age");
        salary = modal.locator("#salary");
        department = modal.locator("#department");
        submit = modal.locator("#submit");
    }

    public void waitUntilOpen() {
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void complete(WebTableRecord record) {
        firstName.fill(record.firstName());
        lastName.fill(record.lastName());
        email.fill(record.email());
        age.fill(Integer.toString(record.age()));
        salary.fill(Integer.toString(record.salary()));
        department.fill(record.department());
    }

    public WebTableRecord currentRecord() {
        return new WebTableRecord(
                firstName.inputValue(),
                lastName.inputValue(),
                Integer.parseInt(age.inputValue()),
                email.inputValue(),
                Integer.parseInt(salary.inputValue()),
                department.inputValue());
    }

    public void submit() {
        submit.click();
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
