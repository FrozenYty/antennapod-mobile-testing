# PROGRESS.md — AI Session State

> Read this first. Update it in real-time — after every compile, every test run, every fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working for** | Member Four |
| **Module** | Settings & System |
| **Device** | MuMu emulator (ALN-AL00, Android 12, 127.0.0.1:7555) |
| **Last updated** | 2026-05-31 |

## Right Now

Sprint 4 in progress. TC-031 through TC-034 and TC-037 through TC-039 have been implemented and compiled. TC-035 and TC-036 passed.

```
Status: IN PROGRESS — Sprint 4 TC-039 compiled
```

## Next Action

```bash
# Verify TC-040:
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugUnitTestSources
```

---

## Done

- [x] TC-001 `espresso/TC001_AppLaunchTest.kt` — 6/6
- [x] TC-002 `espresso/TC002_SubscribePodcastTest.kt` — 4/4
- [x] TC-003 `espresso/TC003_PlayEpisodeTest.kt` — 4/4
- [x] TC-004 `espresso/TC004_QueueManagementTest.kt` — 4/4
- [x] TC-005 `espresso/TC005_SearchDiscoveryTest.kt` — 4/4
- [x] TC-006 `uiautomator/TC006_OpmlImportTest.kt` — 3/3
- [x] TC-007 `unit/TC007_FeedEntityTest.kt` — 17/17
- [x] TC-008 `unit/TC008_FeedItemFeedMediaTest.kt` — 34/34
- [x] TC-009 `integration/TC009_PodDBAdapterSchemaTest.kt` — 8/8
- [x] TC-010 `manual/TC010_FirstLaunchUserFlowTest.kt` — 19/20
- [x] CI: compile → unit test → doc check on push to main
- [x] Docs synced, no stale refs, 9 screenshots
- [x] TC-011 `espresso/TC011_BrowseDiscoveryTest.kt` — compiled, pending device run
- [x] TC-012 `espresso/TC012_SubscribeDiscoveryTest.kt` — compiled, pending device run
- [x] TC-013 `espresso/TC013_UnsubscribeDeleteTest.kt` — compiled, pending device run
- [x] TC-014 `uiautomator/TC014_ShareFeedUrlTest.kt` — compiled, pending device run
- [x] TC-015 `uiautomator/TC015_FeedRefreshTest.kt` — compiled, pending device run
- [x] TC-016 `unit/TC016_FeedUrlParsingTest.kt` — 24/24 passed
- [x] TC-017 `unit/TC017_SortFilterLogicTest.kt` — 23/23 passed
- [x] TC-018 `integration/TC018_FeedItemDaoTest.kt` — compiled, pending device run
- [x] TC-019 `performance/TC019_FeedParsingBenchmarkTest.kt` — compiled, pending device run
- [x] TC-020 `manual/TC020_DiscoveryUsabilityTest.kt` — checklist ready

## Files Created (this sprint)

```
TestHelper.kt
espresso/TC001_AppLaunchTest.kt
espresso/TC002_SubscribePodcastTest.kt
espresso/TC003_PlayEpisodeTest.kt
espresso/TC004_QueueManagementTest.kt
espresso/TC005_SearchDiscoveryTest.kt
uiautomator/TC006_OpmlImportTest.kt
unit/TC007_FeedEntityTest.kt
unit/TC008_FeedItemFeedMediaTest.kt
integration/TC009_PodDBAdapterSchemaTest.kt
manual/TC010_FirstLaunchUserFlowTest.kt
```

## Blockers & Decisions

