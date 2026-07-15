# Hygraph GraphQL module

This module keeps GraphQL protocol infrastructure independent from Hygraph business features.

## Package responsibilities

- `config` owns typed Hygraph runtime configuration.
- `graphql.client` executes GraphQL over HTTP and preserves diagnostic metadata.
- `graphql.document` loads and caches external GraphQL documents.
- `graphql.model` represents protocol requests, responses, errors, and transport results.
- `moviecatalog.client` exposes typed Movie Catalog operations.
- `moviecatalog.model` represents Movie Catalog response data.
- `moviecatalog.data` resolves and caches run-scoped test preconditions.
- `assertions` separates protocol assertions from Movie Catalog assertions.
- `architecture` enforces package boundaries, metadata, Qase IDs, source quality, and document syntax.

## Adding a feature

1. Add a feature package with explicit `client`, `model`, and only the additional packages required by its behavior.
2. Store its operations under `src/main/resources/graphql/<feature-tag>`.
3. Create a controlled feature annotation meta-annotated with `@HygraphFeature`, `@Tag`, and its feature-specific JUnit extension.
4. Keep feature-client and test-data injection in that feature extension.
5. Group tests by behavior and apply `@Hygraph`, the feature annotation, `@ReportGroup`, a test-plan annotation, and a Qase ID where the test represents a managed Qase case.
6. Keep the generic `HygraphExtension` and `graphql` package unchanged.

## Quality gates

The module validates GraphQL document syntax locally and validates executable documents against the published schema through introspection. ArchUnit prevents protocol-to-feature coupling, client dependencies on test frameworks, and package cycles. Metadata checks enforce one feature per Movie Catalog test, required plans and report groups, and unique Qase IDs.

Use `clean test` or `clean verify` when generating reports so renamed or removed tests cannot leave stale Surefire or Allure artifacts.
