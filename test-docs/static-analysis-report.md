# AntennaPod Mobile Testing — Static Analysis Report

**Generated**: 2026-06-03 21:43  
**Tool**: Androguard 4.1.4 + custom analysis  
**APK**: `D:\MYPROJS\Android_Studio_Files\antennapod-mobile-testing\app-under-test\antennapod\app\build\outputs\apk\play\debug\app-play-debug.apk`

---
## 1. Project Overview

| Field | Value |
|-------|-------|
| App Package | `de.danoeh.antennapod.debug` |
| App Version | 3.12.0-beta1 (3120001) |
| Min SDK | 23 |
| Target SDK | 36 |
| Total Test Cases | 40 |
| Test Source Files | 41 |
| Screenshots | 24 |
| Team Members | 4 |

## 2. Test Method Distribution

### 2.1 Overall Distribution

| Method | Count | Percentage | Bar |
|--------|-------|-----------|-----|
| Espresso | 13 | 32.5% | ██████████████████████████████ |
| UIAutomator | 7 | 17.5% | ████████████████ |
| Unit Test | 8 | 20.0% | ██████████████████ |
| Integration | 6 | 15.0% | █████████████ |
| Manual / Exploratory | 4 | 10.0% | █████████ |
| Performance | 2 | 5.0% | ████ |
| Static Analysis | 0 | 0.0% |  |
| Static Analysis (Androguard) | 1 | — | Automated APK audit |

### 2.2 Distribution by Sprint

| Sprint | Member | Espresso | UIAutomator | Unit | Integration | Manual | Performance | Total |
|--------|--------|----------|-------------|------|-------------|--------|-------------|-------|
| Sprint 1 | Tianyu Yao | 5 | 1 | 2 | 1 | 1 | 0 | 10 |
| Sprint 2 | Jianheng Sun | 3 | 2 | 2 | 1 | 1 | 1 | 10 |
| Sprint 3 | Yuanbing Wang | 3 | 2 | 2 | 2 | 1 | 0 | 10 |
| Sprint 4 | Xintao Wang | 2 | 2 | 2 | 2 | 1 | 1 | 10 |

## 3. Test Results Summary

| Sprint | Member | Total TCs | Passed | Partial | N/A | Status |
|--------|--------|-----------|--------|---------|-----|--------|
| Sprint 1 | Tianyu Yao | 10 | 9 | 1 | 0 | ✅ Done |
| Sprint 2 | Jianheng Sun | 10 | 9 | 0 | 1 | ✅ Done |
| Sprint 3 | Yuanbing Wang | 10 | 9 | 0 | 1 | ✅ Done |
| Sprint 4 | Xintao Wang | 10 | 9 | 0 | 1 | ✅ Done |
| **Total** | **4 members** | **40** | **36** | — | — | **100% Done** |

## 4. APK Manifest Analysis (Androguard)

### 4.1 Permissions

**11 permissions declared**:

