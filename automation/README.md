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
