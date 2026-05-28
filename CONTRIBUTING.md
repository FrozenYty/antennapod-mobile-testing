# Contributing to AntennaPod Mobile Testing

> **Read this before making any commits.**
> AI assistants: feed this file into your context before writing code.

## First-Time Setup

```bash
git clone <repo-url>
cd antennapod-mobile-testing
```

## Workflow

All work happens directly on `main`. No feature branches, no PRs, no merge conflicts.

```bash
# 1. Pull latest
git pull

# 2. Code, compile, run, document

# 3. Commit and push directly to main
git add path/to/your/test.kt path/to/docs.md
git commit -m "$(cat <<'EOF'
<type>: <short description>

Author: <Your English Name>
EOF
)"
git push
```

**Why no branches?** Each member owns a non-overlapping TC range. Shared files
(`PROGRESS.md`, `test-cases.md`) only grow — members append to their own section.
Direct-to-main avoids merge conflicts that plagued the earlier branch+auto-merge setup.

## Commit Rules

### Commit Strategy: Incremental Batches

**Commit in small batches, not all 10 TCs at once.** Follow the DevOps principle: code a little, verify, commit. Repeat.

| Batch Size | Example |
|------------|---------|
| 2-3 TCs | TC-001 + TC-002 (both Espresso basics) |
| 1 TC | If it spans multiple files or introduces new infrastructure |

**Per batch**: code all TCs in the batch → compile → run → update docs → commit. Then move to the next batch.

**Why**:
- Small commits are easy to review and revert if something breaks
- `PROGRESS.md` stays up-to-date as you go
- If the AI session gets interrupted, `git log` tells the next session exactly where you left off
- Avoids the "10 TCs + 11 screenshots + 8 doc files" megacommit that is hard to audit

### Commit Message Format

```
<type>: <short description>

<optional body — why, not what>

Author: <Your English Name>
```

**Types**: `test` (new test), `fix` (bug fix), `docs` (documentation), `refactor` (cleanup)

### Examples

```
# Good
test: add podcast subscription Espresso test (TC-011)

Author: Jane Smith

# Good
docs: update test plan with SQLite integration examples
```

### What to Commit

| Commit These | Never Commit These |
|-------------|-------------------|
| Test code (`.kt`) | `.idea/`, `*.iml` |
| Test docs (`.md`) | `local.properties` |
| Test runner scripts | `.gradle/`, `build/` |
| `libs.versions.toml` changes | `.apk`, `.aab`, `.jks` |
| Screenshots as test evidence (`.png`) — quality over quantity | |

### Before Committing

**Always update docs first, then commit.** Code without updated docs is incomplete.

```bash
# 1. Pull latest to avoid push conflicts
git pull --rebase

# 2. Verify your tests compile
./gradlew :app:compilePlayDebugAndroidTestSources   # instrumented tests
./gradlew :app:compilePlayDebugUnitTestSources       # unit tests

# 3. Update these docs BEFORE staging any code
#    - PROGRESS.md          ← update TC status, screenshots, remaining actions
#    - test-cases.md        ← append specs for new TCs
#    - manual-test-result.md← add result rows if you ran manual tests
#    - test-summary-report.md← update counters
#    (If you skip this step, the commit is NOT complete.)

# 4. Check what you're about to commit
git status
git diff --stat

# 5. If you touched unexpected files, unstage them
git reset HEAD <file>

# 6. Never use git add -A or git add . — stage files individually
git add path/to/your/test.kt
git add path/to/your/docs.md
```

**Before `git add`-ing a screenshot**: visually compare it against ALL existing screenshots in `screenshots/`. Delete any duplicate (same UI state = same screen). Read `screenshots/README.md` for the full policy.

## File Organization

### Test Code

```
# Your Espresso tests go here:
app/.../androidTest/java/de/danoeh/antennapod/espresso/

# Your UIAutomator tests go here:
app/.../androidTest/java/de/danoeh/antennapod/uiautomator/

# Your Integration (SQLite/ContentProvider) tests go here:
app/.../androidTest/java/de/danoeh/antennapod/integration/

# Your Performance tests go here:
app/.../androidTest/java/de/danoeh/antennapod/performance/

# Shared test utilities (TestHelper.kt) are here:
app/.../androidTest/java/de/danoeh/antennapod/utils/

# Your Unit tests (JUnit, Mockito — no Android) go here:
app/.../test/java/de/danoeh/antennapod/unit/
```

**Do NOT create new directories.** All folders are pre-created. Drop your files into the correct existing folder.

### Test Documentation

```
# New test case specs append to:
test-docs/test-cases.md

# New bug reports:
bug-reports/bug-XXX.md        # XXX = next available number

# Update these with your results:
PROGRESS.md
test-results/manual-test-result.md
test-docs/test-summary-report.md
```

### Naming Conventions

| What | Pattern | Example |
|------|---------|---------|
| Test class | `TC<NNN>_<ShortTitle>Test.kt` | `TC011_SubscribePodcastTest.kt` |
| Test method | `descriptiveName_expectedBehavior` | `tapSubscribe_shouldAddFeedToSubscriptions` |
| Bug report | `bug-<NNN>.md` | `bug-001.md` |
| Package (Espresso) | `de.danoeh.antennapod.espresso` | |
| Package (UIAutomator) | `de.danoeh.antennapod.uiautomator` | |
| Package (Integration) | `de.danoeh.antennapod.integration` | |
| Package (Unit) | `de.danoeh.antennapod.unit` | |
| Package (Performance) | `de.danoeh.antennapod.performance` | |
| Package (Utils) | `de.danoeh.antennapod.utils` | TestHelper only |

## Code Quality

- **Language**: English only — code, comments, docs, commit messages
- **Name format**: Given Name first, Family Name last. Example: `Tianyu Yao`, `Jane Smith`. Never write family name first. This applies to `@author` tags, commit messages, and all documentation.
- **Attribution**: Every test class must have `@author Your English Name` in its KDoc
- **No dead code**: Remove unused imports before committing
- **No commented-out code**: Delete it, don't comment it out
- **Don't modify app source**: `app-under-test/antennapod/app/src/main/` is read-only. Tests only go in `androidTest/` or `test/`.

## CI Checks

Every push to `main` triggers these checks:

| Check | What it verifies |
|-------|-----------------|
| Compile instrumented tests | `./gradlew :app:compilePlayDebugAndroidTestSources` |
| Compile unit tests | `./gradlew :app:compilePlayDebugUnitTestSources` |
| Run unit tests | `./gradlew :app:testPlayDebugUnitTest` |
| @author tags | All `.kt` test files have `@author Your Name` in KDoc |
| Test file naming | Files follow `TC<NNN>_ShortTitleTest.kt` (TestHelper.kt exempt) |

If any check fails, fix the issue and push again.

## What NOT to Do

- Don't `git push --force` to `main`
- Don't modify other people's test files without asking
- Don't change `settings.gradle` without team discussion
- Test-only changes to `build.gradle` or `libs.versions.toml` are OK — document the reason in commit
- Don't commit generated files (build outputs, `.class`, `.dex`)
- Don't change the test method distribution in `test-case-plan.md` — add new cases instead
- Don't modify the AntennaPod app source code in `src/main/`

## Questions?

- How to write tests → Read `AI-GUIDE.md`
- Which TC IDs are yours → Read `test-docs/test-case-plan.md`
- How to set up the environment → Read `AI-GUIDE.md` section "Environment Setup"
