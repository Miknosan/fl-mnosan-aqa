package io.testautomation.demoqa.features.webtables;

import io.testautomation.demoqa.webtables.model.WebTableColumn;
import io.testautomation.demoqa.webtables.model.WebTableRecord;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class WebTableAssertions {
    private WebTableAssertions() {
    }

    public static void assertSingleRecord(List<WebTableRecord> records, WebTableRecord expected) {
        assertThat(records).filteredOn(record -> record.email().equals(expected.email()))
                .containsExactly(expected);
    }

    public static void assertSorted(List<String> values, WebTableColumn column, boolean ascending) {
        if (column.numeric()) {
            List<Integer> numericValues = values.stream().map(Integer::valueOf).toList();
            Comparator<Integer> comparator = Comparator.naturalOrder();
            assertThat(numericValues).isSortedAccordingTo(ascending ? comparator : comparator.reversed());
        } else {
            Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
            assertThat(values).isSortedAccordingTo(ascending ? comparator : comparator.reversed());
        }
    }
}
