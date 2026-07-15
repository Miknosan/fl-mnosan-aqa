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
2. `Compile framework` compiles production and test sources before external test infrastructure is used and verifies the CI script contracts.
3. `Create consolidated Qase run` creates one environment-aware run when Qase publication is requested and exposes its ID to every selected domain.
4. Domain jobs run in parallel with `fail-fast` disabled, publish into the shared Qase run, and reference the selected GitHub Environment without creating deployment records.
5. `Complete consolidated Qase run` executes with `always()` after the domain matrix so failed tests cannot leave the shared run open.
6. Every domain retains Surefire, raw Allure, screenshot, trace, and Qase fallback evidence even when its tests fail.
7. `Build consolidated Allure report` downloads all available domain evidence and produces one HTML report with execution metadata.
8. `Publish Allure report` optionally delivers the HTML report to GitHub Pages.
9. `Final quality gate` preserves a failing status when preparation, compilation, Qase lifecycle, any selected domain, reporting, or requested publication fails.

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

Manual dispatch publication flags enable the corresponding integration. A requested Qase publication fails fast when its token is unavailable, shared run creation fails, or a reporter falls back to a local Qase report instead of delivering results.

## Qase configuration

Create Qase environments with slugs matching the execution profiles, initially `dev` and `stage`. When publication is requested, the pipeline creates one run named `Quality gate | <environment> | <plan> | GitHub #<number>` and passes its ID to all selected domain jobs through `QASE_TESTOPS_RUN_ID`. Reporters use `QASE_TESTOPS_RUN_COMPLETE=false`; only the finalizer closes the run after every domain has finished. This preserves parallel domain execution while producing one cross-domain quality-gate record.

The lifecycle client reads project code `DA` from the root `qase.config.json`, uses the environment-scoped token, and calls the documented Qase create and complete endpoints without logging credentials. Public access remains an explicit operator decision: open the completed run, select `Share report`, enable public access, and copy the generated link.

The `always()` finalizer covers test and reporting failures. If an operator cancels the workflow after Qase run creation, GitHub can terminate the finalizer as well; in that exceptional case, complete or abort the run from Qase before starting the replacement execution.

## GitHub Pages

Set the repository Pages source to `GitHub Actions` before selecting manual report publication. The workflow packages and deploys only a successfully generated consolidated report. A report may still be delivered for a failed test run so that failure evidence remains accessible; the final quality gate continues to fail.

## Known regression signal

A manually selected DemoQA regression includes DA-52. Its six deterministic failures represent the documented Web Tables sorting product defect. The workflow retains and publishes that evidence without converting the failed tests into a successful quality gate.

## Supply-chain maintenance

Third-party workflow actions are pinned to immutable commit SHAs. Dependabot checks Maven and GitHub Actions dependencies weekly and proposes controlled updates. Each dependency update is verified by explicitly launching the manual quality pipeline with the required scope.
