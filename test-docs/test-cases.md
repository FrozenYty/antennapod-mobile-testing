# Test Cases — AntennaPod Mobile

> Append your TC specs below. Copy the table format from the first entry.
> Set Status after test execution: Passed / Partial / Failed.

---

## Tianyu Yao — Core Foundation (TC-001 ~ TC-010)

| TC-ID | Method | Title | Priority | Status | Notes |
|-------|--------|-------|----------|--------|-------|
| TC-001 | Espresso | App Launch & Main Screen | High | Passed | MuMu emulator, 6/6 passed |
| TC-002 | Espresso | Subscribe to Podcast | High | Passed | MuMu emulator, 4/4 passed |
| TC-003 | Espresso | Play Episode | High | Passed | MuMu emulator, 4/4 passed |
| TC-004 | Espresso | Queue Management (Add / Remove / Reorder) | Medium | Passed | MuMu emulator, 4/4 passed |
| TC-005 | Espresso | In-App Search & Discovery Browse | Medium | Passed | MuMu emulator, 4/4 passed |
| TC-006 | UIAutomator | OPML Import via System File Picker | High | Passed | MuMu emulator, 3/3 passed |
| TC-007 | Unit Test (JUnit) | Feed Entity Field Validation | High | Passed | 17/17 passed |
| TC-008 | Unit Test (JUnit) | FeedItem & FeedMedia Entity Validation | High | Passed | 34/34 passed |
| TC-009 | Integration (SQLite) | PodDBAdapter Schema & Table Creation | Medium | Passed | MuMu emulator, 8/8 passed |
| TC-010 | Manual / Exploratory | First-Launch User Flow | Medium | Manual | Checklist ready, awaiting execution |

### TC-001: App Launch & Main Screen

**File**: `espresso/TC001_AppLaunchTest.kt`
**Adaptation**: Used `ActivityTestRule` (MuMu emulator compatibility).

**Tests**:
- `launchApp_shouldDisplayBottomNavigation` — bottom nav visible (captures screenshot)
- `launchApp_shouldDisplayAppBar` — app bar visible (uses `R.id.appbar` to avoid toolbar ambiguity)
- `launchApp_shouldDisplayDrawerLayout` — drawer layout visible
- `bottomNav_shouldHaveHomeItem` — home nav item present
- `bottomNav_shouldHaveSubscriptionsItem` — subscriptions nav item present
- `bottomNav_shouldHaveMoreItem` — more overflow item present (replaced addfeed — not in default visible items)

### TC-002: Subscribe to Podcast

**File**: `espresso/TC002_SubscribePodcastTest.kt`
**Adaptation**: Subscription requires network content. Tests focus on bottom nav UX: More menu access and tab navigation. Add Feed is inside the More popup — not directly accessible via Espresso.

**Tests**:
- `tapMore_shouldShowMoreMenu` — More menu clickable
- `bottomNav_shouldShowHomeItem` — home item present
- `bottomNav_shouldShowSubscriptionsItem` — subscriptions item present
- `launchApp_shouldDisplayBottomNavigation` — bottom nav visible

### TC-003: Play Episode

**File**: `espresso/TC003_PlayEpisodeTest.kt`
**Adaptation**: Playback requires media content. Tests focus on tab navigation to key screens.

**Tests**:
- `navigateToSubscriptions_shouldDisplayContent` — subscriptions tab (captures screenshot)
- `bottomNav_shouldAllowNavigationBetweenTabs` — tab switching works
- `navigateToQueue_shouldDisplayContent` — queue tab (captures screenshot)
- `navigateToInbox_shouldDisplayContent` — inbox tab

### TC-004: Queue Management

**File**: `espresso/TC004_QueueManagementTest.kt`
**Adaptation**: Queue content requires subscribed feeds. Tests focus on navigation to queue-related tabs. `downloads` and `favorites` are not in the default visible bottom nav items.

