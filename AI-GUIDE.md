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
| **1** | **`PROGRESS.md`** | **Current task status, what's done, what's pending. Read this first.** |
| 2 | `CONTRIBUTING.md` | Commit format, file organization, what NOT to do |
| 3 | `test-docs/test-case-plan.md` | Find your TC-ID range, module, and required testing methods |
| 4 | `test-docs/test-cases.md` | See existing TC specs for format reference |
| 5 | `app/.../utils/TestHelper.kt` | Understand shared utilities before using them |
| 6 | Reference examples (see patterns below) | Copy the pattern matching your testing method |

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
| `PROGRESS.md` | Update your TC status rows and remaining actions |
| `test-docs/test-cases.md` | Append your TC specs |
| `test-results/manual-test-result.md` | Add your rows to the Results table. Update Summary counters |
| `test-docs/test-summary-report.md` | Add key findings for your tests. Update the counters |

### 6. Commit
```bash
# NEVER use git add -A or git add .
# Stage only your files individually:
git pull
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

### 7. Push
```bash
git push
```
Push triggers CI on main. If CI passes, your changes are live.

### Red Flags

| Level | Rule |
|-------|------|
| Hard no | Changing `app/src/main/` — never modify app source code |
| Hard no | Committing generated files (`.class`, `.dex`, build outputs) |
| Hard no | Using `git add -A` — stage files individually |
| Remember | `git pull` before committing to avoid push conflicts |
| Needs note | Modifying `build.gradle` or `libs.versions.toml` — explain why in commit message |
| Needs fix | Compilation fails — fix before committing |
| Needs fix | Tests fail consistently — don't force-commit, investigate first |
| Always OK | Adding test files, updating docs, adding test-only dependencies |

## Environment Setup

```bash
# Required
export JAVA_HOME=<path-to-jdk-21>
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

AntennaPod uses a customizable bottom-navigation bar (`R.id.bottomNavigationView`) plus
drawer layout (`R.id.drawer_layout`) in `MainActivity` (`de.danoeh.antennapod.activity.MainActivity`).

Bottom nav IDs (from `res/values/ids.xml`): `bottom_navigation_home`, `bottom_navigation_queue`,
`bottom_navigation_inbox`, `bottom_navigation_episodes`, `bottom_navigation_subscriptions`,
`bottom_navigation_downloads`, `bottom_navigation_favorites`, `bottom_navigation_addfeed`, etc.

Key fragments and their packages:

| Fragment | Location |
|----------|----------|
| `HomeFragment` | `de.danoeh.antennapod.ui.screen.home` |
| `QueueFragment` | `de.danoeh.antennapod.ui.screen.queue` |
| `SubscriptionFragment` | `de.danoeh.antennapod.ui.screen.subscriptions` |
| `DiscoveryFragment` | `:ui:discovery` module — `de.danoeh.antennapod.ui.discovery` |

Key activities:

| Activity | Package |
|----------|---------|
| `MainActivity` | `de.danoeh.antennapod.activity` |
| `PreferenceActivity` | `de.danoeh.antennapod.ui.screen.preferences` |
| `OpmlImportActivity` | `de.danoeh.antennapod.activity` |
| `OnlineFeedViewActivity` | `de.danoeh.antennapod.ui.screen.onlinefeedview` |

Database: raw SQLite via `PodDBAdapter` (`:storage:database` module). Key tables: Feeds, FeedItems, FeedMedia.

Core data model (`:model` module): `de.danoeh.antennapod.model.feed.Feed`, `FeedItem`, `FeedMedia`.

Player: persistent player sheet with Media3/ExoPlayer backend (`:playback:service`).

## Test Writing Patterns

### Pattern 1: Espresso UI Test

