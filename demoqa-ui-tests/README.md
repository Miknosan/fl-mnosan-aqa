# DemoQA UI Module

This module owns Playwright automation for the DemoQA Registration Form and Web Tables features. It is isolated from the Booker and Hygraph domains and depends only on `qa-core` for shared cross-domain contracts.

## Package ownership

```text
src/main/java/io/testautomation/demoqa/
├── platform/
│   ├── config/       environment-aware DemoQA configuration
│   ├── page/         shared page-object foundation
│   └── playwright/   Playwright browser and context lifecycle
├── registration/
│   ├── component/    registration-specific UI components
│   ├── data/         registration data generation
│   ├── model/        registration domain contracts
│   └── page/         registration business interactions
└── webtables/
    ├── component/    web-table-specific UI components
    ├── data/         web-table data generation
    ├── model/        web-table domain contracts
    └── page/         web-table business interactions

src/test/java/io/testautomation/demoqa/
├── features/                         product feature automation
│   ├── registration/
│   │   ├── scenarios/                DA-44 through DA-47 business test scenarios
│   │   ├── RegistrationExtension     parameter resolution
│   │   ├── RegistrationFeature       feature metadata
│   │   └── StudentRegistrationAssertions
│   └── webtables/
│       ├── scenarios/                DA-48 through DA-52 business test scenarios
│       ├── WebTablesExtension        parameter resolution
│       ├── WebTablesFeature          feature metadata
│       └── WebTableAssertions
├── framework/
│   ├── lifecycle/                    cross-feature DemoQA execution lifecycle
│   ├── metadata/                     domain and reporting annotations
│   └── reporting/                    quality-gate Allure hierarchy
└── system/                            architecture, metadata, and source-quality tests
```

The test tree has three explicit entry points. `features/*/scenarios` contains executable business scenarios mapped to Qase. `system` contains framework-level quality tests that do not represent product behavior. `framework` contains reusable execution infrastructure and no test scenarios. The production tree uses package-by-feature for business behavior and a separate platform package for Playwright infrastructure.

Page objects expose business operations and never assertions. Components own selectors and UI mechanics. Test data factories own valid and invalid domain data. Feature-specific assertions, metadata, and parameter resolution stay directly above their `scenarios` package. Product tests receive feature abstractions through JUnit parameter resolution and do not access a raw Playwright `Page`.

## System quality gates

System tests remain visible in Allure under `DemoQA UI / Framework Quality Gates` and are excluded from Qase reporting. They use a reporting-only extension and never create a browser or Playwright context.

| Allure group | Purpose |
|---|---|
| Architecture | Protects page, component, platform, feature, and executable-test boundaries |
| Test Documentation Integrity | Keeps automated scenario metadata, execution plans, display names, and Qase IDs synchronized |
| Source quality | Prevents source comments and debt markers from entering the module |

## Test scope

| Qase ID | Automated behavior |
|---|---|
| DA-44 | Submit a complete student registration |
| DA-45 | Validate every mandatory registration field |
| DA-46 | Reject an invalid email format |
| DA-47 | Reject a nine-digit mobile number |
| DA-48 | Add a web-table record |
| DA-49 | Update an existing web-table record |
| DA-50 | Delete an existing web-table record |
| DA-51 | Search across all business columns |
| DA-52 | Sort all sortable business columns in both directions |

DA-44 and DA-48 form the smoke plan. All product scenarios form the regression plan. Architecture and metadata gates verify package boundaries, test naming, Qase coverage, report classification, and the absence of source comments or debt markers.

## Known product defects

### DA-52: Web Table headers do not sort records

DA-52 is intentionally retained as an active defect-detecting regression test. The expected behavior is ascending ordering after the first selection of a sortable business-column header and descending ordering after the second selection. In the current DemoQA Web Tables implementation, selecting any business-column header does not change the record order and the rendered headers expose no sorting state.

The test is parameterized across First Name, Last Name, Age, Email, Salary, and Department. Consequently, one Qase case produces six failed JUnit executions when the defect is present. These failures are deterministic and represent the same product defect rather than six independent test cases or an automation instability.

Playwright completes each header click successfully, reads the unchanged table state, and records a screenshot and trace for every failed execution under `target/test-artifacts/<environment>`. The test remains enabled so the regression report transparently communicates the observed product behavior instead of producing an artificially green result.

## Configuration

Select the execution target with `-Dtest.environment=dev` or `TEST_ENVIRONMENT=dev`. The central `config/environments/<environment>.properties` profile owns `demoqa.base-url`; browser and evidence defaults are stored in `src/main/resources/demoqa.properties`. Configuration precedence is Java system property, environment variable, selected profile, then module default.

```text
demoqa.base-url                    DEMOQA_BASE_URL
demoqa.browser                     DEMOQA_BROWSER
demoqa.headless                    DEMOQA_HEADLESS
demoqa.slow-mo-ms                  DEMOQA_SLOW_MO_MS
demoqa.timeout-ms                  DEMOQA_TIMEOUT_MS
demoqa.viewport-width              DEMOQA_VIEWPORT_WIDTH
demoqa.viewport-height             DEMOQA_VIEWPORT_HEIGHT
demoqa.artifacts-directory         DEMOQA_ARTIFACTS_DIRECTORY
demoqa.locale                      DEMOQA_LOCALE
demoqa.timezone                    DEMOQA_TIMEZONE
demoqa.trace-enabled               DEMOQA_TRACE_ENABLED
demoqa.block-third-party-content   DEMOQA_BLOCK_THIRD_PARTY_CONTENT
```

Supported browsers are `chromium`, `firefox`, and `webkit`. Each test receives a new context with a deterministic locale, timezone, viewport, timeout policy, and isolated browser state.

Every Allure result includes the selected environment. Missing, unknown, or mismatched profiles fail before Playwright is started.

## Extension rules

- Put selectors only in feature pages or components.
- Represent submitted and observed data with immutable domain models.
- Generate scenario data through the feature data factory.
- Keep feature assertions, extensions, metadata, and tests inside their feature package.
- Put Qase-mapped product tests only in `features.<feature>.scenarios`.
- Put framework-level executable quality checks only in `system`.
- Keep `framework` free of executable test scenarios.
- Add one exact Qase ID and a `[UI] Verify that ... when ...` display name to every product test.
- Classify each product test by domain, feature, report group, and execution plan.
- Keep platform independent from business features and business features independent from each other.
- Do not introduce operation packages that contain only one test class.
- Promote a feature component to platform only after another feature becomes a real consumer.
- Do not introduce generic `common`, `helpers`, or `utils` packages.

Failure screenshots and traces are stored under `target/test-artifacts/<environment>`. Allure and Surefire outputs are stored under their standard module `target` directories.
