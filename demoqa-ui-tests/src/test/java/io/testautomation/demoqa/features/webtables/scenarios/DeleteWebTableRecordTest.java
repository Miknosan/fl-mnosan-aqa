package io.testautomation.demoqa.features.webtables.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.demoqa.features.webtables.WebTablesFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.webtables.data.WebTableRecordDataFactory;
import io.testautomation.demoqa.webtables.model.WebTableRecord;
import io.testautomation.demoqa.webtables.page.WebTablesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@WebTablesFeature
@ReportGroup("Delete record")
class DeleteWebTableRecordTest {
    @Test
    @Regression
    @QaseId(50)
    @DisplayName("[UI] Verify that a web table record is removed when delete is selected for an existing record")
    void shouldRemoveWebTableRecordWhenDeleteIsSelectedForExistingRecord(
            WebTablesPage webTablesPage,
            WebTableRecordDataFactory dataFactory) {
        WebTableRecord record = dataFactory.newRecord();

        webTablesPage.open();
        webTablesPage.add(record);
        List<WebTableRecord> recordsBeforeDelete = webTablesPage.records();
        webTablesPage.delete(record.email());
        List<WebTableRecord> recordsAfterDelete = webTablesPage.records();

        assertThat(webTablesPage.recordByEmail(record.email())).isEmpty();
        assertThat(recordsAfterDelete).containsExactlyInAnyOrderElementsOf(
                recordsBeforeDelete.stream().filter(existing -> !existing.email().equals(record.email())).toList());
    }
}
