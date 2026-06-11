# AntennaPod Mobile Testing — System Test Progress Summary

**Project**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**App Under Test**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**Date**: 2026-06-11

---

## 1. Overall Progress

| Metric | Value |
|--------|-------|
| Total System Test TCs | **20** |
| Total System Test Methods | **75** |
| Passed | **75** |
| Failed | **0** |
| Pass Rate | **100%** |
| Contributors | 4 members |
| Sprint Span | Sprint 1 – Sprint 4 |

System tests are divided into two frameworks:

| Framework | Purpose | TCs | Test Methods |
|-----------|---------|-----|-------------|
| Espresso | In-app UI automation (single process) | 13 | 54 |
| UIAutomator | Cross-app / system UI automation (multi-process) | 7 | 21 |

---

## 2. Per-Member Contribution Breakdown

### 2.1 Tianyu Yao (Team Lead) — Sprint 1: Core Foundation

| TC-ID | Title | Tests | Framework | Status |
|-------|-------|-------|-----------|--------|
| TC-001 | App Launch & Main Screen | 6 | Espresso | ✅ Passed |
| TC-002 | Subscribe to Podcast | 4 | Espresso | ✅ Passed |
| TC-003 | Play Episode | 4 | Espresso | ✅ Passed |
| TC-004 | Queue Management | 4 | Espresso | ✅ Passed |
| TC-005 | Search & Discovery | 4 | Espresso | ✅ Passed |
| TC-006 | OPML Import via System File Picker | 3 | UIAutomator | ✅ Passed |
| **Subtotal** | | **25** | | **100%** |

**Coverage**:
- App launch: bottom navigation, app bar, content area, home screen default tab, multiple tabs, screenshot capture
- Subscribe: podcast subscription flow, feed list verification, subscription confirmation
- Playback: episode play initiation, player controls structure, media UI elements
- Queue: queue access, episode list, queue management controls
- Search: search entry point, discovery page elements, search field interaction
- OPML import: cross-app system file picker, bottom navigation via UIAutomator, package detection

---

### 2.2 Jianheng Sun — Sprint 2: Subscription & Discovery

| TC-ID | Title | Tests | Framework | Status |
|-------|-------|-------|-----------|--------|
| TC-011 | Browse Discovery Page | 4 | Espresso | ✅ Passed |
| TC-012 | Subscribe from Discovery | 4 | Espresso | ✅ Passed |
| TC-013 | Unsubscribe & Delete | 4 | Espresso | ✅ Passed |
| TC-014 | Share Feed URL | 3 | UIAutomator | ✅ Passed |
| TC-015 | Feed Refresh | 3 | UIAutomator | ✅ Passed |
| **Subtotal** | | **18** | | **100%** |

**Coverage**:
- Browse discovery: subscriptions tab navigation, bottom nav verification, discovery UI structure, swipe refresh
- Subscribe from discovery: discover-to-subscribe flow, feed addition confirmation, subscription list update
- Unsubscribe: feed removal, deletion confirmation, list state after removal
- Share URL: cross-app share intent, system share sheet, feed URL extraction
- Feed refresh: background refresh trigger, swipe-to-refresh via system UI, refresh state detection

---

### 2.3 Yuanbing Wang — Sprint 3: Playback & Downloads

| TC-ID | Title | Tests | Framework | Status |
|-------|-------|-------|-----------|--------|
| TC-021 | Play / Pause Episode Controls | 4 | Espresso | ✅ Passed |
| TC-022 | Playback Speed Adjustment | 4 | Espresso | ✅ Passed |
| TC-023 | Download Episode for Offline Playback | 4 | Espresso | ✅ Passed |
| TC-024 | Audio Focus & Playback Notification | 3 | UIAutomator | ✅ Passed |
| TC-025 | Background Playback Continuity | 3 | UIAutomator | ✅ Passed |
| **Subtotal** | | **18** | | **100%** |

