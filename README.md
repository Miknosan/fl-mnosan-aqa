# QA Automation Test Suite

Java 17 multi-module test automation framework covering the Booker REST API, Hygraph GraphQL API, and DemoQA UI with unified local and CI execution.

## Technology stack

| Area | Technology | Responsibility |
|---|---|---|
| Runtime and build | Java 17, Maven Wrapper, Maven multi-module build | Reproducible local and CI execution |
| Test engine | JUnit 5, JUnit Platform, Maven Surefire | Test lifecycle, parameterization, tagging and execution |
| Assertions | AssertJ | Readable domain and contract assertions |
| API transport | REST Assured | Builds and executes Booker REST and Hygraph GraphQL HTTP requests and exposes their responses for verification |
| JSON serialization | Jackson | Converts typed Java request models into JSON and maps API JSON responses back into typed Java response models |
| GraphQL contracts | GraphQL Java | Parses GraphQL documents and validates queries and fragments against the introspected Hygraph schema |
| UI automation | Playwright for Java | Browser lifecycle, Page Objects, components, waits, screenshots and traces |
| Test data | Datafaker (`Faker`), immutable models, domain data factories | Unique and intention-revealing payloads |
| Model implementation | Lombok | Generates immutable models and builders while keeping request and response contracts concise |
| Architecture control | ArchUnit, Maven Enforcer, custom metadata gates | Module boundaries, naming, dependency convergence and test documentation |
| Reporting | Allure, Surefire | Human-readable reports and raw execution evidence |
| Test management | Qase JUnit 5 reporter | Test-case traceability and consolidated public test runs |
| CI/CD | GitHub Actions, GitHub Environments, GitHub Pages | Parameterized execution, secret isolation, evidence retention and report delivery |

Dependency and plugin versions are centralized in the parent [`pom.xml`](pom.xml).

## Contents

