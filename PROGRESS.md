# PROGRESS.md — AI Session State

> Read this first. Update it in real-time — after every compile, every test run, every fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working for** | Frida Dynamic Call Graph |
| **Module** | automation/ |
| **Device** | MuMu emulator (ALN-AL00, Android 12, x86_64, 127.0.0.1:7555) |
| **Frida version** | 17.10.1 (tools 14.9.0, server android-x86_64) |
| **Last updated** | 2026-06-07 |

## Right Now

**Frida dynamic call graph — WORKING.** CLI subprocess mode (`frida -q -t -o`),
Exception-based caller detection, ADB-simulated interaction. HTML report with
interactive network graph generated and opened in browser.

```
Status: DONE — 11 classes, 56 methods hooked, 4 unique edges (6 calls)
```

---

## Frida Call Graph — Complete Debug Journal

### Phase 1: Environment Setup ✅

Successfully set up frida-server 17.10.1 on MuMu Android 12 x86_64:
- Download: `frida-server-17.10.1-android-x86_64.xz` from GitHub releases
- Push via MSYS2: `MSYS2_ARG_CONV_EXCL="*" adb push ... /data/local/tmp/frida-server`
- Must run `adb root` BEFORE starting frida-server (otherwise "jailed Android" error)
- Verified: `frida-ps -U` lists all processes correctly

### Phase 2: Python API attempts — ALL FAILED ❌

**Core finding**: `frida.get_usb_device().attach(pid).create_script(js).load()` never
makes `Java` available in the script runtime, regardless of:
- Attach mode: `attach(pid)` / `attach('AntennaPod Debug')` / `spawn([pkg])`
- Runtime: `runtime='v8'` / default
- Timing: wait 5s after attach before creating script

**Root cause hypothesis**: The `Java` global is created by Frida core's C code when it
detects the target is an ART/Dalvik process. The `frida` CLI (`frida -U -p <PID>`)
triggers this detection, but `frida.get_usb_device().attach(pid)` via Python API does NOT.
The exact mechanism in frida-core that enables the Java bridge remains unknown.

**Attempted workaround — frida-tools bridge protocol**: 
frida-tools internally uses a message protocol to load `bridges/java.js`:
1. JS script sends: `send({type: 'frida:load-bridge', name: 'java'})`
2. Python handler reads `java.js` (231 KB) and calls `script.post({type: 'frida:bridge-loaded', source: ...})`
3. We implemented this protocol in `frida_callgraph.py`

But `(1, eval)(java.js_source)` in the JS runtime did NOT create the `Java` global.
The java.js file likely requires the native C bridge to be pre-initialized.

### Phase 3: CLI subprocess approach — PARTIALLY WORKS ⚠️

Switched to spawning `frida` CLI as subprocess and parsing stdout JSON lines.

**Key discoveries about frida CLI**:
- `-q` (quiet mode): no REPL prompt, suppresses banner
- `-t N` (timeout): keeps running N seconds then exits (only works with `-q`)
- Without `-q`, REPL blocks event loop → setTimeout doesn't fire
- With `-q -t N`, setTimeout DOES fire (verified with test3.js)
- Need `setInterval(()=>{}, 60000)` keep-alive to prevent immediate exit

**Blocking issue — app crash**:
When hooks_cli.js calls `Java.use('java.lang.Thread')`, the app crashes with:
```
TypeError: Cannot read properties of null (reading 'sleep')
    at xo._getUsedClass (/frida/bridges/java.js:8:43512)
```
This is a Frida 17.10.1 internal error when trying to look up `Thread.sleep()`.
The crash causes the frida process to exit immediately (before setTimeout fires).

**Attempted fix**: Switched to `Java.use('java.lang.Exception')` + `e.getStackTrace()`
for caller detection, avoiding `java.lang.Thread` entirely. NOT YET VERIFIED.

### Phase 4: Static code complete ✅