**Coverage**:
- Play/Pause: bottom navigation, queue item, playback UI infrastructure, more menu access
- Playback speed: speed control entry, speed dialog, slider/preset verification
- Download: episode browsing, download action, offline playback readiness, more menu navigation
- Audio focus: notification channel registration, system notification access, app-level notification infrastructure
- Background playback: home button press continuity, recent apps navigation, process survival, system-level app detection

---

### 2.4 Xintao Wang — Sprint 4: Settings & System

| TC-ID | Title | Tests | Framework | Status |
|-------|-------|-------|-----------|--------|
| TC-031 | Theme & Display Settings | 4 | Espresso | ✅ Passed |
| TC-032 | Storage & Network Preferences | 4 | Espresso | ✅ Passed |
| TC-033 | Runtime Permission Handling | 3 | UIAutomator | ✅ Passed |
| TC-034 | Notification Channel Settings | 3 | UIAutomator | ✅ Passed |
| **Subtotal** | | **14** | | **100%** |

**Coverage**:
- Theme & display: settings entry point, User Interface section, theme control, tinted colors preference
- Storage & network: downloads section, storage options, mobile data toggle, auto-download preference
- Runtime permissions: manifest declaration verification, system settings app page, permission surface detection
- Notification channels: channel registration in system settings, channel list accessibility, notification management

---

## 3. Contribution Comparison

| Member | Sprint | Module | TCs | Test Methods | Share |
|--------|--------|--------|-----|-------------|-------|
| **Tianyu Yao** | 1 | Core Foundation | 6 | **25** | 33.3% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 5 | **18** | 24.0% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 5 | **18** | 24.0% |
| **Xintao Wang** | 4 | Settings & System | 4 | **14** | 18.7% |
| **Total** | | | **20** | **75** | **100%** |

```
Tianyu Yao      █████████████████████████████████████████████  25 (33.3%)
Jianheng Sun    ████████████████████████████████████           18 (24.0%)
Yuanbing Wang   ████████████████████████████████████           18 (24.0%)
Xintao Wang     ██████████████████████████████                 14 (18.7%)
```

---

## 4. Framework Distribution

| Framework | Tests | Share | TC List |
|-----------|-------|-------|---------|
| Espresso (in-app UI, single-process) | 54 | 72.0% | TC-001–005, TC-011–013, TC-021–023, TC-031–032 |
| UIAutomator (cross-app, multi-process) | 21 | 28.0% | TC-006, TC-014–015, TC-024–025, TC-033–034 |

---

## 5. Test Environment

| Component | Value |
|-----------|-------|
| Primary Device | MuMu emulator (ALN-AL00, Android 12, x86_64, 127.0.0.1:7555) |
| Secondary Device | Pixel_7 AVD (API 37) — UIAutomator TC-024, TC-025, TC-028, TC-029 |
| Espresso API Limit | API ≤ 34 (InputManager.getInstance() removed in API 37) |
| Activity Launch | `ActivityTestRule(Activity::class.java, false, false)` + manual `launchActivity()` |
| Screenshot Capture | `TestHelper.saveScreenshot()` → `/storage/emulated/0/Download/screenshots/` |

---

## 6. File Inventory

