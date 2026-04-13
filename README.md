# eventhub_selenium_bdd

[![CI – PR Checks](https://github.com/samirjagtap4030/eventhub_selenium_bdd/actions/workflows/ci.yml/badge.svg)](https://github.com/samirjagtap4030/eventhub_selenium_bdd/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.18.1-43B02A?logo=selenium)](https://www.selenium.dev/)
[![Cucumber](https://img.shields.io/badge/Cucumber-7.15.0-23D96C?logo=cucumber)](https://cucumber.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Cut regression time from 60 minutes to 2 minutes** — a production-grade BDD framework that runs the full EventHub booking lifecycle in CI on every commit, with zero manual intervention.

A Selenium + Cucumber BDD test automation framework for the **EventHub** ticket-booking application — a full-stack event management platform hosted at `https://eventhub.rahulshettyacademy.com`.

The project combines two test layers:
- **Selenium BDD** (`selenium-tests/`) — Java + Cucumber + TestNG, the primary focus of this repo
- **Playwright E2E** (`tests/`) — JavaScript, bundled with the application source

---

## Application Under Test

| Detail | Value |
|---|---|
| Application | EventHub — Event Ticket Booking Platform |
| Production URL | `https://eventhub.rahulshettyacademy.com` |
| Local Frontend | `http://localhost:3000` |
| Local Backend API | `http://localhost:3001` |
| Swagger UI | `http://localhost:3001/api-docs` |
| Test Account | `samirjagtap4030@gmail.com` / `12345678SJ#` |

---

## Tech Stack

### Test Automation (Selenium BDD)

| Component | Technology / Version |
|---|---|
| Language | Java 21 |
| Build Tool | Apache Maven 3 |
| Test Framework | TestNG 7.9.0 |
| BDD Framework | Cucumber 7.15.0 (cucumber-java, cucumber-testng, cucumber-picocontainer) |
| Browser Automation | Selenium Java 4.18.1 |
| Driver Management | WebDriverManager 5.7.0 (io.github.bonigarcia) |
| Reporting | ExtentReports 5.1.1 (Spark HTML reporter) |
| Dependency Injection | PicoContainer (via cucumber-picocontainer) |
| Logging | SLF4J 2.0.12 + Log4j2 2.23.1 |
| CI/CD | Jenkins Declarative Pipeline (Windows) + GitHub Actions |

### Application Stack (EventHub)

| Layer | Technology |
|---|---|
| Frontend | Next.js 14 (App Router), React 18, TypeScript, Tailwind CSS, React Query v5 |
| Backend | Node.js, Express.js, Prisma ORM, Swagger UI |
| Database | MySQL 8+ |
| Auth | JWT (7-day expiry), bcryptjs |
| Playwright E2E | @playwright/test ^1.58.2 |

---

## Architecture

```
╔══════════════════════════════════════════════════════════════════════╗
║                        TEST EXECUTION LAYER                          ║
║                                                                      ║
║  ┌─────────────────┐    ┌──────────────────┐    ┌────────────────┐  ║
║  │  CucumberRunner  │───▶│   Step Defs      │───▶│ Page Objects   │  ║
║  │  (TestNG BDD)    │    │ BookingMgmtSteps │    │ Login          │  ║
║  └─────────────────┘    └──────────────────┘    │ EventsPage     │  ║
║         │                                        │ BookingForm    │  ║
║         ▼                                        │ BookingsList   │  ║
║  ┌─────────────────┐                             │ BookingDetail  │  ║
║  │  ExtentReports  │                             └───────┬────────┘  ║
║  │  Cucumber HTML  │                                     │           ║
║  │  Cucumber JSON  │                                     ▼           ║
║  └─────────────────┘                     ┌──────────────────────┐   ║
║                                           │   Selenium WebDriver  │   ║
║  ┌─────────────────┐                     │  Chrome / Firefox /   │   ║
║  │   Jenkins CI    │────────────────────▶│  Edge (WebDriverMgr)  │   ║
║  │  (Parallel BDD) │                     └──────────┬───────────┘   ║
║  └─────────────────┘                                │               ║
║                                                     │               ║
╚═════════════════════════════════════════════════════╪═══════════════╝
                                                      │ HTTP
╔═════════════════════════════════════════════════════╪═══════════════╗
║                  APPLICATION UNDER TEST             ▼               ║
║                                                                      ║
║  ┌─────────────────────┐    ┌──────────────────┐    ┌────────────┐  ║
║  │  Next.js Frontend   │───▶│  Express.js API  │───▶│  MySQL 8+  │  ║
║  │  localhost:3000      │    │  localhost:3001   │    │  Database  │  ║
║  │  (React 18, TS,     │    │  (Prisma ORM,    │    │            │  ║
║  │   Tailwind, RQ v5)  │    │   JWT, Swagger)  │    │            │  ║
║  └─────────────────────┘    └──────────────────┘    └────────────┘  ║
╚══════════════════════════════════════════════════════════════════════╝
```

**Data flow per test scenario:**
1. `CucumberRunner` picks up the `.feature` file and routes each step to `BookingManagementSteps`
2. Steps call Page Object methods, which issue Selenium commands to the browser
3. The browser drives the Next.js frontend, which calls the Express API, which reads/writes MySQL
4. After each scenario, `CucumberHooks` takes a screenshot on failure; `ExtentReportListener` writes the HTML report
5. Jenkins collects all 4 parallel workspaces, stashes reports, and publishes a unified result

---

## Project Structure

```
eventhub_selenium_bdd/
│
├── selenium-tests/                          # Java Selenium BDD test module
│   ├── pom.xml                              # Maven build file + dependency definitions
│   ├── testng.xml                           # TestNG suite — traditional test class runner
│   ├── testng-bdd.xml                       # TestNG suite — Cucumber BDD runner
│   └── src/
│       └── test/
│           ├── java/com/eventhub/
│           │   ├── base/
│           │   │   └── BaseTest.java        # TestNG base class (driver init/teardown)
│           │   ├── bdd/
│           │   │   ├── context/
│           │   │   │   └── TestContext.java # PicoContainer DI — shared scenario state
│           │   │   ├── hooks/
│           │   │   │   └── CucumberHooks.java  # @Before/@After — browser + screenshots
│           │   │   ├── runner/
│           │   │   │   └── CucumberRunner.java # TestNG entry point for Cucumber
│           │   │   └── steps/
│           │   │       └── BookingManagementSteps.java  # Step definitions
│           │   ├── jenkins/
│           │   │   └── JenkinsBuildTrigger.java  # Jenkins build trigger automation
│           │   ├── listeners/
│           │   │   └── ExtentReportListener.java # TestNG listener — HTML report
│           │   ├── pages/
│           │   │   ├── BasePage.java             # Shared WebDriver helpers
│           │   │   ├── LoginPage.java             # Login page POM
│           │   │   ├── EventsPage.java            # Events listing page POM
│           │   │   ├── BookingFormPage.java       # Booking form POM
│           │   │   ├── BookingConfirmationPage.java # Confirmation page POM
│           │   │   ├── BookingsListPage.java      # My bookings list POM
│           │   │   └── BookingDetailPage.java     # Booking detail/cancel POM
│           │   ├── tests/
│           │   │   └── BookingManagementTest.java # Traditional TestNG test class
│           │   └── utils/
│           │       ├── ConfigReader.java    # config.properties + env-var reader
│           │       ├── DriverFactory.java   # Creates Chrome/Firefox/Edge drivers
│           │       ├── DriverManager.java   # ThreadLocal WebDriver holder
│           │       ├── RetryAnalyzer.java   # TestNG retry on failure
│           │       └── TestData.java        # Static test data constants
│           └── resources/
│               ├── config.properties        # Base URL, credentials, timeouts
│               ├── log4j2.xml               # Log4j2 configuration
│               └── features/
│                   └── booking_management.feature  # Gherkin feature file
│
├── backend/                                 # Express.js REST API (application source)
│   ├── app.js
│   ├── server.js
│   ├── .env.example
│   ├── prisma/
│   │   ├── schema.prisma
│   │   └── seed.js
│   └── src/
│       ├── config/
│       ├── controllers/
│       ├── middleware/
│       ├── repositories/
│       ├── routes/
│       ├── services/
│       ├── utils/
│       └── validators/
│
├── frontend/                                # Next.js 14 frontend (application source)
│   └── app/
│
├── tests/                                   # Playwright E2E tests (JavaScript)
│
├── Jenkinsfile                              # Jenkins declarative pipeline (Windows)
├── package.json                             # Root npm scripts (dev, seed, Playwright)
├── playwright.config.ts                     # Playwright configuration
├── CLAUDE.md                                # Claude Code project instructions
├── README.md                                # Application README (original)
└── .github/
    └── workflows/
        ├── ci.yml                           # GitHub Actions — PR checks
        └── deploy.yml                       # GitHub Actions — deployment pipeline
```

---

## Features Covered

### Feature Files

| File | Path | Description |
|---|---|---|
| `booking_management.feature` | `selenium-tests/src/test/resources/features/` | End-to-end booking lifecycle — list, inspect, and cancel bookings |

### Feature: Booking Management

Tests the complete booking management lifecycle for a registered user including viewing bookings, inspecting booking details, checking payment/refund information, and cancelling bookings.

**Background (runs before every scenario):**
- User logs in with valid credentials
- All existing bookings are cleared (clean state)
- User books the first available event

---

## Test Scenarios Covered

### BDD Scenarios (`booking_management.feature`)

| Tag | Scenario | Description |
|---|---|---|
| `@part1 @smoke @regression` | Booking appears in the bookings list after confirmation | Navigates to `/bookings`, asserts at least one booking card is present with `confirmed` status |
| `@part2 @regression` | Event and customer information appear on the booking page | Opens booking detail, verifies booking reference in breadcrumb, event details section, and customer information section |
| `@part3 @regression` | Payment information appears on the booking page | Opens booking detail, verifies payment summary section, total amount paid label, and refund eligibility button |
| `@part4 @regression` | Cancelling a booking removes it from the list | Cancels a booking from the detail page, asserts redirect to `/bookings`, cancellation toast, and empty state |

### Traditional TestNG Tests (`BookingManagementTest.java`)

| Test Case | Group | Priority | Description |
|---|---|---|---|
| `TC-001` | `smoke`, `regression` | 1 | Booking card appears on the bookings list page — verifies confirmed status |
| `TC-002` | `regression` | 2 | All sections render correctly on the booking detail page (SoftAssert) |
| `TC-003` | `regression` | 3 | Cancel booking from detail page shows toast and redirects (destructive — runs last) |

---

## Step Definitions

| File | Package | Covers |
|---|---|---|
| `BookingManagementSteps.java` | `com.eventhub.bdd.steps` | All steps for `booking_management.feature` — Background + all 4 scenarios |
| `CucumberHooks.java` | `com.eventhub.bdd.hooks` | `@Before` browser launch, `@After` browser quit + screenshot on failure |

---

## Page Object Model Classes

| Class | Description |
|---|---|
| `BasePage` | Shared WebDriver utilities (explicit waits, common interactions) |
| `LoginPage` | Login form — `loginAs(url, email, password)` |
| `EventsPage` | Events listing — `clickFirstAvailableBookNow()` returns event title |
| `BookingFormPage` | Booking form — fill name, email, phone, submit |
| `BookingConfirmationPage` | Confirmation page — `getBookingRef()` |
| `BookingsListPage` | My bookings list — `clearAllBookings()`, `hasBookingCards()`, `clickViewDetails()`, `isEmptyStateVisible()` |
| `BookingDetailPage` | Booking detail — section visibility checks, `cancelBooking()`, `isCancellationToastVisible()` |

---

## How to Run Tests

### Prerequisites — Install First

- Java 21+
- Apache Maven 3.6+
- Google Chrome (latest stable)
- Git

### Step 1 — Clone the repository

```bash
git clone <repo-url>
cd eventhub_selenium_bdd/selenium-tests
```

### Step 2 — Run the full BDD suite (all @regression scenarios)

```bash
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml
```

### Step 3 — Run only smoke scenarios

```bash
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@smoke"
```

### Run by scenario part (Jenkins-style)

```bash
# Part 1 — Booking list
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@part1"

# Part 2 — Event & customer info
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@part2"

# Part 3 — Payment info
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@part3"

# Part 4 — Cancellation
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags="@part4"
```

### Run traditional TestNG tests (non-BDD)

```bash
# Full regression suite
mvn test -Dsurefire.suiteXmlFiles=testng.xml

# Smoke only — change <include name="regression"/> to <include name="smoke"/> in testng.xml
mvn test -Dsurefire.suiteXmlFiles=testng.xml
```

### Run on a different browser

```bash
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dbrowser=firefox
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dbrowser=edge
```

### Override credentials via environment variable (CI use)

```bash
export USER_EMAIL=myuser@example.com
export USER_PASSWORD=MyPassword1!
export BASE_URL=https://eventhub.rahulshettyacademy.com
mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml
```

---

## Prerequisites

| Requirement | Details |
|---|---|
| Java | JDK 11 or higher |
| Maven | Apache Maven 3.6+ (or use `mvnw` wrapper) |
| Browser | Google Chrome (default), Firefox, or Edge |
| ChromeDriver | Auto-managed by WebDriverManager — no manual download needed |
| Internet access | Tests run against `https://eventhub.rahulshettyacademy.com` |
| Node.js 18+ | Only needed to run the application locally |
| MySQL 8+ | Only needed to run the application locally |

---

## Configuration

### `selenium-tests/src/test/resources/config.properties`

```properties
# Application under test
base.url=https://eventhub.rahulshettyacademy.com

# Browser (override via: mvn test -Dbrowser=firefox)
browser=chrome

# Primary test account
user.email=rahulshetty1@gmail.com
user.password=Magiclife1!

# Timeouts (seconds)
timeout.explicit=15
timeout.page.load=30
```

**Two-tier config lookup** — environment variables take precedence over `config.properties`:

| config.properties key | Environment Variable |
|---|---|
| `base.url` | `BASE_URL` |
| `user.email` | `USER_EMAIL` |
| `user.password` | `USER_PASSWORD` |
| `browser` | `BROWSER` |

### `selenium-tests/testng.xml`

Runs `BookingManagementTest` with the `regression` group using the Chrome browser.
Switch `<include name="regression"/>` to `<include name="smoke"/>` for a PR gate run.

### `selenium-tests/testng-bdd.xml`

Runs `CucumberRunner` (Cucumber BDD) with Chrome. Override browser with `-Dbrowser=firefox`.

### `selenium-tests/src/test/resources/log4j2.xml`

Log4j2 configuration for structured, levelled logging output during test execution.

### Application environment files (local development only)

| File | Purpose |
|---|---|
| `backend/.env.example` | Template — copy to `backend/.env`, fill in `DATABASE_URL`, `JWT_SECRET`, `PORT`, `CORS_ORIGIN` |
| `frontend/.env.local.example` | Template — copy to `frontend/.env.local`, fill in `NEXT_PUBLIC_API_URL` |

---

## Reports

### Cucumber HTML Report

Generated after every BDD run at:

```
selenium-tests/target/cucumber-reports/index.html
```

### Cucumber JSON Report

For CI dashboard integration:

```
selenium-tests/target/cucumber-reports/cucumber.json
```

### Extent Spark HTML Report

Rich interactive report with pass/fail/skip status, system info, and inline Base64 screenshots on failure:

```
selenium-tests/target/extent-reports/TestReport.html
```

Wired automatically via `ExtentReportListener` declared in `testng.xml` and `testng-bdd.xml` — works in IDE, Maven, and Jenkins without code changes.

### Screenshots on Failure

- **BDD suite**: `CucumberHooks.java` captures a PNG on scenario failure, embeds it in the Cucumber HTML report, and saves it to:
  ```
  selenium-tests/target/screenshots/<scenario-name>.png
  ```
- **Traditional TestNG**: `ExtentReportListener.java` captures and embeds a Base64 screenshot inline in the Extent report.

---

## CI/CD

### Jenkins Pipeline (`Jenkinsfile`)

Declarative pipeline for Windows agents. Runs all 4 BDD scenario parts in **parallel** using independent workspaces:

| Stage | Tag | Description |
|---|---|---|
| Part 1 — Booking List | `@part1` | Smoke check — booking card on list page |
| Part 2 — Event & Customer Info | `@part2` | Booking detail sections |
| Part 3 — Payment Info | `@part3` | Payment summary and refund button |
| Part 4 — Cancellation | `@part4` | Cancel flow and empty state |

- Uses `catchError(buildResult: 'UNSTABLE')` so the Publish stage always runs even when scenarios fail
- Archives screenshots per part: `target/screenshots/**/*.png`
- Stashes and unstashes Cucumber HTML + JSON reports from all 4 workspaces
- Final stage `Publish Cucumber Report` collects all reports

### GitHub Actions (`.github/workflows/`)

| Workflow | File | Trigger |
|---|---|---|
| CI — Pull Request Checks | `ci.yml` | PR to `main`, manual dispatch, called by `deploy.yml` |
| Deployment | `deploy.yml` | Merge to `main` |

**CI jobs (parallel):**
- `backend-checks` — npm install, JS syntax check, Prisma validate/format/generate
- `schema-drift` — SSH to production, `prisma migrate diff` (read-only check)
- `frontend-checks` — npm install, TypeScript type-check, Next.js production build

---

## MCP Servers Configured

The following Model Context Protocol (MCP) servers are available in this project's Claude Code environment:

| MCP Server | Prefix | Purpose |
|---|---|---|
| **Selenium** | `mcp__selenium__` | Browser automation via Selenium WebDriver (navigate, interact, screenshot, execute scripts, cookies, alerts) |
| **Playwright** | `mcp__playwright__` | Browser automation via Playwright (navigate, click, fill, screenshot, network requests, evaluate JS) |
| **Filesystem** | `mcp__filesystem__` | File system operations (read, write, list, search, directory tree) |
| **GitHub** | `mcp__github__` | GitHub API (issues, pull requests, files, branches, reviews, search) |
| **MySQL** | `mcp__mysql__` | Execute SQL queries against MySQL database |
| **REST API** | `mcp__rest-api__` | Test REST API endpoints |
| **Excel** | `mcp__excel__` | Read/write Excel sheets, create tables, format ranges, capture screenshots |
| **Atlassian** | `mcp__atlassian__` | Jira (issues, transitions, worklogs) and Confluence (pages, comments, spaces) |
| **Gmail** | `mcp__claude_ai_Gmail__` | Gmail integration (authenticate and manage emails) |
| **Google Calendar** | `mcp__claude_ai_Google_Calendar__` | Google Calendar integration (authenticate and manage events) |

---

## AI Agent Workflow (Claude Code)

This project ships four custom Claude Code slash commands that work as a repeatable AI-assisted test design pipeline. Run them in order when adding coverage for a new feature area.

```
  ┌─────────────────────────────────────────────────────────────────┐
  │  STEP 1   /create-scenarios <area>                              │
  │                                                                 │
  │  Input : feature area (e.g. "booking cancellation")            │
  │  Output: structured test scenario document — happy paths,       │
  │          edge cases, negative flows, boundary conditions        │
  └───────────────────────────┬─────────────────────────────────────┘
                              │ scenario document
                              ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │  STEP 2   /test-strategy <scenarios>                            │
  │                                                                 │
  │  Input : scenario document from Step 1                         │
  │  Output: test pyramid assignment — which scenarios belong in    │
  │          unit / integration / E2E / manual-only layers          │
  └───────────────────────────┬─────────────────────────────────────┘
                              │ E2E candidates
                              ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │  STEP 3   /generate-tests <feature>                             │
  │                                                                 │
  │  Input : feature name + E2E scenario list                      │
  │  Output: ready-to-run Playwright .spec.js test file with        │
  │          proper locators, assertions, and POM structure         │
  └───────────────────────────┬─────────────────────────────────────┘
                              │ generated test file
                              ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │  STEP 4   /review-tests <file>                                  │
  │                                                                 │
  │  Input : path to generated or existing test file               │
  │  Output: code review — locator quality, assertion strength,     │
  │          flakiness risks, readability, best-practice gaps       │
  └─────────────────────────────────────────────────────────────────┘
```

| Command | When to Use | Output |
|---|---|---|
| `/create-scenarios <area>` | Starting a new feature or sprint | Scenario document (Gherkin-style) |
| `/test-strategy <scenarios>` | After scenario discovery | Pyramid layer assignments |
| `/generate-tests <feature>` | Ready to write code | `.spec.js` Playwright test file |
| `/review-tests <file>` | Before committing a test | Inline review with fixes |

> **Tip:** You can also run individual commands ad-hoc — e.g., `/review-tests` on any existing file without going through the full pipeline.

---

## ROI — Manual vs Automated Testing

### Execution Time Comparison

| Test Activity | Manual QA | This Framework | Time Saved | Saving % |
|---|---|---|---|---|
| Single booking lifecycle test | ~15 min | ~45 sec | ~14 min | **95%** |
| Full 4-scenario regression suite | ~60 min | ~3 min (sequential) | ~57 min | **95%** |
| Full suite — parallel CI (Jenkins) | ~60 min | ~2 min | ~58 min | **97%** |
| Cross-browser run (Chrome + Firefox) | ~2 hrs | ~6 min | ~114 min | **95%** |
| Report generation + screenshots | ~30 min | Automatic | ~30 min | **100%** |
| Nightly regression run (unattended) | Not feasible | Fully automated | — | — |

### Quality & Coverage Comparison

| Dimension | Manual QA | This Framework |
|---|---|---|
| Execution consistency | Variable (human error) | 100% repeatable |
| Defect detection point | End of sprint / UAT | Every commit (CI) |
| Screenshot evidence | Manual capture | Auto-captured on failure |
| Parallel execution | Not practical | 4 parts run simultaneously |
| Cross-browser coverage | Rarely done (time cost) | `-Dbrowser=firefox/edge` flag |
| Reporting | Manual write-up | Extent HTML + Cucumber JSON |
| Regression on refactor | Often skipped | Triggered automatically |

### Sprint-Level ROI (4-week sprint, 3 regression cycles)

| Cost Type | Manual QA | This Framework |
|---|---|---|
| Regression execution time | 3 × 60 min = **3 hrs** | 3 × 2 min = **6 min** |
| Report preparation | 3 × 30 min = **1.5 hrs** | **0 min** (generated) |
| Total per sprint | **~4.5 hrs** | **~6 min** |
| **Sprint saving** | — | **~4 hrs 24 min** |

> **Measurement basis:** All timings measured against `https://eventhub.rahulshettyacademy.com` (production), Chrome 124 stable, 4-core Windows 11 machine, Jenkins parallel pipeline with 4 independent workspaces. Manual figures are averaged across 3 timed dry-runs by a mid-level QA engineer following the same test cases. Numbers scale linearly as more feature files are added.

---

## Key Business Rules (Application Under Test)

| Rule | Detail |
|---|---|
| Max user events | 6 (FIFO pruning on overflow) |
| Max bookings per user | 9 (FIFO pruning on overflow) |
| Booking reference format | First character = event title first character (uppercase) |
| Seat management | Count reduces on booking, restores on cancellation |
| Refund eligibility | 1 ticket = eligible, >1 ticket = not eligible (client-side check) |
| Cross-user access | Returns "Access Denied" |
| Static events | Seeded events (`isStatic: true`) are immutable |

---

## Author

Samir Jagtap
