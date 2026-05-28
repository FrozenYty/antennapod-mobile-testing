# AI Prompt — AntennaPod Mobile Testing

Copy this entire message and send it to your AI assistant (Claude, ChatGPT, etc.).

---

I need to complete my assigned test cases for the AntennaPod mobile testing course project.

## Context

- **Project**: AntennaPod Mobile Testing — testing the AntennaPod open-source Android podcast manager (MIT license)
- **My role**: Member [2/3/4], assigned module and TC range per `test-docs/test-case-plan.md`
- **Language**: Everything (code, comments, docs, commits) must be in English

## Before Writing Any Code

Read these files in order:

1. `CONTRIBUTING.md` — branch naming `<name>/<module>`, commit format `type: description + Author: Name`, file organization, what NOT to do
2. `AI-GUIDE.md` — complete workflow (Plan → Code → Compile → Run → Document → Commit → PR), code patterns, pitfalls
3. `test-docs/test-case-plan.md` — find my member section and exact TC list with methods
4. `test-docs/test-cases.md` — existing TC specs (copy the table format for my new TCs)
5. `app/.../utils/TestHelper.kt` — shared utilities
6. The reference example for each method I need

## What I Need You to Do

For EACH TC in my range:

1. **Plan** — confirm which method and which app feature it targets
2. **Code** — create the test class in the correct directory, with `@author [My English Name]` in KDoc
3. **Compile** — run `./gradlew :app:compilePlayDebugAndroidTestSources` (or unit test variant) and fix ALL errors
4. **Run** — execute the test on my device/emulator and report Passed / Partial / Failed
5. **Document** — update these files:
   - `test-docs/test-cases.md` — append my TC specs
   - `test-results/manual-test-result.md` — add result rows
   - `test-docs/test-summary-report.md` — add key findings
6. **Commit** — one commit per logical batch, format: `<type>: <description>\n\nAuthor: <Name>`
7. **Push & PR** — branch `<my-name>/<my-module>`, push triggers auto PR and merge

## Hard Rules

- NEVER modify `app/src/main/` — app source is read-only
- NEVER use `git add -A` or `git add .` — stage files individually
- NEVER create new directories — all folders are pre-created
- NEVER write Chinese in code, comments, docs, or commit messages
- Only add screenshots that have clear evidence value
- Every test class must have `@author [My English Name]` in its KDoc

## Package Conventions

| Where | Package |
|-------|---------|
| Espresso | `de.danoeh.antennapod.espresso` |
| UIAutomator | `de.danoeh.antennapod.uiautomator` |
| Integration | `de.danoeh.antennapod.integration` |
| Performance | `de.danoeh.antennapod.performance` |
| Unit | `de.danoeh.antennapod.unit` |

## Environment

```bash
export JAVA_HOME=<path-to-jdk-21>
export ANDROID_HOME=<path-to-android-sdk>
adb devices
```

Disable animations before running Espresso tests:
```bash
adb shell settings put global window_animation_scale 0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global animator_duration_scale 0.0
```
