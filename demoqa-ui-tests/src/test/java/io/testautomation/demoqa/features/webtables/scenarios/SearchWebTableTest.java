package io.testautomation.demoqa.features.webtables.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.demoqa.features.webtables.WebTablesFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.webtables.data.WebTableRecordDataFactory;
import io.testautomation.demoqa.webtables.model.WebTableColumn;
import io.testautomation.demoqa.webtables.model.WebTableRecord;
import io.testautomation.demoqa.webtables.page.WebTablesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@WebTablesFeature
@ReportGroup("Search records")
class SearchWebTableTest {
    @Test
    @Regression
    @QaseId(51)
    @DisplayName("[UI] Verify that web table results are filtered when a matching search value is entered")
    void shouldFilterWebTableResultsWhenMatchingSearchValueIsEntered(
            WebTablesPage webTablesPage,
            WebTableRecordDataFactory dataFactory) {
        WebTableRecord target = dataFactory.searchTarget();
        WebTableRecord control = dataFactory.searchControl();

        webTablesPage.open();
        webTablesPage.add(target);
        webTablesPage.add(control);

        for (WebTableColumn column : Arrays.asList(WebTableColumn.values())) {
            webTablesPage.search(target.value(column));
            assertThat(webTablesPage.records()).containsExactly(target);
            webTablesPage.clearSearch();
            assertThat(webTablesPage.records()).contains(target, control);
        }

        webTablesPage.search(dataFactory.unmatchedSearchTerm());
        assertThat(webTablesPage.records()).isEmpty();
    }
}
