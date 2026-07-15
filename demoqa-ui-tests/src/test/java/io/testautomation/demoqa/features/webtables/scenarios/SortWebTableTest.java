package io.testautomation.demoqa.features.webtables.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.demoqa.features.webtables.WebTablesFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.webtables.model.WebTableColumn;
import io.testautomation.demoqa.webtables.model.WebTableRecord;
import io.testautomation.demoqa.webtables.page.WebTablesPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static io.testautomation.demoqa.features.webtables.WebTableAssertions.assertSorted;
import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@WebTablesFeature
@ReportGroup("Sort records")
class SortWebTableTest {
    @ParameterizedTest(name = "Sort column: {0}")
    @EnumSource(WebTableColumn.class)
    @Regression
    @QaseId(52)
    @DisplayName("[UI] Verify that web table records are ordered when a sortable column header is selected")
    void shouldOrderWebTableRecordsWhenSortableColumnHeaderIsSelected(
            WebTableColumn column,
            WebTablesPage webTablesPage) {
        webTablesPage.open();
        List<WebTableRecord> initialRecords = webTablesPage.records();

        webTablesPage.sort(column);
        assertSorted(webTablesPage.values(column), column, true);
        assertThat(webTablesPage.records()).containsExactlyInAnyOrderElementsOf(initialRecords);

        webTablesPage.sort(column);
        assertSorted(webTablesPage.values(column), column, false);
        assertThat(webTablesPage.records()).containsExactlyInAnyOrderElementsOf(initialRecords);
    }
}