- `automation/frida_callgraph.py` — full Python orchestrator (CLI subprocess + jsonl parsing + matplotlib + HTML report)
- `automation/frida_hooks_cli.js` — CLI-compatible JS hook script (JSON console.log, Exception-based stack trace)
- `automation/frida_hooks.js` — original JS hook (Python API send/recv protocol, unused now)
- `automation/README.md` — complete documentation with usage, architecture, comparison table

---

## Lessons Learned

1. **Never assume API parity between CLI and Python binding.** `frida -U` and `frida.get_usb_device()` don't provide identical environments.
2. **The Java bridge is a C-core feature, not a JS file.** Loading `java.js` via eval doesn't create `Java` — the native bridge must be initialized first.
3. **Read the frida-tools SOURCE CODE early.** The `try_handle_bridge_request()` / `bridges/java.js` architecture could have been understood much earlier by reading `application.py`.
4. **Test minimal scripts incrementally.** The `test.js` → `test2.js` → `test3.js` → `hooks_cli.js` progression was correct but should have been done BEFORE writing the full orchestrator.
5. **`frida -q -t N` is the correct non-interactive mode.** Discovered very late via `frida --help` rather than documentation.
6. **Frida 17.10.1 has a Thread.sleep bug on Android 12.** `Java.use('java.lang.Thread')` crashes with `Cannot read properties of null (reading 'sleep')`. This is likely a known issue in newer Frida versions.
7. **Don't fight one approach for too long.** After 5+ failed Python API attempts, should have pivoted to CLI subprocess or gadget injection much earlier.

---

## Next Approach (Recommended)

### Option A: frida-gadget injection (most reliable)
Inject `frida-gadget.so` into the APK so the Java bridge is available natively:
- Add `frida-gadget-17.10.1-android-x86_64.so` to the APK's `lib/x86_64/` directory
- Configure via `libfrida-gadget.config.so` (JSON config embedding the JS hook)
- No frida-server needed on device
- Refs: https://frida.re/docs/gadget/

### Option B: Use `frida-inject` tool
`frida-inject` is designed for non-interactive script injection:
- `frida-inject -f <package> -s hooks.js` — simpler than `frida` CLI
- Might handle the Java bridge correctly

### Option C: Downgrade Frida version
The `Thread.sleep` crash might be fixed in other versions:
- Try Frida 16.x which may have different Java bridge behavior
- Try the latest dev build

