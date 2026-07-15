package io.testautomation.demoqa.registration.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.time.LocalDate;

public final class DatePickerComponent {
    private final Locator input;
    private final Locator year;
    private final Locator month;
    private final Page page;

    public DatePickerComponent(Page page) {
        this.page = page;
        input = page.locator("#dateOfBirthInput");
        year = page.locator(".react-datepicker__year-select");
        month = page.locator(".react-datepicker__month-select");
    }

    public void select(LocalDate date) {
        input.click();
        year.selectOption(Integer.toString(date.getYear()));
        month.selectOption(Integer.toString(date.getMonthValue() - 1));
        page.locator(String.format(
                        ".react-datepicker__day--%03d:not(.react-datepicker__day--outside-month)",
                        date.getDayOfMonth()))
                .click();
    }
}