**Tests**:
- `navigateToQueue_shouldShowQueueScreen` — queue tab accessible
- `navigateFromQueueToHome_shouldReturnToHome` — back navigation works
- `navigateToInbox_shouldShowInboxScreen` — inbox tab accessible
- `navigateToSubscriptions_shouldShowSubscriptionsScreen` — subscriptions tab accessible

### TC-005: In-App Search & Discovery Browse

**File**: `espresso/TC005_SearchDiscoveryTest.kt`
**Adaptation**: Search/discovery needs network. Tests verify bottom nav item presence including the More overflow trigger.

**Tests**:
- `bottomNav_shouldHaveMoreMenu` — More item visible
- `bottomNav_shouldHaveInbox` — inbox item visible
- `bottomNav_shouldHaveQueue` — queue item visible
- `tapMore_shouldOpenMoreMenu` — More menu clickable

### TC-006: OPML Import via System File Picker

**File**: `uiautomator/TC006_OpmlImportTest.kt`
**Adaptation**: OPML import requires a pre-loaded .opml file on device storage. Tests use UIAutomator for cross-app view detection on the main activity.

**Tests**:
- `mainActivity_shouldDisplayBottomNavigation` — UIAutomator finds bottomNav by resource ID
- `bottomNav_shouldContainHomeItem` — UIAutomator finds home item + verifies enabled state
- `bottomNav_shouldContainQueueItem` — UIAutomator finds queue item + verifies enabled state

### TC-007: Feed Entity Field Validation

**File**: `unit/TC007_FeedEntityTest.kt`

**Tests** (17):
- Constructor validation (full args, test-purpose)
- Title resolution (with/without custom title)
- Identifying value resolution (feed identifier, download URL)
- equals/hashCode (same id, different id)
- SortOrder validation (invalid scope, valid scope)
- Custom title (same as feed title → null, different → stored)
- setID → preferences feed ID sync
- Human readable identifier
- isLocalFeed (local prefix, HTTP URL)
- setItems

### TC-008: FeedItem & FeedMedia Entity Validation

**File**: `unit/TC008_FeedItemFeedMediaTest.kt`

**FeedItem Tests** (20):
- Constructors (default, full args)
- State transitions (setPlayed, setNew, setPlayed(false))
- Identifying value resolution (identifier, title, media URL)
- equals/hashCode (same/different id)
- Bidirectional reference with FeedMedia
- isDownloaded, isInProgress, hasMedia
- Tag management (add/remove)
- Auto download toggle
- Defensive copy of pubDate
- Image location fallback

**FeedMedia Tests** (14):
- Constructors (simple, full args)
- Human readable identifier
- Download state (isDownloaded, setDownloaded)
- Position management
- File existence check
- equals/hashCode (same/different id)
- Playback state (isInProgress)
- Local file availability

### TC-009: PodDBAdapter Schema & Table Creation

**File**: `integration/TC009_PodDBAdapterSchemaTest.kt`

**Tests** (8):
- feedsTable_insertAndRetrieve_shouldWork
- feedItemsTable_insertAndRetrieve_shouldWork
- feedMediaTable_insertAndRetrieve_shouldWork
- queueTable_insert_shouldWork
- favoritesTable_insert_shouldWork
- downloadLogTable_insert_shouldWork
- simpleChaptersTable_insert_shouldWork
- feedsAutoIncrement_shouldGenerateSequentialIds

### TC-010: First-Launch User Flow

**File**: `manual/TC010_FirstLaunchUserFlowTest.kt`

20-step manual checklist covering:
- App launch and initial UI
- Bottom navigation exploration
- Adding a podcast feed
- Playback controls
- Device rotation
- State preservation on relaunch

---
## Jianheng Sun — Subscription & Discovery (TC-011 ~ TC-020) In Progress

