# AntennaPod Mobile Testing — Manual / Exploratory Test Progress Summary

**Project**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**App Under Test**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**Date**: 2026-06-11

---

## 1. Overall Progress

| Metric | Value |
|--------|-------|
| Total Manual Test TCs | **4** |
| Total Manual Test Steps | **80** |
| Executable Steps | **53** |
| Passed | **52** |
| Partial | **1** |
| Failed | **0** |
| N/A (environment limitation) | **27** |
| Pass Rate (executable) | **98.1%** |
| Contributors | 4 members |
| Sprint Span | Sprint 1 – Sprint 4 |

---

## 2. Per-Member Contribution Breakdown

### 2.1 Tianyu Yao (Team Lead) — Sprint 1: Core Foundation

| TC-ID | Title | Steps | Passed | Partial | N/A | Status |
|-------|-------|-------|--------|---------|-----|--------|
| TC-010 | First-Launch User Flow | 20 | 19 | 1 | 0 | ✅ 95% |
| **Subtotal** | | **20** | **19** | **1** | **0** | |

**Test Scope**:
- Complete first-launch onboarding flow from clean state
- Bottom navigation verification, feed subscription via RSS URL
- Episode discovery, playback initiation, mini-player interaction
- Device rotation (landscape/portrait), back navigation, app exit/relaunch
- State persistence across app restarts

**Execution Details**:
- Device: MuMu emulator (ALN-AL00, Android 12)
- Date: 2026-05-28
- Feed used: `https://feeds.npr.org/500005/podcast.xml` (NPR News Now)
- Partial (step 14): Stream playback failed — NPR redirects to Spotify CDN blocked on emulator; app showed error dialog gracefully

---

### 2.2 Jianheng Sun — Sprint 2: Subscription & Discovery

| TC-ID | Title | Steps | Passed | Partial | N/A | Status |
|-------|-------|-------|--------|---------|-----|--------|
| TC-020 | Discovery Page Usability | 20 | 16 | 0 | 4 | ✅ 100% (executable) |
| **Subtotal** | | **20** | **16** | **0** | **4** | |

**Test Scope**:
- Discovery and subscription workflow end-to-end
- Bottom nav, More menu popup, Add Podcast screen
- RSS URL search, feed preview, subscribe confirmation
- Episode list navigation, multi-select mode, rotation

**Execution Details**:
- Device: test33 AVD, 1080x2400
- Date: 2026-06-01
- Feed used: `https://feeds.npr.org/500005/podcast.xml` (NPR News Now)
- N/A (steps 16-17): Multi-select requires multiple subscriptions — fresh install only has one
- N/A (steps 19-20): AVD rotation via adb not reliably testable
- 4 screenshots captured

---

### 2.3 Yuanbing Wang — Sprint 3: Playback & Downloads

| TC-ID | Title | Steps | Passed | Partial | N/A | Status |
|-------|-------|-------|--------|---------|-----|--------|
| TC-030 | Long Playback Stability | 20 | 0 | 0 | 20 | ⚠️ N/A (emulator) |
| **Subtotal** | | **20** | **0** | **0** | **20** | |

**Test Scope**:
- Extended playback (5+ min continuous, 10+ min total)
- Seek operations (forward drag, skip-back button)
- Background playback via Home button, notification controls
- Playback speed changes (1.0x ↔ 1.5x)
- Peripheral changes (wired headphones, Bluetooth connect/disconnect)
- Sleep timer, incoming call interruption, lock screen playback

**Execution Details**:
- Device: MuMu emulator (ALN-AL00, Android 12)
- Date: 2026-06-03
- All 20 steps N/A — emulator lacks: downloaded episodes, Bluetooth, headphone jack, phone call simulation
- Playback logic validated by unit tests (TC-026, TC-027: 21/21) and integration tests (TC-028, TC-029: 13/13)
- Recommendation: Execute on physical device with pre-downloaded content

---

### 2.4 Xintao Wang — Sprint 4: Settings & System

| TC-ID | Title | Steps | Passed | Partial | N/A | Status |
|-------|-------|-------|--------|---------|-----|--------|
| TC-040 | Accessibility & Edge Cases | 20 | 17 | 0 | 3 | ✅ 100% (executable) |
| **Subtotal** | | **20** | **17** | **0** | **3** | |

**Test Scope**:
- Accessibility with default and large font sizes
- Screen reader (TalkBack) navigation
- Theme toggling (light/dark/full black)
- Settings navigation depth (nested screens, back navigation)
- Network-disabled resilience, visual contrast assessment
- Rotation on settings screens

**Execution Details**:
- Device: MuMu emulator (ALN-AL00, Android 12)
- Date: 2026-06-03
- N/A (step 3): TalkBack unavailable on MuMu emulator
- Font size tested at 130% system default
- All view visibility validated by automated tests (TC-031–034)
- Recommendation: Physical device for TalkBack and extreme font sizes

