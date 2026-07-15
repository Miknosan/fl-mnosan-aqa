# QA Automation Test Suite

Java 17 / Maven multi-module automation framework for independent Booker REST API, Hygraph GraphQL, and DemoQA UI domains.

## Architecture

```text
qa-automation-parent/
├── pom.xml
├── qa-core/                 # configuration, JSON, HTTP, and test-plan contracts
├── booker-api-tests/        # REST Assured Booker domain and Qase-mapped tests
├── hygraph-graphql-tests/   # independent Hygraph domain
├── demoqa-ui-tests/         # independent Playwright domain
└── qa-orchestrator/         # validated execution selection only
```

Domain modules do not depend on one another. `qa-core` contains no domain behavior, and `qa-orchestrator` contains no clients, models, workflows, test data, or assertions.

## Prerequisites

- JDK 17+
- Maven 3.6+ (or the committed Maven Wrapper)

```bash
./mvnw --version
```

## Booker tests

The Booker suite implements the 10 test cases stored in Qase suite `DA / Booker`. Tests are linked to their Qase cases with `@QaseId`, while JUnit display names remain human-readable. The mandatory-field case is parameterized across all six required fields, so Maven reports 15 JUnit executions for 10 test methods.

```bash
# All Booker tests
./mvnw -pl booker-api-tests -am test

# Booker smoke plan
./mvnw -pl booker-api-tests -am test -Dgroups=smoke

# Booker regression plan
./mvnw -pl booker-api-tests -am test -Dgroups=regression
```

Booker configuration is stored in `booker-api-tests/src/main/resources/booker.properties`. Values can be overridden by environment variables or Java system properties:

```text
booker.base-url  -> BOOKER_BASE_URL
booker.username  -> BOOKER_USERNAME
booker.password  -> BOOKER_PASSWORD
booker.timeout-ms -> BOOKER_TIMEOUT_MS
```

Tests execute sequentially by default because Restful Booker is a public service. Unique data is generated for each test, and `BookerExtension` cleans up created bookings after every scenario.

## Hygraph tests

The Hygraph suite implements the six cases in Qase suite `DA / Hygraph` against the public Video Streaming schema. It covers movie pagination, variables, single-entity retrieval, fragments and nested fields, not-found behavior, syntax errors, and schema validation.

```bash
# All Hygraph tests
./mvnw -pl hygraph-graphql-tests -am clean test

# Hygraph smoke plan
./mvnw -pl hygraph-graphql-tests -am clean test -Dgroups=smoke

# Hygraph regression plan
./mvnw -pl hygraph-graphql-tests -am clean test -Dgroups=regression
```

Hygraph configuration is stored in `hygraph-graphql-tests/src/main/resources/hygraph.properties` and supports environment-variable or Java-system-property overrides:

```text
hygraph.base-url  -> HYGRAPH_BASE_URL
hygraph.timeout-ms -> HYGRAPH_TIMEOUT_MS
```

The `moviecatalog` package represents the `movie-catalog` feature. Its test packages are grouped by behavior: `listing`, `retrieval`, `relationships`, and `validation`. Queries and fragments are stored under `src/main/resources/graphql/movie-catalog`. Run-scoped test data resolves suitable public entities once and reuses them without global static state. The suite executes sequentially to avoid unnecessary load on the public service.

Reusable GraphQL transport, response contracts, document loading, schema validation, and architecture gates are isolated under the `graphql` and `architecture` packages. Runtime schema introspection validates every executable GraphQL document against the published Hygraph schema. See `hygraph-graphql-tests/README.md` for extension rules.

## Orchestrated execution

The orchestrator validates domain, plan, feature, environment, and parallelism inputs, converts multiple plans to a JUnit logical-OR expression, and invokes only the selected Maven modules.

```bash
# Booker smoke
./mvnw -pl qa-orchestrator compile exec:java \
  -Ddomains=booker \
  -Dplans=smoke

# Booker smoke and regression, filtered to the booking feature
./mvnw -pl qa-orchestrator compile exec:java \
  -Ddomains=booker \
  -Dplans=smoke,regression \
  -Dfeature=booking \
  -Denvironment=public \
  -Dparallelism=1

# Multiple domains
./mvnw -pl qa-orchestrator compile exec:java \
  -Ddomains=booker,hygraph \
  -Dplans=smoke

# Hygraph Movie Catalog feature
./mvnw -pl qa-orchestrator compile exec:java \
  -Ddomains=hygraph \
  -Dplans=regression \
  -Dfeature=movie-catalog
```

Supported domains are `booker`, `hygraph`, `demoqa`, and `all`. Supported plans are `smoke`, `regression`, and `sanity`. Multiple plans use logical OR. Parallelism is validated between 1 and 8; keep it at 1 for public services unless a domain is explicitly designed for concurrency.

## Reports

- Surefire: `<module>/target/surefire-reports`
- Allure: `<module>/target/allure-results`
- DemoQA failure screenshots: `demoqa-ui-tests/target/test-artifacts/screenshots`

### Qase TestOps

Qase reporting is disabled for normal local runs. Booker and Hygraph tests are linked to their Qase cases with `@QaseId`. To create a Qase test run and publish results, provide the API token through the environment and enable TestOps mode:

```bash
QASE_MODE="testops" \
QASE_TESTOPS_API_TOKEN="<your-token>" \
./mvnw -pl booker-api-tests -am clean test
```

Replace `booker-api-tests` with `hygraph-graphql-tests` to publish the Hygraph suite. Set `QASE_TESTOPS_RUN_TITLE` to a domain-specific title for each run.

The non-secret defaults, including project code `DA`, are stored in the root `qase.config.json`, where the reporter can discover them for direct and orchestrated Maven runs. Use `QASE_TESTOPS_RUN_TITLE` to override the run title or `QASE_TESTOPS_RUN_ID` to publish into an existing Qase run. Never store the API token in the repository.

## Full build

```bash
./mvnw clean verify
```
