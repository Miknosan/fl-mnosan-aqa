# CI/CD Quality Pipeline

The `Quality gate` GitHub Actions workflow is the single CI/CD entry point for compile validation, environment-aware domain execution, test evidence retention, Qase TestOps synchronization, consolidated Allure reporting, and optional report delivery through GitHub Pages.

## Trigger policy

| Trigger | Environment | Domains | Test plan | Publication |
|---|---|---|---|---|
| Manual dispatch | user selection | user selection | user selection | user selection |

The workflow starts only through `Actions / Quality gate / Run workflow`. Manual domain selection accepts `all` or a comma-separated list containing `booker`, `hygraph`, and `demoqa`. The environment input is validated against `config/environments/<environment>.properties`, so a newly committed profile is immediately available without a workflow code change.

Runs targeting the same environment are queued instead of cancelled or executed concurrently. Different environments can execute independently.

## Pipeline stages

1. `Prepare execution` validates environment, domains, test plan, publication flags, and generates the domain matrix.
2. `Compile framework` compiles production and test sources before external test infrastructure is used.
3. Domain jobs run in parallel with `fail-fast` disabled and reference the selected GitHub Environment without creating deployment records.
4. Every domain retains Surefire, raw Allure, screenshot, and trace evidence even when its tests fail.
5. `Build consolidated Allure report` downloads all available domain evidence and produces one HTML report with execution metadata.
6. `Publish Allure report` optionally delivers the HTML report to GitHub Pages.
7. `Final quality gate` preserves a failing status when preparation, compilation, any selected domain, reporting, or requested publication fails.

## GitHub Environments

Create GitHub Environments whose names match the committed profile names, initially `dev` and `stage`. Environment jobs use `deployment: false`, so test execution can consume protected configuration without adding test runs to deployment history.

Configure branch restrictions and required reviewers according to the target risk. Stage should be restricted to trusted branches and manual approvals when it provides access to protected credentials.

## Environment secrets

Configure secrets separately in each GitHub Environment:

| Secret | Purpose |
|---|---|
| `BOOKER_USERNAME` | Booker environment credential |
| `BOOKER_PASSWORD` | Booker environment credential |
| `QASE_TESTOPS_API_TOKEN` | Qase TestOps API authentication |

The public assignment defaults keep local execution self-contained. Real environment credentials must exist only in GitHub Environment secrets.

## Environment variables

Environment-level GitHub variables can override non-secret profile values without changing the repository:

| Variable | Purpose |
|---|---|
| `BOOKER_BASE_URL` | Booker target override |
| `HYGRAPH_BASE_URL` | Hygraph target override |
| `DEMOQA_BASE_URL` | DemoQA target override |

Manual dispatch publication flags enable the corresponding integration. A requested Qase publication fails fast when its token is unavailable.

## Qase configuration

Create Qase environments with slugs matching the execution profiles, initially `dev` and `stage`. Each selected domain creates a separately named run containing its domain, environment, plan, and GitHub run number. Results are published only when the operator explicitly enables Qase publication for that run.

## GitHub Pages

Set the repository Pages source to `GitHub Actions` before selecting manual report publication. The workflow packages and deploys only a successfully generated consolidated report. A report may still be delivered for a failed test run so that failure evidence remains accessible; the final quality gate continues to fail.

## Known regression signal

A manually selected DemoQA regression includes DA-52. Its six deterministic failures represent the documented Web Tables sorting product defect. The workflow retains and publishes that evidence without converting the failed tests into a successful quality gate.

## Supply-chain maintenance

Third-party workflow actions are pinned to immutable commit SHAs. Dependabot checks Maven and GitHub Actions dependencies weekly and proposes controlled updates. Each dependency update is verified by explicitly launching the manual quality pipeline with the required scope.
