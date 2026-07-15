package io.testautomation.demoqa.features.webtables.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.demoqa.features.webtables.WebTablesFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.webtables.data.WebTableRecordDataFactory;
import io.testautomation.demoqa.webtables.model.WebTableRecord;
import io.testautomation.demoqa.webtables.page.WebTablesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.demoqa.features.webtables.WebTableAssertions.assertSingleRecord;

@DemoQa
@WebTablesFeature
@ReportGroup("Add record")
class AddWebTableRecordTest {
    @Test
    @Smoke
    @Regression
    @QaseId(48)
    @DisplayName("[UI] Verify that a web table record is added when the registration form contains valid data")
    void shouldAddWebTableRecordWhenRegistrationFormContainsValidData(
            WebTablesPage webTablesPage,
            WebTableRecordDataFactory dataFactory) {
        WebTableRecord record = dataFactory.newRecord();

        webTablesPage.open();
        webTablesPage.add(record);

        assertSingleRecord(webTablesPage.records(), record);
    }
}