| TC-ID | Method | Title | Priority | Status | Notes |
|-------|--------|-------|----------|--------|-------|
| TC-011 | Espresso | Browse Discovery Page | High | Compiled | Pending device run |
| TC-012 | Espresso | Subscribe to Feed from Discovery | High | Compiled | Pending device run |
| TC-013 | Espresso | Unsubscribe & Feed Deletion | Medium | Compiled | Pending device run |
| TC-014 | UIAutomator | Share Feed URL to External App | Medium | Compiled | Pending device run |
| TC-015 | UIAutomator | Feed Refresh & Pull-to-Update | Medium | Compiled | Pending device run |
| TC-016 | Unit Test (JUnit) | Feed URL Parsing & Normalization | High | Passed | 24/24 passed |
| TC-017 | Unit Test (JUnit) | Subscription Sort & Filter Logic | Low | Passed | 23/23 passed |
| TC-018 | Integration (SQLite) | Feed & FeedItem DAO Query Correctness | Medium | Compiled | Pending device run |
| TC-019 | Performance | Feed Parsing Speed Benchmark | Medium | Compiled | Pending device run |
| TC-020 | Manual / Exploratory | Discovery Page Usability | Low | Ready | Checklist ready for execution |

### TC-011: Browse Discovery Page

**File**: `espresso/TC011_BrowseDiscoveryTest.kt`
**Adaptation**: Discovery content requires network access. Tests focus on UI element verification and tab navigation to the Subscriptions screen.

**Tests** (4):
- `launchApp_shouldDisplayBottomNavigation` — bottom nav visible
- `navigateToSubscriptions_shouldDisplaySubscriptionsGrid` — subscriptions grid displayed after clicking nav item (captures screenshot)
- `subscriptionsScreen_shouldDisplayToolbar` — toolbar visible on subscriptions screen
- `subscriptionsScreen_shouldDisplayAddFeedButton` — FAB add-feed button visible

### TC-012: Subscribe to Feed from Discovery

**File**: `espresso/TC012_SubscribeDiscoveryTest.kt`
**Adaptation**: More popup menu content is not reliably testable with Espresso (dynamic layout). Tests verify bottom nav structure and clickability.

**Tests** (4):
- `bottomNav_shouldHaveMoreItem` — More item visible in bottom nav
- `tapMore_shouldBeClickable` — More item is clickable
- `bottomNav_shouldHaveSubscriptionsItem` — subscriptions nav item present
- `bottomNav_shouldHaveHomeItem` — home nav item present

### TC-013: Unsubscribe & Feed Deletion

**File**: `espresso/TC013_UnsubscribeDeleteTest.kt`
**Adaptation**: No pre-existing subscriptions on the test device. Tests verify UI structure and navigation rather than actual deletion actions.

**Tests** (4):
- `navigateToSubscriptions_shouldDisplayContentArea` — subscriptions grid displayed after tab click
- `subscriptionsScreen_shouldDisplayToolbar` — toolbar visible on subscriptions screen
- `navigateToHome_shouldReturnToHomeScreen` — back-navigation from subscriptions to home works
- `bottomNav_shouldAllowTabSwitch` — tab switching between inbox and subscriptions works

### TC-014: Share Feed URL to External App

**File**: `uiautomator/TC014_ShareFeedUrlTest.kt`
**Adaptation**: Sharing a feed URL requires a subscribed feed. Tests verify the UI infrastructure that supports sharing (bottom nav, drawer, subscriptions tab) via UIAutomator.

**Tests** (3):
- `mainActivity_shouldDisplayBottomNavigation` — UIAutomator finds bottomNavigationView by resource ID
- `bottomNav_shouldContainSubscriptionsItem` — UIAutomator finds subscriptions item + verifies enabled state
- `mainActivity_shouldDisplayDrawerLayout` — UIAutomator finds drawer_layout

### TC-015: Feed Refresh & Pull-to-Update

**File**: `uiautomator/TC015_FeedRefreshTest.kt`
**Adaptation**: Feed refresh requires network and subscribed feeds. Tests verify the UI elements that support refresh functionality.

