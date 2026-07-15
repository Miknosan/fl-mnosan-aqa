package io.testautomation.demoqa.webtables.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.testautomation.demoqa.platform.page.BasePage;
import io.testautomation.demoqa.webtables.component.RecordFormModal;
import io.testautomation.demoqa.webtables.component.WebTableComponent;
import io.testautomation.demoqa.webtables.model.WebTableColumn;
import io.testautomation.demoqa.webtables.model.WebTableRecord;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public final class WebTablesPage extends BasePage {
    private static final String PATH = "/webtables";
    private final Locator addRecord;
    private final Locator search;
    private final RecordFormModal recordForm;
    private final WebTableComponent table;

    public WebTablesPage(Page page, URI baseUrl) {
        super(page, baseUrl);
        addRecord = page.locator("#addNewRecordButton");
        search = page.locator("#searchBox");
        recordForm = new RecordFormModal(page);
        table = new WebTableComponent(page);
    }

    public WebTablesPage open() {
        navigate(PATH);
        waitUntilVisible(".web-tables-wrapper");
        return this;
    }

    public void add(WebTableRecord record) {
        addRecord.click();
        recordForm.waitUntilOpen();
        recordForm.complete(record);
        recordForm.submit();
        table.waitUntilRecordIsPresent(record.email());
    }

    public WebTableRecord openForEditing(String email) {
        table.edit(email);
        recordForm.waitUntilOpen();
        return recordForm.currentRecord();
    }

    public void submitChanges(WebTableRecord record) {
        recordForm.complete(record);
        recordForm.submit();
        table.waitUntilRecordIsPresent(record.email());
    }

    public void delete(String email) {
        table.delete(email);
        table.waitUntilRecordIsAbsent(email);
    }

    public void search(String value) {
        search.fill(value);
    }

    public void clearSearch() {
        search.fill("");
    }

    public List<WebTableRecord> records() {
        return table.records();
    }

    public Optional<WebTableRecord> recordByEmail(String email) {
        return table.recordByEmail(email);
    }

    public int recordCountByEmail(String email) {
        return table.recordCountByEmail(email);
    }

    public void sort(WebTableColumn column) {
        table.sort(column);
    }

    public List<String> values(WebTableColumn column) {
        return table.values(column);
    }
}
