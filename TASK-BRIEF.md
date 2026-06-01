# Task Brief — AntennaPod Mobile Testing

**To**: Members 2, 3, 4
**From**: Tianyu Yao (Team Lead)

## Your Assignment

| Member | Module | TC Range |
|--------|--------|-----------|
| **Member 2** | Subscription & Discovery | TC-011 ~ TC-020 |
| **Yuanbing Wang** | Playback & Downloads | TC-021 ~ TC-030 |
| **Member 4** | Settings & System | TC-031 ~ TC-040 |

Full details in `test-docs/test-case-plan.md`. Your TC list is **suggested** — you can adjust
individual TC titles or approaches after discussing with the team lead. Module scope and
method distribution should stay consistent.

## Before You Start

| # | File | Time |
|---|------|------|
| 1 | `CONTRIBUTING.md` | 5 min |
| 2 | `test-docs/test-case-plan.md` (your section) | 5 min |
| 3 | `AI-GUIDE.md` | 10 min |

## Workflow

```
Plan → Code → Compile → Run → Document → Commit → Push
```

### 1. Plan
- Read your TC assignments
- Understand which testing methods you need
- Read the reference examples

### 2. Code
- Create test files in the correct pre-existing directory
- Every class must have `@author Your English Name` in its KDoc
- `de.danoeh.antennapod.{espresso,uiautomator,integration,performance,utils}` for instrumented
- `de.danoeh.antennapod.{unit,manual}` for JVM tests

### 3. Compile
```bash
cd app-under-test/antennapod
./gradlew :app:compilePlayDebugAndroidTestSources   # instrumented
./gradlew :app:compilePlayDebugUnitTestSources       # unit
```

### 4. Run
```bash
./gradlew :app:connectedPlayDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=<full.package.ClassName>
```

### 5. Document
| File | What |
|------|------|
| `PROGRESS.md` | Update your TC status after each batch |
| `test-docs/test-cases.md` | Append your TC specs (copy format from existing) |
| `test-results/manual-test-result.md` | Add your rows, update Summary |
| `test-docs/test-summary-report.md` | Add key findings, update counters |

### 6. Commit
```bash
git pull
git add path/to/your/test.kt test-docs/test-cases.md
git commit -m "$(cat <<'EOF'
<type>: <short description>

Author: <Your English Name>
EOF
)"
```

### 7. Push
```bash
git push
```
Push triggers CI. If CI passes, your changes are live on main.

## Key Rules

- **Language**: English only
- **Never modify** `app/src/main/`
- **Do NOT create new directories**
- **Do NOT use `git add -A`**
- @author in every test class
- Screenshots: quality over quantity (see `screenshots/README.md`)
