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

Push to any branch except `main` triggers:

```
ci-auto-merge.yml
     │
     ▼ compile instrumented tests
     ▼ compile unit tests
     ▼ run unit tests
     ▼ squash merge into main → delete branch
```

See `CONTRIBUTING.md` for the full workflow.

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
