package io.testautomation.demoqa.webtables.data;

import io.testautomation.demoqa.webtables.model.WebTableRecord;
import net.datafaker.Faker;

import java.util.Locale;
import java.util.UUID;

public final class WebTableRecordDataFactory {
    private final Faker faker = new Faker(Locale.ENGLISH);

    public WebTableRecord newRecord() {
        String token = uniqueToken();
        return new WebTableRecord(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(21, 65),
                "qa.record." + token + "@example.com",
                faker.number().numberBetween(45_000, 150_000),
                faker.job().field());
    }

    public WebTableRecord updatedRecord(WebTableRecord original) {
        return new WebTableRecord(
                original.firstName(),
                "Morgan" + uniqueToken(),
                Math.min(original.age() + 1, 120),
                original.email(),
                original.salary() + 7_000,
                "QA" + uniqueToken());
    }

    public WebTableRecord searchTarget() {
        String token = uniqueToken();
        return new WebTableRecord(
                "Nora" + token,
                "Winters" + token,
                67,
                "nora.winters." + token + "@example.com",
                987_654,
                "PlatformQuality" + token);
    }

    public WebTableRecord searchControl() {
        String token = uniqueToken();
        return new WebTableRecord(
                "Liam" + token,
                "Stone" + token,
                68,
                "liam.stone." + token + "@example.com",
                876_543,
                "CustomerSupport" + token);
    }

    public String unmatchedSearchTerm() {
        return "no-record-" + uniqueToken();
    }

    private static String uniqueToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase(Locale.ROOT);
    }
}
