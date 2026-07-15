# Hygraph GraphQL module

This module keeps GraphQL protocol infrastructure independent from Hygraph business features.

## Package ownership

```text
src/main/java/io/testautomation/hygraph/
├── config/              typed Hygraph runtime configuration
├── graphql/
│   ├── client/          generic GraphQL transport
│   ├── document/        external document loading and caching
│   └── model/           protocol requests, responses, errors, and results
└── moviecatalog/
    ├── client/          typed Movie Catalog operations
    └── model/           Movie Catalog response data

src/test/java/io/testautomation/hygraph/
├── features/
│   └── moviecatalog/
│       ├── scenarios/               Qase Movie Catalog scenarios
│       ├── MovieCatalogExtension    feature dependency resolution
│       ├── MovieCatalogFeature      feature metadata
│       ├── MovieCatalogTestData     run-scoped feature preconditions
│       └── MovieAssertions          feature assertions
├── framework/
│   ├── assertions/                  reusable GraphQL assertions
│   ├── lifecycle/                   Hygraph execution lifecycle
│   └── metadata/                    domain, platform, feature, and reporting annotations
└── system/
    ├── config/                      configuration contract tests
    ├── graphql/                     transport, document, model, and schema tests
    ├── HygraphArchitectureTest
    ├── HygraphTestMetadataTest
    └── SourceQualityTest
```

The test tree has three explicit entry points. `features/*/scenarios` contains executable business scenarios mapped to Qase. `system` contains local GraphQL platform, architecture, metadata, and source-quality checks. The published-schema introspection check uses the separate `external-contract` classification. `framework` contains reusable test infrastructure and no executable scenarios.

## Adding a feature

1. Add a feature package with explicit `client`, `model`, and only the additional packages required by its behavior.
2. Store its operations under `src/main/resources/graphql/<feature-tag>`.
3. Create a controlled feature annotation meta-annotated with `@HygraphFeature`, `@Tag`, and its feature-specific JUnit extension.
4. Keep feature-client and test-data injection directly inside the feature package.
5. Put product scenarios in `features.<feature>.scenarios` and apply `@Hygraph`, the feature annotation, `@ReportGroup`, a test-plan annotation, and a Qase ID.
6. Keep the generic `HygraphExtension` and `graphql` package unchanged.

## Quality gates

The automatic pull-request gate validates GraphQL document syntax locally and runs architecture, metadata, configuration, model, and loopback transport checks without calling the public Hygraph service. Published-schema introspection remains an explicit `external-contract` execution. Metadata checks enforce one feature per Movie Catalog test, required plans and report groups, unique Qase IDs, and Qase exclusion for non-business tests.

```bash
./mvnw -pl hygraph-graphql-tests -am clean test \
  -Dtest.environment=dev \
  -Dgroups=external-contract
```

Use `clean test` or `clean verify` when generating reports so renamed or removed tests cannot leave stale Surefire or Allure artifacts.

## Configuration

Select the execution target with `-Dtest.environment=dev` or `TEST_ENVIRONMENT=dev`. The central `config/environments/<environment>.properties` profile owns `hygraph.base-url`; this module owns GraphQL technical defaults. JVM system properties override OS environment variables, which override the selected profile and module defaults.

Every Allure result includes the selected environment. Missing, unknown, or mismatched profiles fail before the GraphQL client is created.
