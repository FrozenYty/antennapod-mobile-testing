# Automation

Test runner and scripts for the AntennaPod Mobile Testing project.

> **Language**: All code, comments, and documentation in **English**.

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

## Running Tests

### Compile
```bash
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugAndroidTestSources   # instrumented
./gradlew :app:compilePlayDebugUnitTestSources       # unit
```

### Specific Instrumented Test Class
```bash
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=de.danoeh.antennapod.espresso.TC001_AppLaunchTest
```

### All Unit Tests
```bash
./gradlew :app:testPlayDebugUnitTest
```

## Test Environment

| Item | Value |
|------|-------|
| Build | `./gradlew :app:assemblePlayDebug :app:assemblePlayDebugAndroidTest` |
| Test Runner | `androidx.test.runner.AndroidJUnitRunner` |
| Min SDK | 23 |
| Target SDK | (from app/build.gradle) |
