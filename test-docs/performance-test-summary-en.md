# AntennaPod Mobile Testing — Performance Test Progress Summary

**Project**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**App Under Test**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**Date**: 2026-06-11

---

## 1. Overall Progress

| Metric | Value |
|--------|-------|
| Total Performance Test TCs | **2** |
| Total Performance Test Methods | **7** |
| Passed | **7** |
| Failed | **0** |
| Pass Rate | **100%** |
| Contributors | 2 members |
| Sprint Span | Sprint 2, Sprint 4 |

---

## 2. Per-Member Contribution Breakdown

### 2.1 Jianheng Sun — Sprint 2: Subscription & Discovery

| TC-ID | Title | Tests | Runner | Status |
|-------|-------|-------|--------|--------|
| TC-019 | Feed Parsing Speed Benchmark | 3 | AndroidJUnit4 | ✅ Passed |
| **Subtotal** | | **3** | | **100%** |

**Coverage**:
- Feed insert benchmark: 20 iterations, average must be < 100ms per operation
- Feed query benchmark: 20 iterations of full cursor scan, average must be < 50ms
- FeedItem insert with feed reference: 20 iterations, average must be < 200ms

**Performance Thresholds**:

| Operation | Iterations | Threshold | Actual Result |
|-----------|-----------|-----------|---------------|
| Feed insert | 20 | < 100ms avg | ✅ Pass |
| Feed query (full scan) | 20 | < 50ms avg | ✅ Pass |
| FeedItem insert | 20 | < 200ms avg | ✅ Pass |

---

### 2.2 Xintao Wang — Sprint 4: Settings & System

| TC-ID | Title | Tests | Runner | Status |
|-------|-------|-------|--------|--------|
| TC-039 | App Startup Time & Memory Footprint | 4 | AndroidJUnit4 | ✅ Passed |
| **Subtotal** | | **4** | | **100%** |

**Coverage**:
- MainActivity cold launch: must complete in < 5000ms
- PreferenceActivity launch: must complete in < 3000ms
- Repeated MainActivity launch (3 iterations): average must be < 3000ms
- Memory footprint after launch: must stay below 256MB

**Performance Thresholds**:

| Operation | Threshold | Actual Result |
|-----------|-----------|---------------|
| MainActivity cold launch | < 5000ms | ✅ Pass |
| PreferenceActivity launch | < 3000ms | ✅ Pass |
| Repeated launch average (3x) | < 3000ms | ✅ Pass |
| Memory after launch | < 256MB | ✅ Pass |

---

## 3. Contribution Comparison

| Member | Sprint | Module | TCs | Test Methods | Share |
|--------|--------|--------|-----|-------------|-------|
| **Jianheng Sun** | 2 | Subscription & Discovery | 1 | **3** | 42.9% |
| **Xintao Wang** | 4 | Settings & System | 1 | **4** | 57.1% |
| **Total** | | | **2** | **7** | **100%** |

```
Jianheng Sun    ███████████████████████████████████           3 (42.9%)
Xintao Wang     ████████████████████████████████████████████  4 (57.1%)
```

---

## 4. Test Architecture

| Component | Description |
|-----------|-------------|
| Runner | `AndroidJUnit4` (on-device execution) |
| Timing Method | `System.nanoTime()` — manual measurement (no Macrobenchmark/Microbenchmark library configured) |
| Memory Method | `Runtime.getRuntime()` — totalMemory minus freeMemory after GC |
| Idle Sync | `InstrumentationRegistry.getInstrumentation().waitForIdleSync()` before measurement |
| Iterations | 20 per DB operation, 3 per activity launch cycle |
| Database | Real SQLite via `PodDBAdapter.init(context)` with `tearDownTests()` cleanup |

---

## 5. File Inventory

```
app-under-test/antennapod/app/src/androidTest/java/de/danoeh/antennapod/
└── performance/
    ├── TC019_FeedParsingBenchmarkTest.kt        ← Jianheng Sun (3 tests)
    └── TC039_StartupMemoryBenchmarkTest.kt      ← Xintao Wang (4 tests)
```

---

## 6. Full Project Test Distribution (for context)

| Method | TCs | Test Methods | Share |
|--------|-----|-------------|-------|
| Espresso (in-app UI) | 13 | 54 | 32.5% |
| Unit Tests (JUnit) | 8 | 132 | 20.0% |
| UIAutomator (cross-app / system UI) | 7 | 21 | 17.5% |
| Integration (SQLite / DB) | 6 | 37 | 15.0% |
| Manual / Exploratory | 4 | 80 steps | 10.0% |
| **Performance (benchmarks)** | **2** | **7** | **5.0%** |
| **Total** | **40** | **251 automated + 80 manual** | **100%** |

---

## 7. Key Lessons

| Category | Takeaway |
|----------|----------|
| No benchmark library | Project has no `androidx.benchmark` dependency — manual `System.nanoTime()` is sufficient for threshold-based assertions |
| GC before memory measurement | Call `Runtime.getRuntime().gc()` before reading memory to get stable results |
| `waitForIdleSync()` | Essential before timing measurements — ensures activity is fully rendered |
| Threshold design | Conservative thresholds (100ms insert, 50ms query, 5s cold launch, 256MB memory) pass reliably on emulator and leave margin for real devices |
| Iteration count | 20 iterations for DB ops averages out I/O variance; 3 iterations for launch gives warm-cache signal |

---

## 8. Conclusion

**Performance testing is 100% complete.** Two members delivered 2 performance test TCs with 7 test methods — all passing. TC-019 validates database operation throughput (feed insert < 100ms, query < 50ms, item insert < 200ms across 20 iterations). TC-039 validates app startup latency (cold launch < 5s, warm < 3s) and memory footprint (< 256MB post-launch). All measurements use manual `System.nanoTime()` timing with conservative thresholds suitable for CI regression detection.
