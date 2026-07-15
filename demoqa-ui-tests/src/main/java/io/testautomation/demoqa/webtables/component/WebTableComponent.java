package io.testautomation.demoqa.webtables.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.testautomation.demoqa.webtables.model.WebTableColumn;
import io.testautomation.demoqa.webtables.model.WebTableRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WebTableComponent {
    private final Page page;
    private final Locator dataRows;

    public WebTableComponent(Page page) {
        this.page = page;
        dataRows = page.locator(".web-tables-wrapper tbody tr");
    }

    public List<WebTableRecord> records() {
        List<WebTableRecord> records = new ArrayList<>();
        for (Locator row : dataRows.all()) {
            List<String> cells = row.locator("td").allTextContents().stream()
                    .map(String::trim)
                    .toList();
            if (cells.size() >= 6 && !cells.get(0).isBlank()) {
                records.add(new WebTableRecord(
                        cells.get(0),
                        cells.get(1),
                        Integer.parseInt(cells.get(2)),
                        cells.get(3),
                        Integer.parseInt(cells.get(4)),
                        cells.get(5)));
            }
        }
        return List.copyOf(records);
    }

    public Optional<WebTableRecord> recordByEmail(String email) {
        return records().stream().filter(record -> record.email().equals(email)).findFirst();
    }

    public int recordCountByEmail(String email) {
        return Math.toIntExact(records().stream().filter(record -> record.email().equals(email)).count());
    }

    public void edit(String email) {
        uniqueRow(email).locator("[id^='edit-record-']").click();
    }

    public void delete(String email) {
        uniqueRow(email).locator("[id^='delete-record-']").click();
    }

    public void sort(WebTableColumn column) {
        header(column).click();
    }

    public List<String> values(WebTableColumn column) {
        return records().stream().map(record -> record.value(column)).toList();
    }

    public void waitUntilRecordIsPresent(String email) {
        matchingRow(email).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitUntilRecordIsAbsent(String email) {
        matchingRow(email).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private Locator uniqueRow(String email) {
        Locator row = matchingRow(email);
        row.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        int count = row.count();
        if (count != 1) {
            throw new IllegalStateException("Expected one row for email " + email + " but found " + count);
        }
        return row;
    }

    private Locator header(WebTableColumn column) {
        Locator header = page.locator(".web-tables-wrapper thead th").nth(column.index());
        header.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        String actualHeader = header.innerText().trim();
        if (!actualHeader.equals(column.header())) {
            throw new IllegalStateException(
                    "Expected header " + column.header() + " but found " + actualHeader);
        }
        return header;
    }

    private Locator matchingRow(String email) {
        return dataRows.filter(new Locator.FilterOptions()
                .setHasText(email));
    }
}