```kotlin
package de.danoeh.antennapod.espresso

@RunWith(AndroidJUnit4::class)
class TCXXX_TitleTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun descriptiveName_expectedBehavior() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
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
        PodDBAdapter.init(context)
        adapter = PodDBAdapter.getInstance()
    }

    @After
    fun tearDown() { PodDBAdapter.tearDownTests() }

    @Test
    fun dbQuery_condition_expectedBehavior() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Test Feed")
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://example.com/feed.xml")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)

        val cursor = adapter.allFeedsCursor
        // NOTE: use PodDBAdapter.SELECT_KEY_FEED_ID ("feed_id"), not KEY_ID ("id")
        val feedId = cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID))
        assertTrue(cursor.count > 0)
        cursor.close()
    }
}
```

## Capturing Screenshots

Screenshots are test evidence. Store them in `screenshots/` at the project root.

Read `screenshots/README.md` **before you capture any screenshots**. The rule is: **Better none than junk.**

Quality over quantity — capture only meaningful UI states, not every test step.

### Mandatory Screenshot Review Before Commit

Before `git add`-ing any new screenshot:

1. Compare the new screenshot against ALL existing screenshots in `screenshots/`
2. If the new screenshot shows the same UI state as an existing one, **delete the new one immediately**
3. If you cannot explain the screenshot's value in one sentence, **delete it**
4. Only commit screenshots that represent a **unique UI state** not yet captured

Common duplicates to watch for:
- Same tab/screen from a different TC (e.g., queue page from TC-003 and queue page from TC-004)
- Home screen captured multiple times across different tests
- Transient states (loading spinners, empty screens without context)

### Recommended: TestHelper.saveScreenshot()

```kotlin
TestHelper.saveScreenshot("tc001-launch-home")
```

Uses `UiAutomation.takeScreenshot()` internally. Screenshots are saved to `Download/screenshots/` on the device so they survive test APK uninstall. Pull files after test run:

