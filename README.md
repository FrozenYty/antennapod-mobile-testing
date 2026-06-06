# AntennaPod Mobile Testing

A structured mobile testing project for [AntennaPod](https://github.com/AntennaPod/AntennaPod) — an open-source podcast manager (MIT license).

## Overview

This repository contains the test documentation, manual test results, bug reports, screenshots, and automation setup for testing the AntennaPod Android app. The app-under-test source code is included for traceability.

> **Language**: All code, comments, documentation, and commit messages must be written in **English**.

## Getting Started

1. Read [CONTRIBUTING.md](CONTRIBUTING.md) — commit rules, workflow, CI checks
2. Read [test-docs/test-case-plan.md](test-docs/test-case-plan.md) — find your assigned TC range
3. Read [AI-GUIDE.md](AI-GUIDE.md) — patterns, pitfalls, complete workflow
4. `git pull` to get latest, then write tests, compile, run, document, commit, push to main

> **For AI assistants**: Feed [AI-PROMPT.md](AI-PROMPT.md) + [AI-GUIDE.md](AI-GUIDE.md) into your context before writing any code.

## Directory Structure

```
antennapod-mobile-testing/
├── README.md
├── TASK-BRIEF.md
├── AI-PROMPT.md
├── CONTRIBUTING.md
├── AI-GUIDE.md
├── PROGRESS.md
├── .gitignore
├── .gitattributes
├── .github/                     # CI workflow
├── app-under-test/              # Application source code under test
│   └── antennapod/              # AntennaPod Android source (MIT license)
│       └── app/src/
│           ├── androidTest/java/de/danoeh/antennapod/
│           │   ├── espresso/    # Espresso UI tests
│           │   ├── uiautomator/ # UIAutomator tests
│           │   ├── integration/ # SQLite/ContentProvider tests
│           │   ├── performance/ # Benchmark tests
│           │   └── utils/       # Shared test utilities
│           └── test/java/de/danoeh/antennapod/
│               ├── unit/        # JUnit unit tests
│               └── manual/      # Manually executed test code
├── test-docs/                   # Test documentation
│   ├── test-plan.md
│   ├── test-case-plan.md
│   ├── test-cases.md
│   ├── test-summary-report.md
│   ├── static-analysis-report.md
│   ├── frida-vibe-coding-report.md
│   ├── bug-report-template.md
│   └── callgraphs/              # Generated call graph images & HTML reports
├── bug-reports/                 # Filed bug reports (bug-XXX.md)
├── screenshots/                 # Test evidence screenshots
├── automation/                  # Test runner scripts, static analysis, call graphs
└── test-results/                # Test execution results
```

## Test Scope

- **Functional Testing**: Podcast subscription, episode playback, download management, queue operations
- **UI Testing**: Bottom navigation, player controls, settings screens, theme switching
- **Compatibility Testing**: Android API levels, screen sizes, playback on various configurations
- **Performance Testing**: App startup time, feed parsing speed, memory footprint

## Testing Methods

This project uses multiple complementary testing approaches:

| Method | Tool | Scope |
|--------|------|-------|
| Espresso | `androidx.test.espresso` | In-app UI automation |
| UIAutomator | `androidx.test.uiautomator` | Cross-app & system UI |
| Unit Tests | JUnit 4 + Mockito | Business logic |
| Integration Tests | SQLite / ContentProvider | Data layer |
| Manual | Structured checklist | UX & accessibility |
| Performance | Benchmark / timer | Speed & memory |
| Static Analysis | Androguard (Python) | Permissions, components, security flags |

See `AI-GUIDE.md` for test writing patterns. See `test-docs/test-case-plan.md` for TC assignments. See `automation/README.md` for static analysis usage.

## App Under Test

- **App Name**: AntennaPod
- **Platform**: Android
- **Build System**: Gradle (Groovy DSL, multi-module)
- **Build Flavor**: `play` (Google Play) or `free` (F-Droid)
- **Min SDK**: 23
- **Language**: Java (core) + Kotlin
- **License**: GPL-3.0