| Date | What |
|------|------|
| 05-28 | Branch+auto-merge CI caused repeated merge conflicts → switched to branchless workflow (push directly to main) |
| 05-28 | CI runs pre-existing AntennaPod unit tests → added `--tests` filter to only run our packages |
| 05-28 | `ActivityScenarioRule` broken on MuMu → use `ActivityTestRule(false, false)` |
| 05-28 | `bottom_navigation_addfeed` not in default visible items → test `bottom_navigation_more` instead |
| 05-28 | Screenshots deleted on test APK uninstall → save to `/storage/emulated/0/Download/screenshots/` |
| 05-28 | PR workflow failed 4 times (merge conflicts, missing origin/main, git identity) → replaced with direct squash-merge |
| 05-28 | CI `git merge --squash` needs `git config user.name/email` → added "CI Bot" identity |
| 05-28 | `PodDBAdapter(context)` constructor doesn't exist → use `init(context)` + `getInstance()` |
| 05-28 | `SortOrder` is enum, not constructable → use `SortOrder.DATE_NEW_OLD` |
| 05-28 | Feed cursor column is `feed_id` (SELECT_KEY_FEED_ID), not `id` (KEY_ID) |

## Command Cheatsheet

```bash
# Compile
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugAndroidTestSources    # instrumented
./gradlew :app:compilePlayDebugUnitTestSources        # unit

# Run specific test
export ANDROID_SERIAL=127.0.0.1:7555
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=de.danoeh.antennapod.espresso.TC001_AppLaunchTest

# Run all unit tests
./gradlew :app:testPlayDebugUnitTest

# Screenshots
adb shell "uiautomator dump /sdcard/ui.xml"  # find view coordinates
MSYS2_ARG_CONV_EXCL="*" adb pull /storage/emulated/0/Download/screenshots/ ./screenshots/
```

---

## Sprint Overview

| Sprint | Member | Module | Status |
|--------|--------|--------|--------|
| 1 | Tianyu Yao | Core Foundation | Done |
| 2 | Member 2 | Subscription & Discovery | — |
| 3 | Member 3 | Playback & Downloads | — |
| 4 | Member 4 | Settings & System | — |

### Sprint 2 Task Board
- [x] TC-011 Browse Discovery (Espresso) — compiled, pending device run
- [x] TC-012 Subscribe from Discovery (Espresso) — compiled, pending device run
- [x] TC-013 Unsubscribe & Delete (Espresso) — compiled, pending device run
- [x] TC-014 Share Feed URL (UIAutomator) — compiled, pending device run
- [x] TC-015 Feed Refresh (UIAutomator) — compiled, pending device run
- [x] TC-016 Feed URL Parsing (Unit) — 24/24 passed
- [x] TC-017 Sort & Filter Logic (Unit) — 23/23 passed
- [x] TC-018 Feed & FeedItem DAO (Integration) — compiled, pending device run
- [x] TC-019 Feed Parsing Speed (Performance) — compiled, pending device run
- [x] TC-020 Discovery Usability (Manual) — checklist ready

### Sprint 3 Task Board
- [ ] TC-021 Play/Pause (Espresso)
- [ ] TC-022 Playback Speed (Espresso)
- [ ] TC-023 Download Episode (Espresso)
- [ ] TC-024 Audio Focus (UIAutomator)
- [ ] TC-025 Background Playback (UIAutomator)
- [ ] TC-026 Playback State Machine (Unit)
- [ ] TC-027 Download Queue Priority (Unit)
- [ ] TC-028 FeedMedia DAO (Integration)
- [ ] TC-029 Download Status Tracking (Integration)
- [ ] TC-030 Long Playback Stability (Manual)

### Sprint 4 Task Board
- [x] TC-031 Theme & Display (Espresso) — compiled, pending device run
- [x] TC-032 Storage & Network Prefs (Espresso) — compiled, pending device run
- [x] TC-033 Permission Handling (UIAutomator) — compiled, pending device run
- [x] TC-034 Notification Channels (UIAutomator) — compiled, pending device run
- [x] TC-035 User Preferences Logic (Unit) — 8/8 passed
- [x] TC-036 Storage Path Validation (Unit) — 5/5 passed
- [x] TC-037 Data Export/Import (Integration) — compiled, pending device run
- [x] TC-038 Episode Cache Cleanup (Integration) — compiled, pending device run
- [x] TC-039 Startup Time & Memory (Performance) — compiled, pending device run
- [ ] TC-040 Accessibility (Manual)
