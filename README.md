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
```

Supported domains are `booker`, `hygraph`, `demoqa`, and `all`. Supported plans are `smoke`, `regression`, and `sanity`. Multiple plans use logical OR. Parallelism is validated between 1 and 8; keep it at 1 for public services unless a domain is explicitly designed for concurrency.

## Reports

- Surefire: `<module>/target/surefire-reports`
- Allure: `<module>/target/allure-results`
- DemoQA failure screenshots: `demoqa-ui-tests/target/test-artifacts/screenshots`

### Qase TestOps

Qase reporting is disabled for normal local runs. To create a Qase test run and publish the Booker results, provide the API token through the environment and enable TestOps mode:

```bash
QASE_MODE="testops" \
QASE_TESTOPS_API_TOKEN="<your-token>" \
./mvnw -pl booker-api-tests -am clean test
```

The non-secret defaults, including project code `DA`, are stored in the root `qase.config.json`, where the reporter can discover them for direct and orchestrated Maven runs. Use `QASE_TESTOPS_RUN_TITLE` to override the run title or `QASE_TESTOPS_RUN_ID` to publish into an existing Qase run. Never store the API token in the repository.

## Full build

```bash
./mvnw clean verify
```
