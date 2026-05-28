# AI Progress Tracker — AntennaPod Mobile Testing

> **For AI assistants**: Read this file first. It tells you exactly where we are, what's done,
> what's pending, and what the next action should be. Update it after every completed task block.

## Current Sprint Status

| Sprint | Member | TC Range | Status |
|--------|--------|----------|--------|
| Sprint 1 | Tianyu Yao (Lead) | TC-001 ~ TC-010 | **Completed** |
| Sprint 2 | Member 2 | TC-011 ~ TC-020 | Not started |
| Sprint 3 | Member 3 | TC-021 ~ TC-030 | Not started |
| Sprint 4 | Member 4 | TC-031 ~ TC-040 | Not started |

---

## Sprint 1 — Tianyu Yao (Lead): Core Foundation — COMPLETED

### Test Execution Results (2026-05-28)

| TC-ID | Method | File | Tests | Status | Device/Notes |
|-------|--------|------|-------|--------|-------------|
| TC-001 | Espresso | `espresso/TC001_AppLaunchTest.kt` | 6/6 | Passed | MuMu ALN-AL00 |
| TC-002 | Espresso | `espresso/TC002_SubscribePodcastTest.kt` | 4/4 | Passed | MuMu ALN-AL00 |
| TC-003 | Espresso | `espresso/TC003_PlayEpisodeTest.kt` | 4/4 | Passed | MuMu ALN-AL00 |
| TC-004 | Espresso | `espresso/TC004_QueueManagementTest.kt` | 4/4 | Passed | MuMu ALN-AL00 |
| TC-005 | Espresso | `espresso/TC005_SearchDiscoveryTest.kt` | 4/4 | Passed | MuMu ALN-AL00 |
| TC-006 | UIAutomator | `uiautomator/TC006_OpmlImportTest.kt` | 3/3 | Passed | MuMu ALN-AL00 |
| TC-007 | Unit Test | `unit/TC007_FeedEntityTest.kt` | 17/17 | Passed | JVM |
| TC-008 | Unit Test | `unit/TC008_FeedItemFeedMediaTest.kt` | 34/34 | Passed | JVM |
| TC-009 | Integration | `integration/TC009_PodDBAdapterSchemaTest.kt` | 8/8 | Passed | MuMu ALN-AL00 |
| TC-010 | Manual | `manual/TC010_FirstLaunchUserFlow.kt` | 14/20 | Partial | 6 steps need network |

### Infrastructure Changes

| File | Change | Reason |
|------|--------|--------|
| `app/build.gradle` | Added `kotlin-android` plugin | Required for Kotlin test compilation |
| `app/build.gradle` | Added `uiautomator` androidTest dep | Required for TC-006 UIAutomator tests |
| `gradle/libs.versions.toml` | Added `uiautomator = "androidx.test.uiautomator:uiautomator:2.3.0"` | TC-006 dependency |

### Screenshots Captured (5 unique states)

| File | TC | What It Shows |
|------|-----|---------------|
| `tc001-launch-home.png` | TC-001 | Home screen: bottom nav, home content, toolbar |
| `tc003-subscriptions.png` | TC-003 | Subscriptions tab: empty state |
| `tc003-queue.png` | TC-003 | Queue tab: empty state |
| `tc010-step5-more-menu.png` | TC-010 | More menu popup: overflow items list |
| `tc010-step17-landscape.png` | TC-010 | Landscape orientation: layout adaptation |

### Documentation Updated

| File | Status | Content |
|------|--------|---------|
| `test-cases.md` | Updated | TC-001~010 specs with actual test lists + adaptation notes |
| `test-summary-report.md` | Updated | Final results: 84/84 automated passed, 14/20 manual |
| `manual-test-result.md` | Updated | TC-010 20-step execution results |
| `AI-GUIDE.md` | Updated | Fixed pitfalls #2, #3; added #4-#10; added "Lessons from Sprint 1" |
| `CONTRIBUTING.md` | Updated | Added screenshot review before commit |
| `screenshots/README.md` | Updated | Added 4-step review process |
| `PROGRESS.md` | Created | This file |

### Remaining Actions

- [ ] **Git commit & PR**: Create branch `tc/tianyu-yao/TC001-010`, commit all files, push, open PR
- [ ] TC-010 steps 6-8, 10-14: re-test on a device with network access

---

## Sprint 2 — Member 2: Subscription & Discovery — NOT STARTED

### Assigned TCs

| TC-ID | Method | Title | Priority |
|-------|--------|-------|----------|
| TC-011 | Espresso | Browse Discovery Page | High |
| TC-012 | Espresso | Subscribe to Feed from Discovery | High |
| TC-013 | Espresso | Unsubscribe & Feed Deletion | Medium |
| TC-014 | UIAutomator | Share Feed URL to External App | Medium |
| TC-015 | UIAutomator | Feed Refresh & Pull-to-Update | Medium |
| TC-016 | Unit Test (JUnit) | Feed URL Parsing & Normalization | High |
| TC-017 | Unit Test (JUnit) | Subscription Sort & Filter Logic | Low |
| TC-018 | Integration (SQLite) | Feed & FeedItem DAO Query Correctness | Medium |
| TC-019 | Performance | Feed Parsing Speed Benchmark | Medium |
| TC-020 | Manual / Exploratory | Discovery Page Usability | Low |

