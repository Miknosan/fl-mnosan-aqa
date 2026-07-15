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

import static io.testautomation.demoqa.features.webtables.WebTableAssertions.assertSingleRecord;
import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@WebTablesFeature
@ReportGroup("Update record")
class UpdateWebTableRecordTest {
    @Test
    @Regression
    @QaseId(49)
    @DisplayName("[UI] Verify that a web table record is updated when valid changes are submitted for an existing record")
    void shouldUpdateWebTableRecordWhenValidChangesAreSubmittedForExistingRecord(
            WebTablesPage webTablesPage,
            WebTableRecordDataFactory dataFactory) {
        WebTableRecord original = dataFactory.newRecord();
        WebTableRecord updated = dataFactory.updatedRecord(original);

        webTablesPage.open();
        webTablesPage.add(original);
        WebTableRecord formRecord = webTablesPage.openForEditing(original.email());
        assertThat(formRecord).isEqualTo(original);
        webTablesPage.submitChanges(updated);

        assertSingleRecord(webTablesPage.records(), updated);
        assertThat(webTablesPage.records()).doesNotContain(original);
    }
}
