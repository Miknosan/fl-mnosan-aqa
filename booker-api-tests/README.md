# Booker REST API Module

This module owns REST Assured automation for the Booker Authentication and Booking features.

## Package ownership

```text
src/main/java/io/testautomation/booker/
├── authentication/
│   ├── client/          authentication API operations
│   ├── model/           authentication request and response contracts
│   └── TokenProvider    authenticated token lifecycle
├── booking/
│   ├── client/          booking API operations
│   ├── data/            booking data generation
│   ├── model/           booking domain contracts
│   └── workflow/        reusable booking setup and cleanup
└── config/              typed Booker runtime configuration

src/test/java/io/testautomation/booker/
├── features/                            product feature automation
│   ├── authentication/
│   │   ├── scenarios/                   Qase authentication scenarios
│   │   └── AuthenticationFeature        feature metadata
│   └── booking/
│       ├── scenarios/                   Qase booking scenarios
│       └── BookingFeature               feature metadata
├── framework/
│   ├── lifecycle/                       dependency resolution and cleanup
│   ├── metadata/                        domain, feature, and reporting annotations
│   └── reporting/                       quality-gate Allure hierarchy
└── system/                              architecture, metadata, and source-quality tests
```

The test tree has three explicit entry points. `features/*/scenarios` contains executable business scenarios mapped to Qase. `system` contains framework-level quality tests that do not represent Booker behavior. `framework` contains reusable execution infrastructure and no executable test scenarios.

## System quality gates

System tests remain visible in Allure under `Booker REST API / Framework Quality Gates` and are excluded from Qase reporting.

| Allure group | Purpose |
|---|---|
| Architecture | Protects package ownership, dependency direction, test placement, and cycle-free boundaries |
| Test Documentation Integrity | Keeps automated scenario metadata, execution plans, display names, and Qase IDs synchronized |
| Source quality | Prevents source comments and debt markers from entering the module |

## Extension rules

- Put Qase-mapped product tests only in `features.<feature>.scenarios`.
- Put framework-level executable quality checks only in `system`.
- Keep `framework` free of executable test scenarios.
- Keep API clients free of assertions and test-framework dependencies.
- Generate scenario data through domain data factories.
- Use workflows for reusable setup and guaranteed cleanup.
- Add one feature annotation, one report group, one test plan, and one Qase ID to every product scenario.
- Do not introduce operation packages that contain only one test class.

Architecture gates enforce package placement, domain boundaries, dependency direction, Qase synchronization, and source quality.

## Configuration

Select the execution target with `-Dtest.environment=dev` or `TEST_ENVIRONMENT=dev`. The central `config/environments/<environment>.properties` profile owns `booker.base-url`; this module owns Booker technical defaults. `BOOKER_USERNAME` and `BOOKER_PASSWORD` supply environment-specific credentials without storing secrets in a profile. JVM system properties override OS environment variables, which override the selected profile and module defaults.

Every Allure result includes the selected environment. Missing, unknown, or mismatched profiles fail before the Booker clients are created.