- [Solution overview](#solution-overview)
- [Architecture](#architecture)
- [Test scope](#test-scope)
- [Prerequisites](#prerequisites)
- [How to run](#how-to-run)
- [Environment configuration](#environment-configuration)
- [Reports and test management](#reports-and-test-management)
- [Test strategy](#test-strategy)
- [Challenges and solutions](#challenges-and-solutions)
- [CI/CD](#cicd)
- [What I would add with more time](#what-i-would-add-with-more-time)

## Solution overview

The framework treats each tested system as an independent domain rather than combining unrelated technologies in one test module.

| Domain | Test type | Main technology | Business features | Automated scenarios |
|---|---|---|---|---|
| Booker | REST API | REST Assured | Authentication, booking lifecycle, validation and search | 10 |
| Hygraph | GraphQL API | REST Assured, GraphQL Java | Movie catalog queries, relationships and contract validation | 6 |
| DemoQA | Browser UI | Playwright for Java | Student Registration Form and Web Tables | 9 |

The 25 product scenarios are complemented by system-level architecture, metadata, configuration, GraphQL contract, and source-quality gates. Product scenarios are synchronized with Qase, while system gates remain visible in Allure but are intentionally excluded from Qase because they validate the framework rather than user behavior.

Key capabilities include:

- domain and test-plan selection;
- DEV and STAGE execution profiles;
- generated and isolated test data;
- data-driven JUnit scenarios;
- Playwright Page Object Model and state-based waits;
- screenshots, traces and browser diagnostics on UI failures;
- configurable parallel execution with conservative defaults for public services;
- Allure, Surefire and Qase TestOps reporting;
- automatic pull-request system quality gates;
- a manually controlled business quality gate with optional public Qase and GitHub Pages reports.

## Architecture

```text
qa-automation-parent/
├── pom.xml                         # modules, dependency and plugin governance
├── qase.config.json                # non-secret Qase reporter defaults
├── config/environments/            # DEV and STAGE execution profiles
├── qa-core/                        # technology-neutral shared contracts
├── booker-api-tests/               # independent Booker REST domain
├── hygraph-graphql-tests/          # independent Hygraph GraphQL domain
├── demoqa-ui-tests/                # independent DemoQA Playwright domain
└── qa-orchestrator/                # validated execution selection
```

### Module responsibilities

- `qa-core` owns only reusable cross-domain infrastructure: configuration loading, environment identity, JSON, generic HTTP settings, test-plan classifications and reporting contracts.
- `booker-api-tests` owns Booker clients, models, authentication, booking workflows, data and assertions.
- `hygraph-graphql-tests` owns the movie-catalog client, GraphQL documents, typed results, schema validation and feature assertions.
- `demoqa-ui-tests` owns Playwright lifecycle, pages, components, models, data factories, UI assertions and failure evidence.
- `qa-orchestrator` converts validated domain, plan, feature, environment and parallelism selections into Maven execution. It contains no domain clients, test data or assertions.

Domain modules depend only on `qa-core`; they never depend on one another. A new service can therefore be introduced as a new Maven module without changing the internal implementation of Booker, Hygraph or DemoQA.

### Package ownership inside a domain

```text
src/main/java/io/testautomation/<domain>/
├── <feature>/                      # clients/pages, components, models and data
└── platform or configuration/      # technology-specific execution infrastructure

src/test/java/io/testautomation/<domain>/
├── features/<feature>/scenarios/  # executable product behavior mapped to Qase
├── framework/                     # JUnit lifecycle and reusable test infrastructure
└── system/                        # architecture, metadata and component quality gates
```

This separation makes the location and responsibility of product scenarios explicit while preventing framework verification from being confused with business test coverage.

## Test scope

### Booker REST API

The Booker scope covers the complete business lifecycle and the authorization boundary around destructive operations:

- successful and rejected authentication;
- booking creation with a complete valid payload;
- data-driven validation of all six mandatory fields;
- retrieval by an existing ID and the not-found contract;
- full authorized update and rejected unauthorized update;
- booking search by guest name;
- authorized deletion and cleanup verification.

These cases were selected because authentication is the prerequisite for protected operations, CRUD represents the service's primary business flow, and negative authorization, required-field and not-found scenarios protect the highest-risk API boundaries. Search adds coverage of collection filtering rather than testing only entity endpoints.

The suite generates unique booking data and tracks created IDs through `BookerExtension`, which removes test-owned bookings after each scenario. It runs sequentially by default to avoid overloading the public Restful Booker service.

### Hygraph GraphQL

The Hygraph scope uses the public Video Streaming schema and covers GraphQL behavior at both data and document-contract levels:

- ordered movie pagination using `first` and `skip` variables;
- single movie retrieval by a published ID;
- GraphQL variables rather than query-string interpolation;
- fragments and the nested `publishedBy` relationship;
- non-existent ID behavior;
- malformed query syntax errors;
- validation errors for a non-existent field.

The cases intentionally represent the main ways a GraphQL integration can fail: incorrect variables, unexpected nullability, syntax errors, schema validation errors and broken nested relationships. Runtime schema introspection validates executable documents against the published schema, while queries and fragments remain external resources instead of Java string literals.

Run-scoped discovery resolves suitable public movie data once without global mutable state. Execution is sequential by default to respect the public endpoint.

### DemoQA UI

The DemoQA scope implements both UI options described by the assignment.

Student Registration Form:

- successful submission using all supported controls;
- file upload, date picker and React dropdown interaction;
- data-driven required-field validation;
- invalid email validation;
- invalid mobile-number validation;
- success modal content verification.

Web Tables:

- add a record;
- update an existing record;
- delete a record;
- search and filter records;
- validate ascending and descending sorting for every supported column.

The successful form submission and record creation form the smoke plan because they prove that the two main UI features are usable end to end. The regression plan adds validation boundaries and the complete Web Tables behavior set. Tests operate through feature-level Page Objects and reusable components, receive a fresh browser context and generated data per execution, and never access the raw Playwright `Page` from a product scenario.

Playwright auto-waiting is complemented by explicit waits for meaningful UI states such as visible forms, opened or closed modals, and present or absent table rows. Arbitrary sleeps and blind test retries are not used.

#### Known DemoQA regression signal

The Web Tables sorting scenario currently exposes a reproducible DemoQA product defect: header clicks succeed, but the public page leaves the record order unchanged. The parameterized scenario therefore reports six deterministic failures, one for each sortable column. The test remains enabled so the regression report communicates the observed product behavior instead of producing an artificially green result. Failure screenshots and Playwright traces are retained as evidence.

## Prerequisites

- JDK 17 or newer;
- Bash-compatible terminal;
- internet access to Maven Central and the public test services;
- Maven 3.6+ or the committed Maven Wrapper;
- Allure CLI only when rendering a report locally.

Verify the toolchain:

```bash
./mvnw --version
```

Playwright uses its version-matched Chromium binary, so a separately installed system Chrome is not required. Install the browser once before the first local UI run:

```bash
./mvnw -pl demoqa-ui-tests -am install -DskipTests
./mvnw -pl demoqa-ui-tests exec:java \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

## How to run

Every command requires an explicit environment. Available committed profiles are `dev` and `stage`.

### Run the complete repository

```bash
./mvnw clean verify -Dtest.environment=dev -Dgroups=business
```

The complete regression currently returns a failing build because the Web Tables sorting scenario detects the documented DemoQA defect. Use the all-domain smoke command below for a green framework verification; use the full command when the known product failure must remain part of the evidence.

### Run API domains only

```bash
./mvnw -pl booker-api-tests,hygraph-graphql-tests -am clean verify \
  -Dtest.environment=dev \
  -Dgroups=business
```

### Run UI tests only

```bash
./mvnw -pl demoqa-ui-tests -am clean verify \
  -Dtest.environment=dev \
  -Dgroups=business
```

### Run one domain and test plan

```bash
# Booker smoke
./mvnw -pl booker-api-tests -am clean verify \
  -Dtest.environment=dev \
  -Dgroups="business & smoke"

# Hygraph regression
./mvnw -pl hygraph-graphql-tests -am clean verify \
  -Dtest.environment=stage \
  -Dgroups="business & regression"

# DemoQA smoke
./mvnw -pl demoqa-ui-tests -am clean verify \
  -Dtest.environment=dev \
  -Dgroups="business & smoke"
```

The published Hygraph schema contract is intentionally separate from deterministic PR checks and business runs:

```bash
./mvnw -pl hygraph-graphql-tests -am clean test \
  -Dtest.environment=dev \
  -Dgroups=external-contract
```

### Run through the orchestrator

Install the orchestrator and shared contracts once:

```bash
./mvnw -pl qa-orchestrator -am install -DskipTests
```

Then select domains, plans, environment, optional feature and parallelism. The orchestrator always combines the requested plan with the `business` classification, so system and external-contract tests cannot enter a manual product run.

```bash
# Smoke for all domains
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=all \
  -Dplans=smoke \
  -Denvironment=dev

# Booker booking regression on STAGE
./mvnw -pl qa-orchestrator exec:java \
  -Ddomains=booker \
  -Dplans=regression \
  -Dfeature=booking \
  -Denvironment=stage \
  -Dparallelism=1
```

Supported domains are `booker`, `hygraph`, `demoqa` and `all`. Supported plans are `smoke`, `regression` and `sanity`; multiple plans use logical OR. Parallelism is validated between 1 and 8, but the default is 1 because the assignment targets are public services.

## Environment configuration

Direct module runs use `-Dtest.environment=<name>` or `TEST_ENVIRONMENT`. Orchestrated runs use `-Denvironment=<name>`. The selected profile is resolved from `config/environments/<name>.properties` before clients or browsers are created.

Configuration precedence is:

```text
JVM system property
OS environment variable or CI secret
config/environments/<environment>.properties
module classpath defaults
```

Important overrides include:

```text
BOOKER_BASE_URL       BOOKER_USERNAME       BOOKER_PASSWORD
HYGRAPH_BASE_URL      HYGRAPH_TIMEOUT_MS
DEMOQA_BASE_URL       DEMOQA_BROWSER        DEMOQA_HEADLESS
```

Profiles contain only non-secret target settings. Credentials and API tokens are supplied through environment variables or GitHub Environment secrets. Unknown profile names, unsafe paths, missing identity and identity mismatches fail before external test infrastructure starts.

DEV and STAGE currently point to the same public assignment services because those systems expose only one public endpoint. The profiles still preserve independent execution identity and allow real environment URLs to be introduced later by changing configuration rather than test code.

## Reports and test management

### Local evidence

- Surefire XML: `<module>/target/surefire-reports`
- Allure results: `<module>/target/allure-results`
- DemoQA screenshots: `demoqa-ui-tests/target/test-artifacts/<environment>/screenshots`
- DemoQA traces: `demoqa-ui-tests/target/test-artifacts/<environment>/traces`

After a test run, render one consolidated local Allure report with an installed Allure CLI:

```bash
allure serve \
  booker-api-tests/target/allure-results \
  hygraph-graphql-tests/target/allure-results \
  demoqa-ui-tests/target/allure-results
```

Manual Allure results contain only business scenarios, use the hierarchy `Domain -> Feature -> Report group`, and include the selected environment. API requests and responses, UI screenshots, traces and browser errors are attached where applicable. Pull-request system gates do not publish Allure or Qase evidence.

### Qase TestOps

Qase reporting is disabled for normal local execution. To publish a direct module run, provide the token at runtime:

```bash
QASE_MODE=testops \
QASE_TESTOPS_API_TOKEN="<your-token>" \
./mvnw -pl booker-api-tests -am clean test \
  -Dtest.environment=dev \
  -Dgroups=business
```

Project code and non-secret defaults are stored in [`qase.config.json`](qase.config.json). The token is never committed.

When enabled in CI, one Qase run is created before the selected domain matrix starts. All domain reporters append results through the shared run ID without closing it. A dedicated finalizer completes the run after every domain, enables public access, and publishes both the authenticated and public report URLs in the GitHub Actions summary. Qase creation, result delivery, completion and public-link generation are part of the final quality gate.

## Test strategy

The strategy prioritizes representative risk and architectural clarity over maximizing the number of similar tests.

1. **Cover critical user and integration flows first.** Authentication and CRUD protect the Booker business lifecycle; GraphQL variables, relationships and schema errors protect the Hygraph contract; complete form submission and Web Tables operations protect DemoQA user behavior.
2. **Balance positive and negative coverage.** Every domain contains successful paths and meaningful validation or error contracts rather than only happy-path checks.
3. **Separate smoke from regression by purpose.** Smoke tests prove that each domain's primary capability is available. Regression adds authorization, validation, negative contracts and full feature behavior.
4. **Keep product scenarios readable.** Tests express arrange, action and assertion at business level. Protocol mechanics, selectors, lifecycle and reusable domain assertions stay behind focused collaborators.
5. **Make data deterministic and isolated.** Datafaker generates unique values, domain factories build valid relationships, mutable Booker records are cleaned up, and every UI test receives a fresh browser context.
6. **Prefer observable synchronization over retries.** Playwright state waits replace sleeps; blind retries are avoided so real defects and flaky behavior remain visible.
7. **Treat reporting as part of execution.** Qase traceability, Allure metadata and failure artifacts are verified by the pipeline rather than handled as optional afterthoughts.
8. **Protect the architecture continuously.** ArchUnit, metadata gates, source-quality tests and Maven Enforcer stop boundary violations, undocumented product tests and dependency drift automatically on every pull request.

## Challenges and solutions

### Independent technologies in one repository

Booker REST, Hygraph GraphQL and DemoQA UI require different clients, models, lifecycle and runtime resources. Putting them into one package tree would create coupling and make selective execution unclear. Each system is therefore an independent Maven domain with its own feature packages and configuration. `qa-core` contains only stable technology-neutral contracts.

### Independent domain control and orchestration

A monolithic test command makes it difficult to select scope, assign failure ownership or tune resources per technology. `qa-orchestrator` provides one validated execution entry point while preserving domain independence. It selects modules, plans, features, environments and parallelism, then delegates execution to Maven without importing domain logic. GitHub Actions applies the same model through separate matrix jobs, so Booker, Hygraph and DemoQA can be run, diagnosed and extended independently while their evidence is consolidated afterward.

### Environment growth without duplicated code

Environment URLs and credentials must not be embedded in tests. Central profiles define environment identity and non-secret endpoints, while environment variables and JVM properties provide controlled overrides. Adding another environment requires one profile and CI environment configuration rather than changes across clients and scenarios.

### Stable execution against public services

The assignment targets can reset data, become temporarily unavailable or reject excessive traffic. Domain suites therefore execute sequentially by default, generate unique data, clean mutable Booker state, reuse read-only Hygraph discovery data within a run, and block disruptive third-party DemoQA content. Parallelism remains available but explicit and bounded.

### Reliable UI synchronization and diagnostics

DemoQA uses dynamic React controls, modals and table updates. Page Objects and components wait for business-relevant states instead of using fixed sleeps. A JUnit extension owns the browser lifecycle and automatically records screenshots, Playwright traces and browser errors when a product scenario fails.

### Consolidated reporting from parallel jobs

Parallel domain jobs naturally produce separate result directories and can prematurely close a shared test-management run. CI retains evidence per domain, merges raw Allure results into one report, and controls Qase through an explicit create, append, finalize and share lifecycle. `fail-fast` is disabled so one failing domain does not erase evidence from the others, while the final quality gate still remains red.

### Transparent handling of a real product defect

DemoQA currently accepts Web Tables header clicks without sorting the records. The affected scenario was not disabled, weakened or converted into a soft assertion. The deterministic failures and their artifacts remain in regression reporting, demonstrating how the framework communicates a product issue rather than hiding it.

## CI/CD

The [`Pull request quality gates`](.github/workflows/pull-request-quality-gate.yml) workflow automatically runs shared unit tests and local `system` checks for Booker, Hygraph and DemoQA. It uses no environment secrets, does not call the public assignment services, and publishes neither Qase nor Allure results.

The [`Quality gate`](.github/workflows/quality-gate.yml) workflow remains intentionally manual for business verification. The operator selects:

- environment: `dev`, `stage`, or another committed profile;
- domains: `all` or a comma-separated subset of `booker`, `hygraph`, `demoqa`;
- test plan: `smoke`, `regression` or `sanity`;
- whether to publish results to Qase;
- whether to deploy the consolidated Allure report to GitHub Pages.

The pipeline performs:

```text
Validate inputs
-> Verify scripts and compile the framework
-> Create the optional shared Qase run
-> Execute selected business domains as isolated matrix jobs
-> Retain Surefire, Allure and UI evidence
-> Complete and publicly share the Qase run
-> Build one consolidated Allure report
-> Optionally deploy the report to GitHub Pages
-> Evaluate the final quality gate
```

CI covers compilation, controlled test execution and quality evaluation. CD optionally delivers the generated Allure report to GitHub Pages; deployment of the external assignment applications is outside this repository's ownership. GitHub Environment secrets isolate DEV and STAGE credentials, workflow actions are pinned to immutable commit SHAs, and Dependabot proposes dependency updates. Detailed setup and operating rules are documented in [`docs/ci-cd.md`](docs/ci-cd.md).

## What I would add with more time

1. **Cross-browser CI coverage.** Expand only the DemoQA matrix across Chromium, Firefox and WebKit, install the selected Playwright engine per job, label Allure results by browser, and publish browser-specific Qase runs using Qase configurations.
2. **Performance testing with Gatling.** Introduce an independent performance module with baseline, load and stress profiles, explicit response-time and error-rate thresholds, and versioned reports. The workloads would run only against an approved controlled environment or service virtualization, never against the public assignment services.
3. **Qase-driven pipeline execution.** Allow an approved Qase Test Plan or run to securely dispatch GitHub Actions with domain, environment and plan inputs, then write the GitHub run and public report URLs back to Qase.
4. **Additional automated triggers.** Keep the deterministic PR system gate and add label-controlled product smoke, scheduled regression and post-deployment triggers when the repository is connected to a real delivery lifecycle.
5. **Service virtualization.** Add contract-compatible mocks for documented external-service outages and deterministic error scenarios that public systems cannot reliably provide.
6. **Broader behavior coverage.** Add Booker partial update and date-filter cases, expand Hygraph relationship and schema-evolution coverage, and add DemoQA boundary, accessibility and responsive-layout checks.
7. **Historical quality analytics.** Preserve Allure history across GitHub Pages deployments and publish trend metrics for pass rate, duration and recurring failure categories.
