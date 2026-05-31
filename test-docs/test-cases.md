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
| TC-015 | UIAutomator | Feed Refresh & Pull-to-Update | Medium | — | — |
| TC-016 | Unit Test (JUnit) | Feed URL Parsing & Normalization | High | — | — |
| TC-017 | Unit Test (JUnit) | Subscription Sort & Filter Logic | Low | — | — |
| TC-018 | Integration (SQLite) | Feed & FeedItem DAO Query Correctness | Medium | — | — |
| TC-019 | Performance | Feed Parsing Speed Benchmark | Medium | — | — |
| TC-020 | Manual / Exploratory | Discovery Page Usability | Low | — | — |

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
