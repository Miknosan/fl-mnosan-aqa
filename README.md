# QA Automation Test Suite

Java 17 / Maven multi-module automation framework for independent Booker REST API, Hygraph GraphQL, and DemoQA UI domains.

## Architecture

```text
qa-automation-parent/
├── pom.xml
├── config/environments/      # centrally governed DEV and STAGE target profiles
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
./mvnw -pl booker-api-tests -am test -Dtest.environment=dev

# Booker smoke plan
./mvnw -pl booker-api-tests -am test -Dtest.environment=dev -Dgroups=smoke

# Booker regression plan
./mvnw -pl booker-api-tests -am test -Dtest.environment=dev -Dgroups=regression
```

The Booker target URL is selected from `config/environments/<environment>.properties`. Technical defaults are stored in `booker-api-tests/src/main/resources/booker.properties`. Values can be overridden by environment variables or Java system properties:

```text
booker.base-url  -> BOOKER_BASE_URL
booker.username  -> BOOKER_USERNAME
booker.password  -> BOOKER_PASSWORD
booker.timeout-ms -> BOOKER_TIMEOUT_MS
```

Tests execute sequentially by default because Restful Booker is a public service. Unique data is generated for each test, and `BookerExtension` cleans up created bookings after every scenario. Qase product scenarios are located under `features/<feature>/scenarios`, reusable test infrastructure under `framework`, and architecture and metadata gates under `system`. See `booker-api-tests/README.md` for package ownership.

## Hygraph tests

The Hygraph suite implements the six cases in Qase suite `DA / Hygraph` against the public Video Streaming schema. It covers movie pagination, variables, single-entity retrieval, fragments and nested fields, not-found behavior, syntax errors, and schema validation.

```bash
# All Hygraph tests
./mvnw -pl hygraph-graphql-tests -am clean test -Dtest.environment=dev

# Hygraph smoke plan
./mvnw -pl hygraph-graphql-tests -am clean test -Dtest.environment=dev -Dgroups=smoke

# Hygraph regression plan
./mvnw -pl hygraph-graphql-tests -am clean test -Dtest.environment=dev -Dgroups=regression
```

The Hygraph target URL is selected from `config/environments/<environment>.properties`. Technical defaults are stored in `hygraph-graphql-tests/src/main/resources/hygraph.properties` and support environment-variable or Java-system-property overrides:

```text
hygraph.base-url  -> HYGRAPH_BASE_URL
hygraph.timeout-ms -> HYGRAPH_TIMEOUT_MS
```

The `moviecatalog` production package represents the `movie-catalog` feature. Its Qase scenarios are located under `features/moviecatalog/scenarios`. Queries and fragments are stored under `src/main/resources/graphql/movie-catalog`. Run-scoped test data resolves suitable public entities once and reuses them without global static state. The suite executes sequentially to avoid unnecessary load on the public service.

Reusable test infrastructure is isolated under `framework`, while platform-level tests for GraphQL transport, response contracts, document loading, schema validation, and architecture gates are located under `system`. Runtime schema introspection validates every executable GraphQL document against the published Hygraph schema. See `hygraph-graphql-tests/README.md` for package ownership and extension rules.

## DemoQA UI tests

The DemoQA suite implements Qase cases `DA-44` through `DA-52` with Playwright for Java. Registration Form and Web Tables are independent feature packages with page objects, reusable components, immutable domain models, generated test data, domain assertions, and JUnit extensions that own browser lifecycle and failure evidence.

```bash
# Install the required browser once
./mvnw -pl demoqa-ui-tests -am install -DskipTests
./mvnw -pl demoqa-ui-tests exec:java \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"

# All DemoQA tests
./mvnw -pl demoqa-ui-tests -am clean test -Dtest.environment=dev

# DemoQA smoke plan
./mvnw -pl demoqa-ui-tests -am clean test -Dtest.environment=dev -Dgroups=smoke

# DemoQA regression plan
./mvnw -pl demoqa-ui-tests -am clean test -Dtest.environment=dev -Dgroups=regression
```

The DemoQA target URL is selected from `config/environments/<environment>.properties`. Technical browser defaults are stored in `demoqa-ui-tests/src/main/resources/demoqa.properties`. Every property can be overridden by its uppercase underscore environment variable or by a Java system property. For example, use `DEMOQA_HEADLESS=false`, `DEMOQA_BROWSER=firefox`, or `-Ddemoqa.base-url=https://demoqa.com`.