---

## 3. Contribution Comparison

| Member | Sprint | Module | TCs | Steps | Executable | Passed | Share (steps) |
|--------|--------|--------|-----|-------|-----------|--------|---------------|
| **Tianyu Yao** | 1 | Core Foundation | 1 | **20** | 20 | 19 + 1 partial | 25% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 1 | **20** | 16 | 16 | 25% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 1 | **20** | 0 | 0 (all N/A) | 25% |
| **Xintao Wang** | 4 | Settings & System | 1 | **20** | 17 | 17 | 25% |
| **Total** | | | **4** | **80** | **53** | **52 + 1 partial** | **100%** |

```
Tianyu Yao      ████████████████████████████████████████ 19 pass + 1 partial (20 steps)
Jianheng Sun    █████████████████████████████████        16 pass + 4 N/A    (20 steps)
Yuanbing Wang   ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  0 pass + 20 N/A   (20 steps)
Xintao Wang     ██████████████████████████████████████   17 pass + 3 N/A    (20 steps)
```

---

## 4. N/A Breakdown by Reason

| Reason | Steps Affected | TCs |
|--------|---------------|-----|
| Emulator lacks downloaded episodes + peripherals | 20 | TC-030 |
| AVD rotation not reliable via adb | 2 | TC-020 |
| Multi-select requires multiple subscriptions | 2 | TC-020 |
| TalkBack unavailable on MuMu | 3 | TC-040 |
| **Total N/A** | **27** | |

---

## 5. Test Execution Environment

| Field | Value |
|-------|-------|
| Primary Device | MuMu emulator (ALN-AL00, Android 12, x86_64) |
| Secondary Device | test33 AVD (1080x2400) |
| Build | playDebug |
| Animation State | Disabled (window/transition/animator scales = 0.0) |
| Clean State | `adb shell pm clear de.danoeh.antennapod.debug` before each TC |
| Screenshots | Saved to `/storage/emulated/0/Download/screenshots/` and `screenshots/` directory |

---

## 6. File Inventory

```
app-under-test/antennapod/app/src/test/java/de/danoeh/antennapod/
└── manual/
    ├── TC010_FirstLaunchUserFlowTest.kt          ← Tianyu Yao (20 steps)
    ├── TC020_DiscoveryUsabilityTest.kt           ← Jianheng Sun (20 steps)
    ├── TC030_LongPlaybackStabilityTest.kt        ← Yuanbing Wang (20 steps)
    └── TC040_AccessibilityEdgeCasesTest.kt       ← Xintao Wang (20 steps)

test-results/
└── manual-test-result.md                         ← Execution records for all 4 TCs
```

---

## 7. Full Project Test Distribution (for context)

| Method | TCs | Test Methods | Share |
|--------|-----|-------------|-------|
| Espresso (in-app UI) | 13 | 54 | 32.5% |
| Unit Tests (JUnit) | 8 | 132 | 20.0% |
| UIAutomator (cross-app / system UI) | 7 | 21 | 17.5% |
| Integration (SQLite / DB) | 6 | 37 | 15.0% |
| **Manual / Exploratory** | **4** | **80 steps** | **10.0%** |
| Performance (benchmarks) | 2 | 7 | 5.0% |
| **Total** | **40** | **251 automated + 80 manual** | **100%** |

---

## 8. Key Lessons

| Category | Takeaway |
|----------|----------|
| Emulator limitations | MuMu lacks Bluetooth, headphone routing, phone call simulation — TC-030 entirely N/A |
| CDN blocking | NPR feed redirects to `prfx.byspotify.com` which is unreachable from emulator — graceful error dialog confirmed |
| Complementary automated tests | Manual TC-030 logic is validated by unit/integration tests (TC-026–029, 34/34 passing) |
| Screenshot strategy | Save to `/storage/emulated/0/Download/screenshots/` to survive APK uninstall |
| Multi-select precondition | Testing multi-select requires at least 2 subscriptions — seed data before testing |
| TalkBack on emulator | Screen reader functionality unavailable — physical device required for accessibility audit |
| Automated cross-validation | Steps verified by Espresso/UIAutomator TCs (TC-031–034) reduce manual regression load |

---

## 9. Conclusion

**Manual testing is 100% complete (within executable scope).** Four members designed 4 manual test TCs totaling 80 steps across 4 sprints. Of 53 executable steps, 52 passed and 1 partially passed (CDN blocking, not an app bug). 27 steps are N/A due to emulator limitations (no peripherals, no TalkBack, no downloaded content). The core user flows — first launch, discovery/subscription, settings/accessibility — are verified working. TC-030 (long playback stability) requires physical device re-execution for full coverage; its logic is backed by 34 passing automated tests.
