package io.testautomation.demoqa.framework.reporting;

import io.testautomation.core.reporting.QualityGateReportExtension;
import io.testautomation.demoqa.framework.metadata.ReportGroup;

public final class DemoQaQualityReportExtension extends QualityGateReportExtension {
    @Override
    protected String domainName() {
        return "DemoQA UI";
    }

    @Override
    protected String qualityGateGroup(Class<?> testClass) {
        ReportGroup reportGroup = testClass.getAnnotation(ReportGroup.class);
        if (reportGroup == null) {
            throw new IllegalStateException(
                    "DemoQA quality gate must declare @ReportGroup: " + testClass.getName());
        }
        return reportGroup.value();
    }
}