Tests use a fresh browser context and generated data per execution. Third-party advertising is blocked at the context boundary. Failed scenarios retain a screenshot, Playwright trace, browser errors, Surefire result, and Allure result. See `demoqa-ui-tests/README.md` for package ownership and extension rules.

## Orchestrated execution

The orchestrator validates domain, plan, feature, environment, and parallelism inputs, converts multiple plans to a JUnit logical-OR expression, and invokes only the selected Maven modules.

```bash
# Build and install the orchestrator and its shared dependency once
./mvnw -pl qa-orchestrator -am install -DskipTests

# Booker smoke
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=booker \
  -Dplans=smoke \
  -Denvironment=dev

# Booker smoke and regression, filtered to the booking feature
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=booker \
  -Dplans=smoke,regression \
  -Dfeature=booking \
  -Denvironment=stage \
  -Dparallelism=1

# Multiple domains
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=booker,hygraph \
  -Dplans=smoke \
  -Denvironment=dev

# Hygraph Movie Catalog feature
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=hygraph \
  -Dplans=regression \
  -Dfeature=movie-catalog \
  -Denvironment=stage
```

Supported domains are `booker`, `hygraph`, `demoqa`, and `all`. Supported plans are `smoke`, `regression`, and `sanity`. Multiple plans use logical OR. Environment is mandatory and is resolved from `config/environments/<name>.properties`; committed profiles are `dev` and `stage`. Parallelism is validated between 1 and 8; keep it at 1 for public services unless a domain is explicitly designed for concurrency.

## Environment configuration

`test.environment` is mandatory for direct module runs, while orchestrated runs accept the equivalent `environment` selection. The selected profile is loaded over module defaults, then OS environment variables and JVM system properties apply their overrides:

```text
JVM system property
OS environment variable / CI secret
config/environments/<environment>.properties
module classpath defaults
```

Profiles contain non-secret, domain-namespaced target settings. `TEST_ENVIRONMENT` can replace the JVM selection property. Unknown profiles, unsafe names, missing profile identity, and identity mismatches stop execution before test infrastructure is created. Add an environment by committing one new `config/environments/<name>.properties` file; no Java change is required. Credentials and tokens belong in CI secret storage and are supplied through their domain environment variables.

The assignment systems expose one public endpoint per domain, so the committed `dev` and `stage` profiles currently target the same services while preserving independent execution identity. Allure adds the selected environment to every test result, and DemoQA failure evidence is isolated under `target/test-artifacts/<environment>`.

## Reports

- Surefire: `<module>/target/surefire-reports`
- Allure: `<module>/target/allure-results`
- DemoQA failure screenshots: `demoqa-ui-tests/target/test-artifacts/<environment>/screenshots`

## CI/CD

GitHub Actions provides an exclusively manual quality pipeline with validated environment, domain, and test-plan selection, parallel domain jobs, environment-scoped configuration, optional consolidated Qase publication, consolidated Allure generation, and optional GitHub Pages delivery. Repository settings, secrets, inputs, and operating rules are documented in [`docs/ci-cd.md`](docs/ci-cd.md).

### Qase TestOps

Qase reporting is disabled for normal local runs. Booker, Hygraph, and DemoQA tests are linked to their Qase cases with `@QaseId`. To create a Qase test run and publish results, provide the API token through the environment and enable TestOps mode:

```bash
QASE_MODE="testops" \
QASE_TESTOPS_API_TOKEN="<your-token>" \
./mvnw -pl booker-api-tests -am clean test -Dtest.environment=dev
```

Replace `booker-api-tests` with `hygraph-graphql-tests` or `demoqa-ui-tests` to publish another domain suite. Set `QASE_TESTOPS_RUN_TITLE` to a domain-specific title for each run.

The non-secret defaults, including project code `DA`, are stored in the root `qase.config.json`, where the reporter can discover them for direct and orchestrated Maven runs. Use `QASE_TESTOPS_RUN_TITLE` to override the run title or `QASE_TESTOPS_RUN_ID` to publish into an existing Qase run. Never store the API token in the repository.

CI creates one Qase run per quality-gate execution and shares its ID across all selected domain jobs. The reporters append results without completing the run; a dedicated finalizer closes it after the full matrix, including failure paths. Public access is enabled manually from the completed run through `Share report`.

## Full build

```bash
./mvnw clean verify -Dtest.environment=dev
```
