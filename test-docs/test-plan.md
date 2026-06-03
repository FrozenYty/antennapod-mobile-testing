# Test Plan — AntennaPod Mobile

> This plan describes the overall testing strategy. For detailed TC assignments, see `test-case-plan.md`.
> For how to contribute, see `AI-GUIDE.md`.

## 1. Introduction

This document outlines the testing strategy for the AntennaPod Android application — an
open-source podcast manager.

## 2. Test Objectives

- Verify core podcast subscription and playback functionality
- Identify UI/UX issues across different Android versions
- Validate OPML import/export, download management, and settings
- Ensure app stability during prolonged playback
- Cover both in-app interactions and cross-app/system flows

## 3. Scope

### In Scope
- Podcast subscription (subscribe, unsubscribe, discovery, search)
- Playback (play/pause, speed control, queue management, background playback)
- Downloads (episode download, offline access, cache management)
- Settings (theme, storage, network, notifications)
- OPML import and export
- Database schema (PodDBAdapter, Feed/FeedItem/FeedMedia CRUD)
- Audio focus and notification handling

### Out of Scope
- Backend/server-side testing (AntennaPod has no server)
- Third-party podcast content validation
- Play Store / F-Droid release testing
- Chromecast / Wear OS / Android Auto integration testing

## 4. Test Approach

Uses a multi-method strategy for broad coverage:

| Method | Scope |
|--------|-------|
| **Espresso** | In-app UI automation: activity launches, button clicks, view assertions |
| **UIAutomator** | Cross-app & system UI: file picker, notifications, permission dialogs |
| **Unit Tests** | Business logic: feed URL parsing, playback state, preferences |
| **Integration Tests** | Data layer: PodDBAdapter schema, DAO queries, data integrity |
| **Manual / Exploratory** | UX evaluation: visual feel, audio quality, accessibility |
| **Performance** | Metrics: startup time, feed parsing speed, memory footprint |
| **Static Analysis** | APK manifest: permissions, components, security flags (Androguard) |

## 5. Test Environment

| Item | Details |
|------|---------|
| Device | Android Emulator (API 32+) / Physical device |
| OS | Android 12+ |
| App Version | `app-under-test/antennapod` (playDebug build) |
| Test Runner | `androidx.test.runner.AndroidJUnitRunner` |

## 6. Schedule

| Phase | Activity | Status |
|-------|----------|--------|
| Test Design | Write test plan, design test cases, assign modules | Completed |
| Sprint 1 | Core Foundation tests (TC-001~010, Tianyu Yao) | Completed |
| Sprint 2 | Subscription & Discovery (TC-011~020, Jianheng Sun) | Done |
| Sprint 3 | Playback & Downloads (TC-021~030, Yuanbing Wang) | Done |
| Sprint 4 | Settings & System (TC-031~040, Xintao Wang) | Done |
| Sprint 5 | Bug fixes, regression, final report | Pending |

## 7. Risks

- AntennaPod source is Java-heavy; Kotlin test code must interoperate with Java APIs
- PodDBAdapter uses raw SQLite (no Room); integration tests require manual connection management
- Network-dependent features (subscription, discovery, feed refresh) may be affected by server availability
- Audio playback testing requires media files; use small local test files for reproducible results
- OPML picker and other system UIs require UIAutomator, which is slower and less reliable than Espresso
