# Automation

Test runner script and CI workflow for the AntennaPod Mobile Testing project.

## Quick Run

```bash
./automation/run-tests.sh                              # all instrumented tests
./automation/run-tests.sh TC001_AppLaunchTest          # single class
./automation/run-tests.sh "de.danoeh.antennapod.espresso.*"  # all Espresso
```

The script handles: disable animations → install app → run tests.

## CI/CD (GitHub Actions)

Every push to `main` triggers:

```
ci.yml
     │
     ▼ compile instrumented tests
     ▼ compile unit tests
     ▼ run unit tests
     ▼ doc checks (@author, naming)
```

If any check fails, fix the issue and push again.

## Static Analysis (Androguard)

Python-based APK manifest and security analysis without running the app.

```bash
# Install dependencies
pip install -r automation/requirements.txt

# Run analysis (human-readable)
python automation/static_analysis.py

# Run analysis (JSON output for CI)
python automation/static_analysis.py --json

# Specify custom APK path
python automation/static_analysis.py --apk path/to/app.apk
```

### What it checks

| Category | Checks |
|----------|--------|
| **App Info** | Package name, version, min/target SDK |
| **Permissions** | Declared permissions, dangerous permissions, risk assessment for podcast app context |
| **Components** | Activities, services, receivers, providers with intent-filters |
| **Security Flags** | debuggable, allowBackup, network security config |
| **Signature** | V1/V2/V3 signing verification |

### Risk Levels

- **[PASS]** — Permission is expected for a podcast/media app
- **[WARN]** — Unusual permission that may need justification
- **[FAIL]** — High-risk permission (contacts, location, camera, etc.)

## Call Graph Visualization

Generates call graph images from APK bytecode using Androguard's Analysis API.

```bash
python automation/callgraph.py
# → test-docs/callgraphs/callgraph-methods.png    (method-level network)
# → test-docs/callgraphs/callgraph-package.png    (package interactions)
# → test-docs/callgraphs/callgraph-stats.png      (top callers/callees)
```

### Output Images

| Image | Description |
|-------|-------------|
| `callgraph-methods.png` | Method call network for top 60 most-connected classes |
| `callgraph-package.png` | Package-level interaction graph (edge weight ≥ 2) |
| `callgraph-stats.png` | Bar charts: top 15 callers (outgoing) and top 15 callees (incoming) |

Based on lecture L11 techniques: Androguard DEX → Analysis → create_xref() → call graph extraction.

## Comprehensive Report Generator

Generates a full static analysis report covering all project dimensions.

```bash
# Generate report (saves to test-docs/reports/static-analysis-report.md)
python automation/generate_report.py

# Custom output path
python automation/generate_report.py --output reports/my-report.md
```

### Report Sections

| Section | Content |
|---------|---------|
| Project Overview | App version, SDK, TC count, screenshots |
| Test Method Distribution | Coverage by method (Espresso/UIAutomator/Unit/Integration/Manual/Performance) |
| Test Results Summary | Per-sprint pass/partial/N/A counts |
| APK Manifest Analysis | Permissions, components, security flags (Androguard) |
| Code Structure | Test file inventory by directory |
| Screenshot Inventory | Per-file size, ownership, sprint attribution |
| Findings & Recommendations | Risk assessment and action items |

## Dynamic Call Graph (Frida Runtime Tracing)

Captures **actual method call edges at runtime** by hooking Java methods with Frida as the app runs. Unlike `callgraph.py` (static DEX/bytecode analysis), this traces real execution paths driven by user interaction.

### Prerequisites

```bash
# 1. Install Python deps
pip install -r automation/requirements.txt

# 2. Set up frida-server on the Android device
#    Download the matching frida-server binary from:
#    https://github.com/frida/frida/releases
#    (e.g., frida-server-16.x.x-android-arm64.xz for ARM64 devices)
#
#    Push it to the device:
adb push frida-server-16.x.x-android-arm64 /data/local/tmp/frida-server
adb shell chmod 755 /data/local/tmp/frida-server

# 3. Start frida-server (in a separate terminal, keep running):
adb shell su -c /data/local/tmp/frida-server

# 4. Verify the device is visible:
frida-ps -U
```

### Usage

```bash
# Default: install & launch the debug APK, trace for 30s, open interactive report in browser
python automation/frida_callgraph.py

# Longer trace (90s) for deeper coverage
python automation/frida_callgraph.py --duration 90

# Attach to an already-running app (skip install/launch)
python automation/frida_callgraph.py --attach

# Specify device serial (e.g., MuMu emulator)
python automation/frida_callgraph.py --device 127.0.0.1:7555

# Trace only a specific sub-package
python automation/frida_callgraph.py --class-filter de.danoeh.antennapod.activity

# CLI-only: skip HTML report generation
python automation/frida_callgraph.py --no-html

# Don't auto-open browser (HTML still generated)
python automation/frida_callgraph.py --no-browser

# JSON-only export (skip all visualizations and HTML)
python automation/frida_callgraph.py --no-vis --no-html
```

