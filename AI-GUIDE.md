# AI Collaboration Guide — AntennaPod Mobile Testing

This document is written for AI assistants (Claude, ChatGPT, Copilot, etc.) to quickly
understand the project and start contributing test cases.

## Quick Context

This is a mobile testing course project for [AntennaPod](https://github.com/AntennaPod/AntennaPod) — an open-source podcast manager under MIT license. The project contains:

- The app source under `app-under-test/antennapod/`
- Test documentation under `test-docs/`
- Espresso & UIAutomator tests under the app's `androidTest` source set
- Unit tests under the app's `test` source set

**All code, comments, documentation, and commit messages must be in English.**

## Required Reading

Before writing any code, open these files in order:

| # | File | Why |
|---|------|-----|
| 1 | `CONTRIBUTING.md` | Branch naming, commit format, PR rules, what NOT to do |
| 2 | `test-docs/test-case-plan.md` | Find your TC-ID range, module, and required testing methods |
| 3 | `test-docs/test-cases.md` | See existing TC specs for format reference |
| 4 | `app/.../utils/TestHelper.kt` | Understand shared utilities before using them |
| 5 | Reference examples (see patterns below) | Copy the pattern matching your testing method |

Then scan the relevant app source files for your feature.

## Complete Workflow

When you receive a TC assignment, execute these steps in order. Do NOT skip steps.

### 1. Plan
- Read your TC range from `test-docs/test-case-plan.md`
- Identify which testing methods you must use (Espresso / UIAutomator / Unit / Integration / Manual / Perf)
- Read the existing TC specs in `test-docs/test-cases.md` for format reference

### 2. Code
- Create test class(es) in the correct source directory (see [Where Files Live](#where-files-live))
- Copy the pattern from the relevant template in [Test Writing Patterns](#test-writing-patterns)
- Use `TestHelper` methods for DB setup and cleanup
- Every class must have `@author Your English Name` in its KDoc

### 3. Compile
```bash
cd app-under-test/antennapod

# For instrumented tests (Espresso, UIAutomator, Integration):
./gradlew :app:compilePlayDebugAndroidTestSources

# For unit tests (JUnit):
./gradlew :app:compilePlayDebugUnitTestSources
```
Fix all compilation errors. Do NOT proceed until this passes.

### 4. Run
```bash
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=<full.package.ClassName>
```
Re-run if flaky. Log the result (Passed / Partial / Failed).

### 5. Document
Update these files with your results. Follow the existing format — don't invent new columns or layouts.

| File | What to update |
|------|---------------|
| `test-docs/test-cases.md` | Append your TC specs |
| `test-results/manual-test-result.md` | Add your rows to the Results table. Update Summary counters |
| `test-docs/test-summary-report.md` | Add key findings for your tests. Update the counters |

### 6. Commit
```bash
# NEVER use git add -A or git add .
# Stage only your files individually:
git add path/to/your/test.kt
git add test-docs/test-cases.md

# Commit with the required format:
git commit -m "$(cat <<'EOF'
<type>: <short description>

Author: <Your English Name>
EOF
)"
```

**Commit types**: `test` (new test), `fix` (bug fix), `docs` (documentation only)

### 7. Push & PR
```bash
git checkout -b tc/<your-name>/<TC-range>
git push -u origin tc/<your-name>/<TC-range>
```
Then open a PR on GitHub. The PR template (`.github/pull_request_template.md`) loads automatically — fill in all sections.

### Red Flags

| Level | Rule |
|-------|------|
| Hard no | Changing `app/src/main/` — never modify app source code |
| Hard no | Committing generated files (`.class`, `.dex`, build outputs) |
| Hard no | Using `git add -A` — stage files individually |
| Needs PR note | Modifying `build.gradle` or `libs.versions.toml` — explain why in PR description |
| Needs fix | Compilation fails — fix before committing |
| Needs fix | Tests fail consistently — don't force-commit, investigate first |
| Always OK | Adding test files, updating docs, adding test-only dependencies |

## Environment Setup

```bash
# Required
export JAVA_HOME=<path-to-jdk-17>
export ANDROID_HOME=<path-to-android-sdk>

# Verify device is connected
adb devices

# Disable animations for stable Espresso tests
adb shell settings put global window_animation_scale 0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global animator_duration_scale 0.0
```

Build and run tests:
```bash
cd app-under-test/antennapod

# Compile only (fast check)
./gradlew :app:compilePlayDebugAndroidTestSources

# Run specific test class
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=de.danoeh.antennapod.espresso.TC001_AppLaunchTest
```

## Where Files Live

```
app-under-test/antennapod/app/src/
├── androidTest/java/de/danoeh/antennapod/
│   ├── espresso/        ← Espresso UI tests (in-app clicks, ViewActions)
│   ├── uiautomator/     ← UIAutomator tests (cross-app, system UI)
│   ├── integration/     ← SQLite, ContentProvider integration tests
│   ├── performance/     ← Benchmark tests (startup time, memory)
│   └── utils/           ← Shared test utilities (TestHelper.kt)
├── test/java/de/danoeh/antennapod/
│   ├── unit/            ← JUnit unit tests (no Android dependency)
│   └── manual/          ← Manually executed test code
└── main/java/de/danoeh/antennapod/  ← App source (read-only reference)

test-docs/
├── test-plan.md
├── test-case-plan.md
├── test-cases.md
├── bug-report-template.md
└── test-summary-report.md

bug-reports/
└── bug-XXX.md

automation/
├── run-tests.sh
└── README.md
```

## Key App Architecture

AntennaPod uses a bottom-navigation + drawer structure in `MainActivity`:

| Tab | Role | Key Classes |
|-----|------|------------|
| Home | `HomeFragment` | Welcome / recent episodes |
| Queue | `QueueFragment` | Episodes queued for playback |
| Subscriptions | `SubscriptionFragment` | Subscribed podcast feeds |
| Discovery | `DiscoveryFragment` | Browse and search podcasts |

Key activities beyond MainActivity:

| Activity | Role |
|----------|------|
| `PreferenceActivity` | App settings |
| `OpmlImportActivity` | Import OPML subscriptions |
| `OnlineFeedViewActivity` | Preview feed before subscribing |
| `VideoplayerActivity` | Video podcast playback |

Database: raw SQLite via `PodDBAdapter` (extends `SQLiteOpenHelper`). Key tables: Feeds, FeedItems, FeedMedia.

Player: `AudioPlayerFragment` (persistent bottom sheet) with Media3/ExoPlayer backend.

## Test Writing Patterns

### Pattern 1: Espresso UI Test

```kotlin
package de.danoeh.antennapod.espresso

@RunWith(AndroidJUnit4::class)
class TCXXX_TitleTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun descriptiveName_expectedBehavior() {
        onView(withId(R.id.some_view)).check(matches(isDisplayed()))
    }
}
```

### Pattern 2: UIAutomator Test

```kotlin
@RunWith(AndroidJUnit4::class)
class TCXXX_TitleTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun crossAppInteraction() {
        device.wait(Until.hasObject(By.pkg("com.android.documentsui")), 5000)
        device.findObject(By.text("Open")).click()
    }
}
```

### Pattern 3: Unit Test

```kotlin
// File: app/src/test/java/de/danoeh/antennapod/unit/XXXTest.kt
class XXXTest {
    @Test
    fun methodName_scenario_expectedBehavior() {
        val result = SomeHelper.process(input)
        assertEquals(expected, result)
    }
}
```

### Pattern 4: SQLite Integration Test

```kotlin
@RunWith(AndroidJUnit4::class)
class XXXTest {
    private lateinit var adapter: PodDBAdapter

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        adapter = PodDBAdapter(context)
        adapter.open()  // creates DB in memory or on disk
    }

    @After
    fun tearDown() { adapter.close() }

    @Test
    fun dbQuery_condition_expectedBehavior() {
        val cursor = adapter.someQuery()
        assertTrue(cursor.moveToFirst())
    }
}
```

## Capturing Screenshots

Screenhots are test evidence. Store them in `screenshots/` at the project root.

Quality over quantity — capture only meaningful UI states, not every test step.

### Recommended: TestHelper.saveScreenshot()

```kotlin
TestHelper.saveScreenshot("tc001-launch-home")
```

Uses `UiAutomation.takeScreenshot()` internally. Pull files after test run:

```bash
# On Linux/macOS:
adb pull /sdcard/Android/data/de.danoeh.antennapod.debug/files/screenshots/ ./screenshots/

# On Windows (Git Bash):
MSYS2_ARG_CONV_EXCL="*" adb pull /sdcard/Android/data/de.danoeh.antennapod.debug/files/screenshots/ ./screenshots/
```

### When to capture

| Scenario | Reason |
|----------|--------|
| Bug reproduction | Show the exact visual state of a defect |
| Manual test evidence | Prove a manual test step was executed |
| Cross-app flows | Capture system dialogs Espresso can't view |
| Visual regression | Document theme changes, layout issues |

### Do NOT capture

| Anti-pattern | Why |
|-------------|-----|
| Every test step | Screenshots are evidence, not a step-by-step log |
| Duplicate UI states | Same screen twice — pick one |
| Empty or blank screens | Wait for the view to load first |
| Loading spinners | Transient states are not meaningful evidence |

### Naming convention

`<tc-id>-<short-description>.png` — e.g., `tc001-launch-home.png`, `tc002-opml-import.png`

## Common Pitfalls

1. **Animations Cause Espresso Failures**
   → Always disable the three animation scales on the test device before running Espresso tests.

2. **MainActivity Bottom Nav**
   → Use `onView(withId(R.id.bottom_navigation))` or click nav items by ID. The app uses both bottom nav and a side drawer.

3. **PodDBAdapter Requires Manual open()/close()**
   → Unlike Room, `PodDBAdapter` does not auto-manage connections. Call `adapter.open()` in `@Before` and `adapter.close()` in `@After`.

4. **Network Calls in Tests**
   → AntennaPod fetches podcast feeds over the network. Use local test data or mock HTTP responses for reproducible tests.

5. **Flaky Tests After App Data Clear**
   → `pm clear de.danoeh.antennapod.debug` resets the app. The first test run after may fail because of first-launch state. Run tests twice if needed.
