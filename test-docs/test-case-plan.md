# Test Case Assignment Plan — AntennaPod Mobile Testing

> **Living document** — TC ranges, member counts, and method distributions can be extended.
> When adding new members or test cases, use the next available TC-IDs and update the assignment table.
> See `AI-GUIDE.md` for setup, conventions, and patterns.
>
> **Team Members**: Your TC plans below are suggested starting points. If a TC doesn't fit your
> implementation or you find a better testing angle, discuss with the team lead and adjust.
> The module and method distribution are the only constraints — individual titles are flexible.

## Testing Methods

This project uses complementary testing methods. Mix and match per test case as appropriate.

| Method | Tool / Framework | Best For |
|--------|-----------------|----------|
| **Espresso** | `androidx.test.espresso` | In-app UI: button clicks, text input, view assertions |
| **UIAutomator** | `androidx.test.uiautomator` | Cross-app & system UI: file picker, notifications, system dialogs |
| **Unit Test** | JUnit 4 + Mockito | Business logic: parsers, config, helpers |
| **Integration Test** | SQLite / PodDBAdapter | Data layer: DB schema, DAO queries, data integrity |
| **Manual / Exploratory** | Checklist + device | UX & usability: visual feel, audio quality, accessibility |
| **Performance** | Benchmark / manual timing | Metrics: startup time, parsing speed, memory footprint |
| **Static Analysis** | Androguard (Python) | APK manifest: permissions audit, component exposure, security flags |

---

## Team Members & Current Assignment

> TC ranges are **allocated blocks** — not fixed caps. To add more cases, extend the range.

| Member | Module | Current TC Range |
|--------|--------|------------------|
| **Tianyu Yao** (Lead) | Core Foundation | TC-001 ~ TC-010 | Finalized |
| **Jianheng Sun** | Subscription & Discovery | TC-011 ~ TC-020 | Suggested |
| **Yuanbing Wang** | Playback & Downloads | TC-021 ~ TC-030 | Suggested |
| **Xintao Wang** | Settings & System | TC-031 ~ TC-040 | Suggested |

---

## Tianyu Yao — Core Foundation (TC-001 ~ TC-010) Finalized

| TC-ID | Method | Title | Priority |
|-------|--------|-------|----------|
| TC-001 | Espresso | App Launch & Main Screen | High |
| TC-002 | Espresso | Subscribe to Podcast | High |
| TC-003 | Espresso | Play Episode | High |
| TC-004 | Espresso | Queue Management (Add / Remove / Reorder) | Medium |
| TC-005 | Espresso | In-App Search & Discovery Browse | Medium |
| TC-006 | UIAutomator | OPML Import via System File Picker | High |
| TC-007 | Unit Test (JUnit) | Feed Entity Field Validation | High |
| TC-008 | Unit Test (JUnit) | FeedItem & FeedMedia Entity Validation | High |
| TC-009 | Integration (SQLite) | PodDBAdapter Schema & Table Creation | Medium |
| TC-010 | Manual / Exploratory | First-Launch User Flow | Medium |

---

## Jianheng Sun — Subscription & Discovery (TC-011 ~ TC-020) Suggested

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

---

## Yuanbing Wang — Playback & Downloads (TC-021 ~ TC-030) Suggested

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

## Xintao Wang — Settings & System (TC-031 ~ TC-040) Suggested

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

## Approximate Method Distribution

> Actual counts from the 40-TC plan. Percentages are guidelines, not quotas.

| Method | Count | Percentage | Target |
|--------|-------|-----------|--------|
| Espresso | 13 | 32.5% | ~35% |
| UIAutomator | 7 | 17.5% | ~20% |
| Unit Test (JUnit) | 8 | 20% | ~20% |
| Integration (SQLite) | 6 | 15% | ~15% |
| Manual / Exploratory | 4 | 10% | ~10% |
| Performance | 2 | 5% | ~5% |

Note: Integration tests use PodDBAdapter (raw SQLite) for DAO and ContentProvider coverage.

---

## Source Code Locations

```
app-under-test/antennapod/app/src/
├── androidTest/java/de/danoeh/antennapod/
│   ├── espresso/         ← Espresso UI tests
│   ├── uiautomator/      ← UIAutomator tests
│   ├── integration/      ← SQLite integration tests
│   ├── performance/      ← Benchmark tests
│   └── utils/            ← Shared utilities (TestHelper.kt)
├── test/java/de/danoeh/antennapod/
│   ├── unit/              ← JUnit unit tests
│   └── manual/            ← Manually executed test code
└── main/java/de/danoeh/antennapod/   ← App source (read-only reference)
```

## Standard Conventions

| Aspect | Convention |
|--------|-----------|
| Language | English (code, comments, documentation) |
| Attribution | `@author [English Name]` in class KDoc |
| Espresso runner | `@RunWith(AndroidJUnit4::class)` |
| UIAutomator | `UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())` |
| Unit test | JUnit 4 + Mockito, no Android framework dependency |
| Integration test | `@RunWith(AndroidJUnit4::class)` + `PodDBAdapter` |
| Performance | `androidx.benchmark` or manual `System.nanoTime()` |

## How to Extend This Plan

- **Modifying a suggested TC**: Team members can change titles, swap priorities, or redesign
  TC content as long as the testing method and module scope stay consistent. Discuss changes
  with the team lead and update this document.
- **Adding a member**: Insert a new section with the next available TC-ID block. No need to renumber.
- **Adding cases to an existing member**: Extend their TC range and add rows to their table.
- **Adding a new testing method**: Add it to the Methods table. Reference it in any TC's Method column.
- **Splitting/merging modules**: Reassign TC ranges as needed.
- **Dependencies**: See `AI-GUIDE.md` for how to add test library dependencies to `libs.versions.toml`.