### Option D: Use existing frida-tools infrastructure
Instead of building from scratch, extend `frida-trace` with custom handler generation:
- `frida-trace -U -f <pkg> -j 'de.danoeh.antennapod.*!*'` auto-generates hundreds of handler files
- Write a post-processing script that modifies each handler to also log caller info
- Already handles Java bridge correctly (it's part of frida-tools)

---

## Quick Resume

```bash
# 1. Reconnect
adb connect 127.0.0.1:7555
adb -s 127.0.0.1:7555 root

# 2. Start frida-server
adb -s 127.0.0.1:7555 shell "killall frida-server 2>/dev/null"
adb -s 127.0.0.1:7555 shell "/data/local/tmp/frida-server -D &"
frida-ps -U | grep -i antenna   # verify

# 3. Test Exception-based hooks (fix for Thread.sleep crash)
frida -U -f de.danoeh.antennapod.debug --runtime=v8 \
    -l automation/frida_hooks_cli.js -q -t 15

# 4. If that works, run full orchestrator:
python automation/frida_callgraph.py --device 127.0.0.1:7555 --duration 20

# 5. Alternative: try frida-inject
# pip install frida-tools  (already done)
# Check if frida-inject is available

# 6. Best bet for reliability: Option A (gadget) or Option D (frida-trace)
```

---

## Files Tracking

```
automation/frida_callgraph.py       # Python orchestrator (CLI subprocess + HTML + matplotlib)
automation/frida_hooks_cli.js       # CLI-compatible JS hook (Exception-based, JSON stdout)
automation/requirements.txt         # Updated: frida-tools>=12.0
automation/README.md                # Updated: full documentation
```

---

## Next Session Quick Start

```bash
# 1. Reconnect
adb connect 127.0.0.1:7555 && adb -s 127.0.0.1:7555 root

# 2. Start frida-server
adb -s 127.0.0.1:7555 shell "killall frida-server 2>/dev/null; /data/local/tmp/frida-server -D &"
sleep 2 && frida-ps -U | head -3

# 3. FIRST: Verify Exception-based hooks don't crash
frida -U -f de.danoeh.antennapod.debug --runtime=v8 \
    -l automation/frida_hooks_cli.js -q -t 15
# Look for: {"type":"debug","msg":"scanAndHook called"}
# If you see this, the Exception.getStackTrace() fix works!

# 4. If step 3 works, run full orchestrator:
python automation/frida_callgraph.py --device 127.0.0.1:7555 --duration 20 --no-browser

# 5. FORGET the Python API approach. Only use CLI subprocess (-q -t mode).
#    If CLI still crashes, switch to Option D (frida-trace -j) or Option A (gadget).
```

## Next Action

```bash
# Done! Frida call graph is working.
# Output: test-docs/callgraphs/frida-callgraph.html (opens in browser)
#         11 classes, 56 methods hooked, 4 unique call edges
# To re-run with more interaction:
python automation/frida_callgraph.py --device 127.0.0.1:7555 --duration 20
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
- [x] Docs synced, 24 screenshots
- [x] TC-011 `espresso/TC011_BrowseDiscoveryTest.kt` — 4/4 passed (fixed toolbar→appbar, grid→swipeRefresh)
- [x] TC-012 `espresso/TC012_SubscribeDiscoveryTest.kt` — 4/4 passed
- [x] TC-013 `espresso/TC013_UnsubscribeDeleteTest.kt` — 4/4 passed
- [x] TC-014 `uiautomator/TC014_ShareFeedUrlTest.kt` — 3/3 passed
- [x] TC-015 `uiautomator/TC015_FeedRefreshTest.kt` — 3/3 passed
- [x] TC-016 `unit/TC016_FeedUrlParsingTest.kt` — 24/24 passed
- [x] TC-017 `unit/TC017_SortFilterLogicTest.kt` — 23/23 passed
- [x] TC-018 `integration/TC018_FeedItemDaoTest.kt` — 8/8 passed
- [x] TC-019 `performance/TC019_FeedParsingBenchmarkTest.kt` — 3/3 passed
- [x] TC-020 `manual/TC020_DiscoveryUsabilityTest.kt` — 16/16 executable pass, 4 N/A, 4 screenshots
- [x] TC-021 `espresso/TC021_PlayPauseControlsTest.kt` — 4/4 passed (MuMu, fixed episodes→more)
- [x] TC-022 `espresso/TC022_PlaybackSpeedAdjustmentTest.kt` — 4/4 passed (MuMu)
- [x] TC-023 `espresso/TC023_DownloadEpisodeForOfflinePlaybackTest.kt` — 4/4 passed (MuMu, fixed episodes→more)
- [x] TC-024 `uiautomator/TC024_AudioFocusPlaybackNotificationTest.kt` - 3/3 passed (API 37)
- [x] TC-025 `uiautomator/TC025_BackgroundPlaybackContinuityTest.kt` - 3/3 passed (API 37)
- [x] TC-026 `unit/TC026_PlaybackStateMachineLogicTest.kt` - 11/11 passed
- [x] TC-027 `unit/TC027_DownloadQueuePriorityLogicTest.kt` - 10/10 passed
- [x] TC-028 `integration/TC028_FeedMediaDaoReadWriteIntegrityTest.kt` — 6/6 passed (API 37)
- [x] TC-029 `integration/TC029_EpisodeDownloadStatusTrackingTest.kt` — 7/7 passed (API 37)
- [x] TC-030 `manual/TC030_LongPlaybackStabilityTest.kt` — 20/20 N/A on emulator (needs physical device)
- [x] TC-031 `espresso/TC031_ThemeDisplaySettingsTest.kt` — 4/4 passed (MuMu, fixed pref_tinted_theme_title)
- [x] TC-032 `espresso/TC032_StorageNetworkPreferencesTest.kt` — 4/4 passed (MuMu)
- [x] TC-033 `uiautomator/TC033_RuntimePermissionHandlingTest.kt` — 3/3 passed (MuMu)
- [x] TC-034 `uiautomator/TC034_NotificationChannelSettingsTest.kt` — 3/3 passed (MuMu)
- [x] TC-035 `unit/TC035_UserPreferencesTest.kt` — 8/8 passed
- [x] TC-036 `unit/TC036_StoragePathValidationTest.kt` — 5/5 passed
- [x] TC-037 `integration/TC037_DataExportImportIntegrityTest.kt` — 4/4 passed (MuMu)
- [x] TC-038 `integration/TC038_EpisodeCacheCleanupTest.kt` — 4/4 passed (MuMu)
- [x] TC-039 `performance/TC039_StartupMemoryBenchmarkTest.kt` — 4/4 passed (MuMu)
- [x] TC-040 `manual/TC040_AccessibilityEdgeCasesTest.kt` — 17/17 pass, 3 N/A (MuMu)

## Files Created

### Sprint 1 (Tianyu Yao)
```
utils/TestHelper.kt
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

### Sprint 2 (Jianheng Sun)
```
espresso/TC011_BrowseDiscoveryTest.kt
espresso/TC012_SubscribeDiscoveryTest.kt
espresso/TC013_UnsubscribeDeleteTest.kt
uiautomator/TC014_ShareFeedUrlTest.kt
uiautomator/TC015_FeedRefreshTest.kt
unit/TC016_FeedUrlParsingTest.kt
unit/TC017_SortFilterLogicTest.kt
integration/TC018_FeedItemDaoTest.kt
performance/TC019_FeedParsingBenchmarkTest.kt
manual/TC020_DiscoveryUsabilityTest.kt
```

### Sprint 3 (Yuanbing Wang)
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

### Sprint 4 (Xintao Wang)
```
espresso/TC031_ThemeDisplaySettingsTest.kt
espresso/TC032_StorageNetworkPreferencesTest.kt
uiautomator/TC033_RuntimePermissionHandlingTest.kt
uiautomator/TC034_NotificationChannelSettingsTest.kt
unit/TC035_UserPreferencesTest.kt
unit/TC036_StoragePathValidationTest.kt
integration/TC037_DataExportImportIntegrityTest.kt
integration/TC038_EpisodeCacheCleanupTest.kt
performance/TC039_StartupMemoryBenchmarkTest.kt
manual/TC040_AccessibilityEdgeCasesTest.kt
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
| 05-31 | `UrlChecker` uses `android.util.Log.d()` → unit test needs `@RunWith(RobolectricTestRunner::class)` |
| 05-31 | `SortOrder.fromCodeString()` and `SubscriptionsFilter` use `TextUtils` → unit test needs Robolectric |
| 05-31 | `FeedOrder.fromOrdinal(id)` uses `id` field, not Java `ordinal()` — values are non-sequential |
| 05-31 | CI naming check requires `TC<NNN>_*Test.kt` — missing `Test` suffix causes CI failure |
| 05-31 | CI Gradle download timeout 10s too short → increased to 120s in gradle-wrapper.properties |
| 05-31 | Local network can't reach services.gradle.org → use `file\:/<path-to-gradle-dist>/gradle-8.13-bin.zip` locally, REVERT before commit |
| 05-31 | JDK 21 required, available at `<path-to-jdk-21>` — `export JAVA_HOME` before running Gradle |
| 05-31 | GitHub push: `git config user.name "chemflowers"`, `git config user.email "chemflowers@outlook.com"` |
| 06-01 | TC-011/013: `R.id.toolbar` ambiguous (2 toolbars in hierarchy) → use `R.id.appbar` (unique to subscriptions fragment) |
| 06-01 | TC-011/013: `subscriptions_grid` has empty globalVisibleRect when no feeds → use `R.id.swipeRefresh` instead |
| 06-02 | Espresso tests fail on API 37: `InputManager.getInstance()` removed → use API ≤34 emulator |
| 06-02 | TC-024: `bottom_navigation_episodes` not in default visible items → use `bottom_navigation_more` |
| 06-02 | TC-029: DownloadLog table has no `download_url` column → removed from ContentValues |
| 06-02 | TC-029: `getItemsOfFeedCursor` may omit `size` column → guard with `if (sizeIdx >= 0)` |

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
| 2 | Jianheng Sun | Subscription & Discovery | Done — all 10 TCs passed |
| 3 | Yuanbing Wang | Playback & Downloads | Done — all 10 TCs passed (52/52 automated) |
| 4 | Xintao Wang | Settings & System | Done — all 10 TCs passed (39/39 automated) |

### Sprint 2 Task Board
- [x] TC-011 Browse Discovery (Espresso) — 4/4 passed (fixed toolbar→appbar, grid→swipeRefresh)
- [x] TC-012 Subscribe from Discovery (Espresso) — 4/4 passed
- [x] TC-013 Unsubscribe & Delete (Espresso) — 4/4 passed
- [x] TC-014 Share Feed URL (UIAutomator) — 3/3 passed
- [x] TC-015 Feed Refresh (UIAutomator) — 3/3 passed
- [x] TC-016 Feed URL Parsing (Unit) — 24/24 passed
- [x] TC-017 Sort & Filter Logic (Unit) — 23/23 passed
- [x] TC-018 Feed & FeedItem DAO (Integration) — 8/8 passed
- [x] TC-019 Feed Parsing Speed (Performance) — 3/3 passed (insert <100ms, query <50ms)
- [x] TC-020 Discovery Usability (Manual) — 16/16 executable steps pass, 4 N/A, 4 screenshots

### Sprint 3 Task Board
- [x] TC-021 Play/Pause (Espresso) — 4/4 passed (MuMu, unblocked on API 31)
- [x] TC-022 Playback Speed (Espresso) — 4/4 passed (MuMu, unblocked on API 31)
- [x] TC-023 Download Episode (Espresso) — 4/4 passed (MuMu, unblocked on API 31)
- [x] TC-024 Audio Focus (UIAutomator) — 3/3 passed (Pixel_7 AVD, API 37)
- [x] TC-025 Background Playback (UIAutomator) — 3/3 passed (Pixel_7 AVD, API 37)
- [x] TC-026 Playback State Machine (Unit) — 11/11 passed
- [x] TC-027 Download Queue Priority (Unit) — 10/10 passed
- [x] TC-028 FeedMedia DAO (Integration) — 6/6 passed (Pixel_7 AVD, API 37)
- [x] TC-029 Download Status Tracking (Integration) — 7/7 passed (Pixel_7 AVD, API 37), fixed download_url+size column
- [x] TC-030 Long Playback Stability (Manual) — 20/20 N/A (MuMu emulator)

### Sprint 4 Task Board
- [x] TC-031 Theme & Display (Espresso) — 4/4 passed (MuMu)
- [x] TC-032 Storage & Network Prefs (Espresso) — 4/4 passed (MuMu)
- [x] TC-033 Permission Handling (UIAutomator) — 3/3 passed (MuMu)
- [x] TC-034 Notification Channels (UIAutomator) — 3/3 passed (MuMu)
- [x] TC-035 User Preferences Logic (Unit) — 8/8 passed
- [x] TC-036 Storage Path Validation (Unit) — 5/5 passed
- [x] TC-037 Data Export/Import (Integration) — 4/4 passed (MuMu)
- [x] TC-038 Episode Cache Cleanup (Integration) — 4/4 passed (MuMu)
- [x] TC-039 Startup Time & Memory (Performance) — 4/4 passed (MuMu)
- [x] TC-040 Accessibility (Manual) — 17/17 pass, 3 N/A (MuMu)
