# PROGRESS.md — AI Work Log

> AI assistants: read this first. Update it as you work. Keep it real-time, not polished.

## Now

**Tianyu Yao** | Branch: `tianyu-yao/core-foundation` | Sprint 1 complete

## Task Board

### Sprint 1 — Tianyu Yao: Core Foundation
- [x] TC-001 App Launch (Espresso) — 6/6 passed
- [x] TC-002 Subscribe Podcast (Espresso) — 4/4 passed
- [x] TC-003 Play Episode (Espresso) — 4/4 passed
- [x] TC-004 Queue Management (Espresso) — 4/4 passed
- [x] TC-005 Search & Discovery (Espresso) — 4/4 passed
- [x] TC-006 OPML Import (UIAutomator) — 3/3 passed
- [x] TC-007 Feed Entity (Unit) — 17/17 passed
- [x] TC-008 FeedItem & FeedMedia (Unit) — 34/34 passed
- [x] TC-009 PodDBAdapter Schema (Integration) — 8/8 passed
- [x] TC-010 First-Launch (Manual) — 19/20 passed
- [x] Screenshots: 9 unique, no duplicates
- [x] CI: direct squash-merge to main on push
- [x] CI: doc checks (@author, naming)
- [x] Docs: all 10 files synced, no stale refs

### Sprint 2 — Member 2: Subscription & Discovery
- [ ] TC-011 Browse Discovery Page (Espresso)
- [ ] TC-012 Subscribe from Discovery (Espresso)
- [ ] TC-013 Unsubscribe & Delete (Espresso)
- [ ] TC-014 Share Feed URL (UIAutomator)
- [ ] TC-015 Feed Refresh (UIAutomator)
- [ ] TC-016 Feed URL Parsing (Unit)
- [ ] TC-017 Sort & Filter Logic (Unit)
- [ ] TC-018 Feed & FeedItem DAO (Integration)
- [ ] TC-019 Feed Parsing Speed (Performance)
- [ ] TC-020 Discovery Usability (Manual)

### Sprint 3 — Member 3: Playback & Downloads
- [ ] TC-021 Play/Pause Controls (Espresso)
- [ ] TC-022 Playback Speed (Espresso)
- [ ] TC-023 Download Episode (Espresso)
- [ ] TC-024 Audio Focus & Notification (UIAutomator)
- [ ] TC-025 Background Playback (UIAutomator)
- [ ] TC-026 Playback State Machine (Unit)
- [ ] TC-027 Download Queue Priority (Unit)
- [ ] TC-028 FeedMedia DAO (Integration)
- [ ] TC-029 Download Status Tracking (Integration)
- [ ] TC-030 Long Playback Stability (Manual)

### Sprint 4 — Member 4: Settings & System
- [ ] TC-031 Theme & Display (Espresso)
- [ ] TC-032 Storage & Network Prefs (Espresso)
- [ ] TC-033 Permission Handling (UIAutomator)
- [ ] TC-034 Notification Channels (UIAutomator)
- [ ] TC-035 User Preferences Logic (Unit)
- [ ] TC-036 Storage Path Validation (Unit)
- [ ] TC-037 Data Export/Import (Integration)
- [ ] TC-038 Episode Cache Cleanup (Integration)
- [ ] TC-039 Startup Time & Memory (Performance)
- [ ] TC-040 Accessibility & Edge Cases (Manual)

## Key Decisions

| When | Decision |
|------|----------|
| 2026-05-28 | Use `ActivityTestRule` not `ActivityScenarioRule` — MuMu emulator (ALN-AL00) incompatible |
| 2026-05-28 | Screenshots to `Download/screenshots/` — app private dir deleted on test uninstall |
| 2026-05-28 | Branch naming: `<name>/<module>` not `tc/<name>/<TC-range>` — TC numbers change, module doesn't |
| 2026-05-28 | No PRs — direct squash-merge via CI. PR flow failed 4 times due to merge conflicts |
| 2026-05-28 | CI doc checks: @author tags + file naming convention |
| 2026-05-28 | Docs must be updated BEFORE commit — added to CONTRIBUTING.md checklist and AI-PROMPT.md hard rules |

## Notes

- AntennaPod is Java, our tests are Kotlin. Added `kotlin-android` plugin to `app/build.gradle`.
- PodDBAdapter is singleton: `init(context)` + `getInstance()`. `open()/close()` are no-ops.
- SortOrder is enum (`SortOrder.DATE_NEW_OLD`), not constructable.
- Bottom nav items are dynamic — only first 4 + "More" visible by default.
- Feed cursor columns: use `SELECT_KEY_FEED_ID` ("feed_id"), `SELECT_KEY_ITEM_ID` ("item_id"), not `KEY_ID` ("id").
- TestHelper.kt already exists in `utils/` — reuse, don't recreate.
- Kotlin + UIAutomator deps already added — no more build.gradle changes needed.
