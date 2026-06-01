# PROGRESS.md — AI Session State

> Read this first. Update it in real-time — after every compile, every test run, every fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working for** | Member 3 (Yuanbing Wang) |
| **Module** | Playback & Downloads |
| **Device** | MuMu emulator (ALN-AL00, Android 12, 127.0.0.1:7555) |
| **Last updated** | 2026-06-01 |

## Right Now

Sprint 3 complete. TC-021 through TC-030 have been implemented. TC-026 (11/11) and TC-027 (11/11) unit tests passed. TC-021~025, TC-028~029 compiled and pending device run. TC-030 manual checklist ready.

```
Status: READY - Sprint 3 TC-021 through TC-030 implemented
```

## Next Action

```bash
# Optional next action: execute TC-030 manually on a device/emulator.
# Compile instrumented tests:
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugAndroidTestSources
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
- [x] TC-031 `espresso/TC031_ThemeDisplaySettingsTest.kt` - compiled, pending device run
- [x] TC-032 `espresso/TC032_StorageNetworkPreferencesTest.kt` - compiled, pending device run
- [x] TC-033 `uiautomator/TC033_RuntimePermissionHandlingTest.kt` - compiled, pending device run
- [x] TC-034 `uiautomator/TC034_NotificationChannelSettingsTest.kt` - compiled, pending device run
- [x] TC-035 `unit/TC035_UserPreferencesTest.kt` - 8/8 passed
- [x] TC-036 `unit/TC036_StoragePathValidationTest.kt` - 5/5 passed
- [x] TC-037 `integration/TC037_DataExportImportIntegrityTest.kt` - compiled, pending device run
- [x] TC-038 `integration/TC038_EpisodeCacheCleanupTest.kt` - compiled, pending device run
- [x] TC-039 `performance/TC039_StartupMemoryBenchmarkTest.kt` - compiled, pending device run
- [x] TC-040 `manual/TC040_AccessibilityEdgeCasesTest.kt` - checklist ready
- [x] TC-021 `espresso/TC021_PlayPauseControlsTest.kt` - compiled, pending device run
- [x] TC-022 `espresso/TC022_PlaybackSpeedAdjustmentTest.kt` - compiled, pending device run
- [x] TC-023 `espresso/TC023_DownloadEpisodeForOfflinePlaybackTest.kt` - compiled, pending device run
- [x] TC-024 `uiautomator/TC024_AudioFocusPlaybackNotificationTest.kt` - compiled, pending device run
- [x] TC-025 `uiautomator/TC025_BackgroundPlaybackContinuityTest.kt` - compiled, pending device run
- [x] TC-026 `unit/TC026_PlaybackStateMachineLogicTest.kt` - 11/11 passed
- [x] TC-027 `unit/TC027_DownloadQueuePriorityLogicTest.kt` - 11/11 passed
- [x] TC-028 `integration/TC028_FeedMediaDaoReadWriteIntegrityTest.kt` - compiled, pending device run
- [x] TC-029 `integration/TC029_EpisodeDownloadStatusTrackingTest.kt` - compiled, pending device run
- [x] TC-030 `manual/TC030_LongPlaybackStabilityTest.kt` - checklist ready

## Files Created (Sprint 3)

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

## Files Created (Sprint 3)

```
espresso/TC021_PlayPauseControlsTest.kt
espresso/TC022_PlaybackSpeedAdjustmentTest.kt
espresso/TC023_DownloadEpisodeForOfflinePlaybackTest.kt
uiautomator/TC024_AudioFocusPlaybackNotificationTest.kt
uiautomator/TC025_BackgroundPlaybackContinuityTest.kt
unit/TC026_PlaybackStateMachineLogicTest.kt
unit/TC027_DownloadQueuePriorityLogicTest.kt
integration/TC028_FeedMediaDaoReadWriteIntegrityTest.kt
integration/TC029_EpisodeDownloadStatusTrackingTest.kt
manual/TC030_LongPlaybackStabilityTest.kt
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
| 3 | Member 3 (Yuanbing Wang) | Playback & Downloads | Done |
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
- [x] TC-021 Play/Pause (Espresso) — compiled, pending device run
- [x] TC-022 Playback Speed (Espresso) — compiled, pending device run
- [x] TC-023 Download Episode (Espresso) — compiled, pending device run
- [x] TC-024 Audio Focus (UIAutomator) — compiled, pending device run
- [x] TC-025 Background Playback (UIAutomator) — compiled, pending device run
- [x] TC-026 Playback State Machine (Unit) — 11/11 passed
- [x] TC-027 Download Queue Priority (Unit) — 11/11 passed
- [x] TC-028 FeedMedia DAO (Integration) — compiled, pending device run
- [x] TC-029 Download Status Tracking (Integration) — compiled, pending device run
- [x] TC-030 Long Playback Stability (Manual) — checklist ready

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
- [x] TC-040 Accessibility (Manual) - checklist ready