**Tests** (3):
- `mainActivity_shouldDisplayDrawerLayout` — UIAutomator finds drawer_layout
- `bottomNav_shouldContainSubscriptionsItem` — UIAutomator finds subscriptions item + verifies enabled state
- `bottomNav_shouldContainHomeItem` — UIAutomator finds home item + verifies enabled state

### TC-016: Feed URL Parsing & Normalization

**File**: `unit/TC016_FeedUrlParsingTest.kt`
**Runner**: `@RunWith(RobolectricTestRunner::class)` — required because `UrlChecker.prepareUrl()` uses `android.util.Log.d()`.

**Tests** (24):
- Valid URL preservation (http, https)
- Missing protocol → http:// prepended
- Protocol conversion (feed://, itpc://, pcast://, pcast:)
- AntennaPod subscribe protocol removal
- AntennaPod deeplink URL extraction
- Whitespace trimming (leading, trailing, newline+tab)
- SubscribeOnAndroid prefix removal
- Case-insensitive protocol matching
- urlEquals: same URLs, different hosts, trailing slash, case-insensitive host

### TC-017: Subscription Sort & Filter Logic

**File**: `unit/TC017_SortFilterLogicTest.kt`
**Runner**: `@RunWith(RobolectricTestRunner::class)` — required because `SortOrder.fromCodeString()` and `SubscriptionsFilter` use `android.text.TextUtils`.

**Tests** (23):
- FeedOrder: fromOrdinal(id) round-trip, Java ordinal positions
- SortOrder: scope validation (INTRA_FEED vs INTER_FEED), fromCodeString/toCodeString
- FeedItemFilter: PLAYED matches played item, UNPLAYED matches unplayed, NEW matches new items, QUEUED matches tagged items, IS_FAVORITE matches favorited items, HAS_MEDIA matches items with media
- SubscriptionsFilter: string constructor, isEnabled, getValues serialization

### TC-018: Feed & FeedItem DAO Query Correctness

**File**: `integration/TC018_FeedItemDaoTest.kt`
**Adaptation**: Requires device/emulator for instrumented test run. Follows TC-009 pattern with PodDBAdapter.

**Tests** (8):
- `feedsTable_insertWithAllFields_shouldRetrieveCorrectly`
- `feedsTable_updateState_shouldPersist`
- `feedsTable_setCustomTitle_shouldPersist`
- `feedsTable_insertMultiple_shouldHaveUniqueIds`
- `feedItemsTable_insertMultiple_shouldBeRetrievable`
- `feedItemsTable_queryWithSortOrder_shouldReturnOrderedResults`
- `feedItemsTable_itemsByFeedId_shouldOnlyReturnThatFeedsItems`
- `feedItemsTable_queueInsert_shouldCreateQueueEntry`

### TC-019: Feed Parsing Speed Benchmark

**File**: `performance/TC019_FeedParsingBenchmarkTest.kt`
**Adaptation**: No benchmark library (Macrobenchmark/Microbenchmark) configured. Uses manual timing with `System.nanoTime()` and 20 iterations per test.

**Tests** (3):
- `benchmark_feedInsert_shouldBeUnder100ms` — 20 feed inserts, average time check
- `benchmark_feedQuery_shouldBeUnder50ms` — 20 feed queries, average time check
- `benchmark_itemInsertWithFeed_shouldBeUnder200ms` — 20 item inserts with FK, average time check

### TC-020: Discovery Page Usability

**File**: `manual/TC020_DiscoveryUsabilityTest.kt`

20-step manual checklist covering:
- App launch and bottom navigation
- Subscriptions tab navigation
- More menu → Add Feed flow
- Podcast URL entry and subscription
- Feed detail and episode list exploration
- Multi-select mode activation
- Landscape/portrait rotation
- State preservation on navigation

---
## Member Four — Settings & System (TC-031 ~ TC-040) In Progress

| TC-ID | Method | Title | Priority | Status | Notes |
|-------|--------|-------|----------|--------|-------|
| TC-031 | Espresso | Theme & Display Settings | Medium | Compiled | Pending device run |
| TC-032 | Espresso | Storage & Network Preferences | Medium | Compiled | Pending device run |
| TC-033 | UIAutomator | Runtime Permission Handling | High | Compiled | Pending device run |
| TC-034 | UIAutomator | Notification Channel Settings | Medium | Compiled | Pending device run |
| TC-035 | Unit Test (JUnit) | User Preferences Read / Write Logic | Medium | Passed | 8/8 passed |

### TC-031: Theme & Display Settings

**File**: `espresso/TC031_ThemeDisplaySettingsTest.kt`

**Tests** (4):
- `settingsMain_shouldDisplayUserInterfaceEntry` — settings main screen shows User interface entry
- `userInterfaceSettings_shouldDisplayThemeControls` — User interface screen shows Full black theme control
- `userInterfaceSettings_shouldDisplayEpisodeCoverControl` — display preferences include episode cover setting
- `userInterfaceSettings_shouldDisplayBottomNavigationControl` — behavior preferences include bottom navigation setting

### TC-032: Storage & Network Preferences

**File**: `espresso/TC032_StorageNetworkPreferencesTest.kt`

**Tests** (4):
- `settingsMain_shouldDisplayDownloadsEntry` — settings main screen shows Downloads entry
- `downloadsSettings_shouldDisplayDataFolderPreference` — Downloads screen shows data folder preference
- `downloadsSettings_shouldDisplayFeedRefreshPreference` — Downloads screen shows feed refresh interval preference
- `downloadsSettings_shouldOpenProxyDialog` — Proxy preference opens its configuration dialog

### TC-033: Runtime Permission Handling

**File**: `uiautomator/TC033_RuntimePermissionHandlingTest.kt`
**Adaptation**: Android 12 test devices do not show the Android 13 notification runtime dialog. Tests validate the manifest declaration and the system Settings entry point used to manage runtime permissions.

**Tests** (3):
- `manifest_shouldDeclareNotificationRuntimePermission` — app manifest declares `POST_NOTIFICATIONS`
- `systemAppInfo_shouldOpenForAntennaPodPackage` — package-specific Android Settings page opens through UIAutomator
- `systemPermissionManagement_shouldRemainInSettingsApp` — permission management flow stays in the system Settings app

### TC-034: Notification Channel Settings

**File**: `uiautomator/TC034_NotificationChannelSettingsTest.kt`

**Tests** (3):
- `createChannels_shouldRegisterPlaybackAndDownloadChannels` — playback, downloading, and refresh channels are registered
- `createChannels_shouldRegisterErrorAndNewsGroups` — error and news notification channel groups are registered
- `appNotificationSettings_shouldOpenSystemNotificationScreen` — Android app notification settings opens through UIAutomator

### TC-035: User Preferences Read / Write Logic

**File**: `unit/TC035_UserPreferencesTest.kt`
**Runner**: `@RunWith(RobolectricTestRunner::class)` — required because UserPreferences depends on Android Context, resources, and SharedPreferences.

**Tests** (8):
- `themePreference_setLightDarkSystem_shouldRoundTrip` — theme values persist and read back correctly
- `playbackPreferences_setSpeedAndSkipSilence_shouldPersist` — playback speed and skip silence values persist
- `feedRefreshInterval_setZero_shouldDisableAutoUpdate` — update interval zero disables auto update
- `mobileDataPreferences_toggleFeedRefreshAndImages_shouldPersist` — mobile network toggles persist independently
- `notificationButtons_setCustomButtons_shouldPersist` — notification button selection persists
- `defaultPage_setQueueFragment_shouldPersist` — default page setting persists
- `bottomNavigation_setDisabledAndEnabled_shouldPersist` — bottom navigation toggle persists
- `proxyConfig_setHttpProxy_shouldRoundTrip` — proxy configuration persists and reads back correctly