```bash
# Pull all screenshots after test run:
MSYS2_ARG_CONV_EXCL="*" adb pull /storage/emulated/0/Download/screenshots/ ./screenshots/

# Remove test-generated screenshots from device:
adb shell rm -rf /storage/emulated/0/Download/screenshots/
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

2. **Bottom Nav ID is `bottomNavigationView`, not `bottom_navigation`**
   → The bottom navigation bar View ID is `R.id.bottomNavigationView`. The individual nav item IDs (e.g. `bottom_navigation_home`, `bottom_navigation_queue`) are defined in `res/values/ids.xml` and used as menu item IDs. The nav bar itself is at `main.xml:57`.

3. **Bottom Nav Items Are Dynamic**
   → `BottomNavigation.buildMenu()` populates the bar from `UserPreferences.getVisibleDrawerItemOrder()`. Only the first `maxItems - 1` items appear directly; the rest go into a "More" overflow popup. On first launch, visible items are typically Home, Subscriptions, Queue, Inbox + More. Items like `addfeed`, `downloads`, `favorites` may be hidden in the More popup.

4. **PodDBAdapter Uses Singleton Pattern**
   → Unlike Room DAOs, `PodDBAdapter` is a singleton: call `PodDBAdapter.init(context)` once, then `PodDBAdapter.getInstance()`. Clean up with `PodDBAdapter.tearDownTests()`. The `open()` and `close()` methods are no-ops — do not rely on them.

5. **PodDBAdapter Cursor Column Names Differ from Table Column Names**
   → Cursors returned by `getAllFeedsCursor()` use `SELECT_KEY_FEED_ID` ("feed_id"), not `KEY_ID` ("id"). Similarly, `getItemsOfFeedCursor()` returns `SELECT_KEY_ITEM_ID` ("item_id") and `SELECT_KEY_MEDIA_ID` ("media_id"). Always use the `SELECT_KEY_*` constants when reading from cursors.

6. **SortOrder Is an Enum, Not a Class**
   → `SortOrder` values are enum constants (e.g. `SortOrder.DATE_NEW_OLD`), not constructable. For Feed's `setSortOrder()`, only values with `INTRA_FEED` scope are accepted — values with `INTER_FEED` scope throw `IllegalArgumentException`.

7. **MuMu Emulator Requires `ActivityTestRule`**
   → On MuMu emulator (reported as "ALN-AL00", Android 12), `ActivityScenarioRule` / `ActivityScenario.launch()` fails with "Activity never becomes requested state [RESUMED]". Use `ActivityTestRule(MainActivity::class.java, false, false)` + `launchActivity(Intent(Intent.ACTION_MAIN))` instead.

8. **Screenshots Must Survive Test Cleanup**
   → `connectedAndroidTest` uninstalls the test APK after running, which deletes the app's private external storage. Save screenshots to `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)` so they persist across test runs and uninstalls. Pull with:
   ```bash
   MSYS2_ARG_CONV_EXCL="*" adb pull /storage/emulated/0/Download/screenshots/ ./screenshots/
   ```

9. **Network Calls in Tests**
   → AntennaPod fetches podcast feeds over the network. For reproducible tests, subscription and playback TCs should either use pre-loaded test data via `PodDBAdapter.insertTestData()`, or mock HTTP responses. Tests that navigate to subscription/playback UI can still verify tab navigation without network content.

10. **Flaky Tests After App Data Clear**
    → `pm clear de.danoeh.antennapod.debug` resets the app. The first test run after may fail because of first-launch state. Run tests twice if needed. On emulators, prefer `./gradlew :app:installPlayDebug` to reinstall rather than clearing data.

---

## Lessons from Sprint 1 (TC-001~010) — Read Before Starting Your Sprint

These are issues the team lead hit during the first sprint. Review them before you write any code.

### Screenshot Discipline

This was the #1 quality issue. Read `screenshots/README.md` carefully. Specific rules from experience:

- **Before `git add`-ing any new screenshot, visually diff it against ALL existing screenshots in the directory.** We ended up with 11 initial screenshots from which only 5 were unique. After cleanup and selective additions from manual testing, the final count is 9 unique screenshots — each representing a distinct UI state.
- **Screenshot placement matters.** Put `TestHelper.saveScreenshot()` AFTER the navigation action (e.g. `perform(click())`), not before. Otherwise you capture the previous screen.
- **The first screenshot per unique UI state is the one that stays.** If TC-003 already captured `tc003-queue.png`, do NOT capture another queue screenshot for TC-004.

### Test Adaptation for Network/Content Constraints

Some TC titles in the plan assume network access or pre-existing app content (subscribed feeds, downloaded episodes). When these are unavailable on your test device:

- **It's OK to adapt the test** — the test plan says titles are flexible. Focus on what you CAN verify with your method.
- **Document the adaptation** in `test-cases.md` under an "Adaptation" note for your TC.
- **Examples from Sprint 1**: TC-002 "Subscribe to Podcast" became bottom nav + More menu verification. TC-003 "Play Episode" became tab navigation verification. TC-006 "OPML Import" became UIAutomator view detection verification.

### build.gradle Changes

The team lead already added the `kotlin-android` plugin and `uiautomator` test dependency. You should NOT need to modify `build.gradle` or `libs.versions.toml` further. If your test requires a new library, discuss with the team lead first.

### TestHelper Is Already Created

`TestHelper.kt` lives in `utils/` and provides `saveScreenshot(name)`. Do NOT create your own screenshot utility — use this one. Its save path is `/storage/emulated/0/Download/screenshots/` (public Downloads, survives test APK uninstall).

### Pattern to Follow for Espresso Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class TCXXX_XxxTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun descriptiveName_expectedBehavior() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        // optional: TestHelper.saveScreenshot("tcxxx-description") — only if unique state
        onView(withId(R.id.some_view)).check(matches(isDisplayed()))
    }
}
```

Key differences from the template in the earlier section:
- Uses `ActivityTestRule(false, false)` NOT `ActivityScenarioRule` (MuMu emulator compatibility)
- Uses `activityRule.launchActivity(Intent(Intent.ACTION_MAIN))` NOT `ActivityScenario.launch()`
- Screenshot call is AFTER key actions, not before