### Interactive HTML Report

After tracing, the script generates `test-docs/callgraphs/frida-callgraph.html` and automatically opens it in your default browser. The report has **three tabs**:

| Tab | Content |
|-----|---------|
| 🕸 **Network Graph** | Interactive vis-network — zoom, pan, drag nodes, hover for details, search bar, color-coded by sub-package, physics simulation, toggle physics on/off |
| 📊 **Stats Charts** | Chart.js horizontal bar charts — Top 15 Callers (outgoing) + Top 15 Callees (incoming), same color scheme as static PNGs |
| 📋 **Edge Table** | Full sortable/searchable table — caller, callee, call count. Click column headers to sort, use the filter box to search |

The HTML file is fully self-contained: all data is embedded as JSON, only CDN-hosted libraries (vis-network, Chart.js) are loaded externally.

### How It Works

```
┌─────────────┐    send(edges)     ┌──────────────────┐
│ frida_hooks_cli │ ──────────────→ │  frida_callgraph  │
│    .js          │  -o log file    │     .py           │
│  (Android)      │                 │  (Host Python)    │
└─────────────────┘                 └──────────────────┘
       │                                      │
  Hook all methods                     Parse log file
  in de.danoeh.*                       Generate visualizations
       │                                      │
  Exception.getStackTrace()            ┌─────▼──────┐
  → find caller class                  │ callgraphs/ │
       │                               │  frida-*    │
  console.log(JSON)                    └────────────┘
```

1. **`frida_hooks_cli.js`** — Injected into the running APK via Frida CLI:
   - Enumerates all loaded classes matching `de.danoeh.antennapod.*`
   - For each class, gets declared methods via Java reflection
   - Hooks each method and captures the caller via `Exception.getStackTrace()`
   - Batches `{caller, callee}` pairs via `console.log(JSON)` to the `-o` log file

2. **`frida_callgraph.py`** — Python orchestrator:
   - Installs & launches the APK, attaches Frida, injects the JS hook
   - Collects edge data for the configured trace duration
   - Aggregates into a weighted call graph (`defaultdict(int)` on caller→callee)
   - Generates bar charts, package heatmap, top-edges chart
   - Exports JSON for further analysis

### Output

| File | Description |
|------|-------------|
| `frida-callgraph.html` | **Interactive report** — network graph, stats charts, edge table (opens in browser automatically) |
| `frida-callgraph.json` | Full edge list (top 500), meta stats, top callers/callees |
| `frida-callgraph-stats.pdf/png` | Bar chart: top 15 callers (outgoing) and top 15 callees (incoming) |
| `frida-callgraph-heatmap.pdf/png` | Package-level call density heatmap |
| `frida-callgraph-edges.pdf/png` | Top 20 individual caller→callee edges by call count |

### Comparison: Static vs Dynamic Call Graph

| Aspect | `callgraph.py` (Androguard) | `frida_callgraph.py` (Frida) |
|--------|---------------------------|------------------------------|
| Method | Static DEX bytecode analysis | Runtime method hooking |
| Coverage | All possible code paths | Actual executed code paths |
| Requires device | No | Yes (Android + frida-server) |
| Speed | Minutes (one-time analysis) | Real-time (interactive tracing) |
| Excludes | Dead code, reflection calls | Un-executed branches, cold paths |
| Includes | All cross-references | Only paths hit during trace session |
| Best for | Architecture overview, security audit | User-flow call graphs, debugging |

Use both together: static for completeness, dynamic for real-world execution paths.

## Manual Commands

### Compile
```bash
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugAndroidTestSources   # instrumented
./gradlew :app:compilePlayDebugUnitTestSources       # unit
```

### Run Specific Test
```bash
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=de.danoeh.antennapod.espresso.TC001_AppLaunchTest
```

### Run All Unit Tests
```bash
./gradlew :app:testPlayDebugUnitTest
```

### Pull Screenshots After Test Run
```bash
MSYS2_ARG_CONV_EXCL="*" adb pull /storage/emulated/0/Download/screenshots/ ./screenshots/
adb shell rm -rf /storage/emulated/0/Download/screenshots/
```

## Test Source Locations

```
app-under-test/antennapod/app/src/
├── androidTest/java/de/danoeh/antennapod/
│   ├── espresso/         ← Espresso UI tests
│   ├── uiautomator/      ← UIAutomator tests
│   ├── integration/      ← SQLite integration tests
│   ├── performance/      ← Benchmark tests
│   └── utils/            ← Shared utilities (TestHelper.kt)
└── test/java/de/danoeh/antennapod/
    ├── unit/              ← JUnit unit tests
    └── manual/            ← Manually executed test code
```
