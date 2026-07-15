package io.testautomation.booker.framework.reporting;

import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.reporting.QualityGateReportExtension;

public final class BookerQualityReportExtension extends QualityGateReportExtension {
    @Override
    protected String domainName() {
        return "Booker REST API";
    }

    @Override
    protected String qualityGateGroup(Class<?> testClass) {
        ReportGroup reportGroup = testClass.getAnnotation(ReportGroup.class);
        if (reportGroup == null) {
            throw new IllegalStateException(
                    "Booker quality gate must declare @ReportGroup: " + testClass.getName());
        }
        return reportGroup.value();
    }
}