```
app-under-test/antennapod/app/src/androidTest/java/de/danoeh/antennapod/
├── espresso/
│   ├── TC001_AppLaunchTest.kt                              ← Tianyu Yao (6 tests)
│   ├── TC002_SubscribePodcastTest.kt                       ← Tianyu Yao (4 tests)
│   ├── TC003_PlayEpisodeTest.kt                            ← Tianyu Yao (4 tests)
│   ├── TC004_QueueManagementTest.kt                        ← Tianyu Yao (4 tests)
│   ├── TC005_SearchDiscoveryTest.kt                        ← Tianyu Yao (4 tests)
│   ├── TC011_BrowseDiscoveryTest.kt                        ← Jianheng Sun (4 tests)
│   ├── TC012_SubscribeDiscoveryTest.kt                     ← Jianheng Sun (4 tests)
│   ├── TC013_UnsubscribeDeleteTest.kt                      ← Jianheng Sun (4 tests)
│   ├── TC021_PlayPauseControlsTest.kt                      ← Yuanbing Wang (4 tests)
│   ├── TC022_PlaybackSpeedAdjustmentTest.kt                ← Yuanbing Wang (4 tests)
│   ├── TC023_DownloadEpisodeForOfflinePlaybackTest.kt      ← Yuanbing Wang (4 tests)
│   ├── TC031_ThemeDisplaySettingsTest.kt                   ← Xintao Wang (4 tests)
│   └── TC032_StorageNetworkPreferencesTest.kt              ← Xintao Wang (4 tests)
├── uiautomator/
│   ├── TC006_OpmlImportTest.kt                             ← Tianyu Yao (3 tests)
│   ├── TC014_ShareFeedUrlTest.kt                           ← Jianheng Sun (3 tests)
│   ├── TC015_FeedRefreshTest.kt                            ← Jianheng Sun (3 tests)
│   ├── TC024_AudioFocusPlaybackNotificationTest.kt         ← Yuanbing Wang (3 tests)
│   ├── TC025_BackgroundPlaybackContinuityTest.kt           ← Yuanbing Wang (3 tests)
│   ├── TC033_RuntimePermissionHandlingTest.kt              ← Xintao Wang (3 tests)
│   └── TC034_NotificationChannelSettingsTest.kt            ← Xintao Wang (3 tests)
└── utils/
    └── TestHelper.kt                                       ← Shared utilities
```

---

## 7. Full Project Test Distribution (for context)

| Method | TCs | Test Methods | Share |
|--------|-----|-------------|-------|
| **Espresso (in-app UI)** | **13** | **54** | **32.5%** |
| Unit Tests (JUnit) | 8 | 132 | 20.0% |
| **UIAutomator (cross-app / system UI)** | **7** | **21** | **17.5%** |
| Integration (SQLite / DB) | 6 | 37 | 15.0% |
| Manual / Exploratory | 4 | 80 steps | 10.0% |
| Performance (benchmarks) | 2 | 7 | 5.0% |
| **Total** | **40** | **251 automated + 80 manual** | **100%** |

---

## 8. Key Lessons

| Category | Takeaway |
|----------|----------|
| API 37 incompatibility | `InputManager.getInstance()` removed in API 37 — Espresso tests must target API ≤ 34 emulators |
| `ActivityScenarioRule` vs `ActivityTestRule` | `ActivityScenarioRule` broken on MuMu — use `ActivityTestRule(false, false)` with manual launch |
| Resource ID ambiguity | `R.id.toolbar` has 2 matches in hierarchy — use unique IDs like `R.id.appbar` |
| Empty container views | `subscriptions_grid` has empty `globalVisibleRect` when no feeds — use `R.id.swipeRefresh` instead |
| Bottom nav hidden items | `bottom_navigation_addfeed` / `bottom_navigation_episodes` not always visible — use `bottom_navigation_more` |
| Screenshot persistence | Screenshots deleted on test APK uninstall — save to `/storage/emulated/0/Download/screenshots/` |
| UIAutomator timeout | `Until.hasObject()` / `Until.findObject()` require explicit timeout (5000ms+) for system UI transitions |

---

## 9. Conclusion

**System testing is 100% complete.** Four members delivered 20 system test TCs (13 Espresso + 7 UIAutomator) with 75 test methods across 4 sprints — all passing. Espresso tests cover in-app UI flows including launch (6), subscription management (12), playback controls (12), and settings navigation (8). UIAutomator tests validate cross-app interactions including file picker (3), share/refresh (6), notifications/audio focus (6), and permission/channel management (6). Tests run on both MuMu emulator (Android 12) and Pixel_7 AVD (API 37) depending on framework compatibility requirements.