- 🟢 `android.permission.ACCESS_NETWORK_STATE`
- 🟢 `android.permission.ACCESS_WIFI_STATE`
- 🟢 `android.permission.BLUETOOTH`
- 🟢 `android.permission.FOREGROUND_SERVICE`
- 🟢 `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- 🟢 `android.permission.INTERNET`
- 🟢 `android.permission.POST_NOTIFICATIONS`
- 🟢 `android.permission.RECEIVE_BOOT_COMPLETED`
- 🟢 `android.permission.VIBRATE`
- 🟢 `android.permission.WAKE_LOCK`
- 🟡 `de.danoeh.antennapod.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`

### 4.2 Components

**46 total components**:

| Type | Count | With Intent-Filter |
|------|-------|-------------------|
| Activity | 13 | 10 |
| Service | 11 | 4 |
| Receiver | 19 | 13 |
| Provider | 3 | 0 |

### 4.3 Security Flags

| Flag | Value | Assessment |
|------|-------|------------|
| debuggable | `None` | ⚠ Not declared in manifest (debug build, attribute may be implicit) |
| allowBackup | `None` | Standard Android setting |
| Signed V1 | Yes | |
| Signed V2 | Yes | |
| Signed V3 | No | |

## 5. Test Code Structure

### 5.1 Instrumented Tests (androidTest)

| Directory | Files | Test Classes |
|-----------|-------|-------------|
| `espresso/` | 13 | `TC001_AppLaunchTest.kt`, `TC002_SubscribePodcastTest.kt`, `TC003_PlayEpisodeTest.kt`, `TC004_QueueManagementTest.kt`, `TC005_SearchDiscoveryTest.kt`, `TC011_BrowseDiscoveryTest.kt`, `TC012_SubscribeDiscoveryTest.kt`, `TC013_UnsubscribeDeleteTest.kt`, `TC021_PlayPauseControlsTest.kt`, `TC022_PlaybackSpeedAdjustmentTest.kt`, `TC023_DownloadEpisodeForOfflinePlaybackTest.kt`, `TC031_ThemeDisplaySettingsTest.kt`, `TC032_StorageNetworkPreferencesTest.kt` |
| `integration/` | 6 | `TC009_PodDBAdapterSchemaTest.kt`, `TC018_FeedItemDaoTest.kt`, `TC028_FeedMediaDaoReadWriteIntegrityTest.kt`, `TC029_EpisodeDownloadStatusTrackingTest.kt`, `TC037_DataExportImportIntegrityTest.kt`, `TC038_EpisodeCacheCleanupTest.kt` |
| `performance/` | 2 | `TC019_FeedParsingBenchmarkTest.kt`, `TC039_StartupMemoryBenchmarkTest.kt` |
| `uiautomator/` | 7 | `TC006_OpmlImportTest.kt`, `TC014_ShareFeedUrlTest.kt`, `TC015_FeedRefreshTest.kt`, `TC024_AudioFocusPlaybackNotificationTest.kt`, `TC025_BackgroundPlaybackContinuityTest.kt`, `TC033_RuntimePermissionHandlingTest.kt`, `TC034_NotificationChannelSettingsTest.kt` |
| `utils/` | 1 | `TestHelper.kt` |

### 5.2 JVM Tests (test)

| Directory | Files | Test Classes |
|-----------|-------|-------------|
| `manual/` | 4 | `TC010_FirstLaunchUserFlowTest.kt`, `TC020_DiscoveryUsabilityTest.kt`, `TC030_LongPlaybackStabilityTest.kt`, `TC040_AccessibilityEdgeCasesTest.kt` |
| `unit/` | 8 | `TC007_FeedEntityTest.kt`, `TC008_FeedItemFeedMediaTest.kt`, `TC016_FeedUrlParsingTest.kt`, `TC017_SortFilterLogicTest.kt`, `TC026_PlaybackStateMachineLogicTest.kt`, `TC027_DownloadQueuePriorityLogicTest.kt`, `TC035_UserPreferencesTest.kt`, `TC036_StoragePathValidationTest.kt` |

## 6. Screenshot Evidence Inventory

**Total**: 24 screenshots  

| # | File | Size (KB) | Sprint / Member |
|---|------|-----------|-----------------|
| 1 | `tc001-launch-home.png` | 101.4 | Tianyu Yao (Sprint 1) |
| 2 | `tc003-queue.png` | 68.8 | Tianyu Yao (Sprint 1) |
| 3 | `tc003-subscriptions.png` | 55.5 | Tianyu Yao (Sprint 1) |
| 4 | `tc006-main-screen.png` | 101.1 | Tianyu Yao (Sprint 1) |
| 5 | `tc010-step11-episode.png` | 172.5 | Tianyu Yao (Sprint 1) |
| 6 | `tc010-step13-miniplayer.png` | 121.6 | Tianyu Yao (Sprint 1) |
| 7 | `tc010-step14-error.png` | 200.2 | Tianyu Yao (Sprint 1) |
| 8 | `tc010-step17-landscape.png` | 77.7 | Tianyu Yao (Sprint 1) |
| 9 | `tc010-step5-more-menu.png` | 99.0 | Tianyu Yao (Sprint 1) |
| 10 | `tc010-step8-subscribed.png` | 464.7 | Tianyu Yao (Sprint 1) |
| 11 | `tc020-step10-feed-preview.png` | 387.6 | Jianheng Sun (Sprint 2) |
| 12 | `tc020-step12-subscribed-list.png` | 804.0 | Jianheng Sun (Sprint 2) |
| 13 | `tc020-step13-episode-list.png` | 372.9 | Jianheng Sun (Sprint 2) |
| 14 | `tc020-step7-add-podcast.png` | 787.4 | Jianheng Sun (Sprint 2) |
| 15 | `tc021-queue-screen.png` | 68.2 | Yuanbing Wang (Sprint 3) |
| 16 | `tc023-more-menu.png` | 105.9 | Yuanbing Wang (Sprint 3) |
| 17 | `tc024-main-activity.png` | 149.6 | Yuanbing Wang (Sprint 3) |
| 18 | `tc025-launcher-after-home.png` | 1234.2 | Yuanbing Wang (Sprint 3) |
| 19 | `tc031-settings-main.png` | 102.2 | Xintao Wang (Sprint 4) |
| 20 | `tc031-user-interface-settings.png` | 89.7 | Xintao Wang (Sprint 4) |
| 21 | `tc032-downloads-settings.png` | 112.5 | Xintao Wang (Sprint 4) |
| 22 | `tc032-proxy-dialog.png` | 76.0 | Xintao Wang (Sprint 4) |
| 23 | `tc033-system-app-info-permissions.png` | 96.0 | Xintao Wang (Sprint 4) |
| 24 | `tc034-system-notification-settings.png` | 76.0 | Xintao Wang (Sprint 4) |

### 6.1 Screenshots per Member

| Member | Screenshots |
|--------|-------------|
| Tianyu Yao | 10 |
| Jianheng Sun | 4 |
| Yuanbing Wang | 4 |
| Xintao Wang | 6 |

## 7. Findings & Recommendations

| # | Severity | Category | Finding |
|---|----------|----------|---------|
| 1 | ℹ️ INFO | Coverage | 1 test steps are marked as partial/N/A — review for completeness |

---

*Report generated by `automation/generate_report.py`*