### Key Reminders for Member 2

- Read [Lessons from Sprint 1](#lessons-from-sprint-1) before writing any code.
- `TestHelper.kt` already exists in `utils/` — do not create a new one.
- Kotlin plugin and UIAutomator dependency already added — do not modify `build.gradle`.
- For screenshot rules, see `screenshots/README.md`.
- Use the Espresso test pattern from `AI-GUIDE.md` (ActivityTestRule, not ActivityScenarioRule on MuMu).

---

## Sprint 3 — Member 3: Playback & Downloads — NOT STARTED

### Assigned TCs

| TC-ID | Method | Title | Priority |
|-------|--------|-------|----------|
| TC-021 | Espresso | Play / Pause Episode Controls | High |
| TC-022 | Espresso | Playback Speed Adjustment | Medium |
| TC-023 | Espresso | Download Episode for Offline Playback | High |
| TC-024 | UIAutomator | Audio Focus & Playback Notification | Medium |
| TC-025 | UIAutomator | Background Playback Continuity | Medium |
| TC-026 | Unit Test (JUnit) | Playback State Machine Logic | High |
| TC-027 | Unit Test (JUnit) | Download Queue Priority Logic | Medium |
| TC-028 | Integration (SQLite) | FeedMedia DAO Read / Write Integrity | Medium |
| TC-029 | Integration (SQLite) | Episode Download Status Tracking | Medium |
| TC-030 | Manual / Exploratory | Long Playback Stability & Battery | Medium |

---

## Sprint 4 — Member 4: Settings & System — NOT STARTED

### Assigned TCs

| TC-ID | Method | Title | Priority |
|-------|--------|-------|----------|
| TC-031 | Espresso | Theme & Display Settings | Medium |
| TC-032 | Espresso | Storage & Network Preferences | Medium |
| TC-033 | UIAutomator | Runtime Permission Handling (Storage / Audio) | High |
| TC-034 | UIAutomator | Notification Channel Settings | Medium |
| TC-035 | Unit Test (JUnit) | User Preferences Read / Write Logic | Medium |
| TC-036 | Unit Test (JUnit) | Storage Path Validation & Sanitization | Medium |
| TC-037 | Integration (SQLite) | Data Export & Import Integrity | Medium |
| TC-038 | Integration (SQLite) | Episode Cache Table Cleanup | Medium |
| TC-039 | Performance | App Startup Time & Memory Footprint | High |
| TC-040 | Manual / Exploratory | Accessibility & Edge Cases | Medium |

---

## Lessons from Sprint 1

### Screenhots
- Before committing: visually diff every new screenshot against ALL existing ones.
- Delete any duplicate (same UI state = same screen). Different TC-ID prefix does not justify duplicates.
- Place `TestHelper.saveScreenshot()` AFTER navigation actions (e.g., `perform(click())`), not before.
- Pull path: `/storage/emulated/0/Download/screenshots/`. The old doc path (`/sdcard/Android/data/...`) gets deleted on test uninstall.

### Test Code
- Use `ActivityTestRule(false, false)` + `launchActivity(Intent(Intent.ACTION_MAIN))` — not `ActivityScenarioRule`. MuMu emulator (ALN-AL00) doesn't work with ActivityScenario.
- Bottom nav items are dynamic. Only the first 4 + "More" show by default. `addfeed`, `downloads`, `favorites` may be hidden.
- `SortOrder` is an enum, not a class. Use `SortOrder.DATE_NEW_OLD`, not constructor.
- PodDBAdapter cursors use `SELECT_KEY_FEED_ID` ("feed_id"), not `KEY_ID` ("id").

### Test Content
- Tests that need network (subscribe, play, search) can adapt: verify tab navigation / UI element presence instead.
- Document adaptations in `test-cases.md`.

### Documentation
- Update `PROGRESS.md` after every commit.
- Update `test-cases.md` with actual test lists after implementation (not planned).
- Update `test-summary-report.md` with final results after test execution.

### Commit Strategy
- Commit in batches of 2-3 TCs, not all 10 at once.
- Each batch: code → compile → run → docs → commit → update PROGRESS.md.
- Small commits = easy review, easy revert, clear git log for AI context recovery.

---

## Quick-Start for AI Assistants

1. Read this file — understand current progress.
2. Read `AI-GUIDE.md` — coding patterns, pitfalls, workflow.
3. Read `CONTRIBUTING.md` — commit rules, file organization.
4. Read `test-docs/test-case-plan.md` — find your member's TC range.
5. Start coding.

**Update this file after every completed task block.**
