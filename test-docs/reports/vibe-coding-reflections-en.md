# Vibe Coding Reflections: Lessons from an Android Testing Project's AI Collaboration

> **Author**: Tianyu Yao (FrozenYty)
> **Date**: 2026-06-17
> **Project**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
> **AI Partner**: Claude Code (Anthropic Claude Fable 5)

---

## 1. What Is Vibe Coding

"Vibe Coding" is not about letting AI write code for you. It is a **collaboration paradigm** — the human defines direction, constraints, and goals; the AI handles execution-level coding, debugging, and documentation synchronization. Human attention focuses on "doing the right thing," not "doing the thing right."

In this project, Claude Code and I collaborated to deliver 40 test cases across a 4-person team, a Frida dynamic call graph subsystem, and a complete project infrastructure (documentation, CI, coding standards). Total output exceeded 5,000 lines of Kotlin + 1,500 lines of Python/JS. The entire process crystallized into a replicable methodology.

This article summarizes that methodology across six dimensions: **Project Architecture**, **Memory System**, **Core Patterns**, **Case Study**, **The Human Role**, and **Boundaries & Limitations**.

---

## 2. Infrastructure: Project Architecture Designed for AI Collaboration

The first lesson of Vibe Coding is: **AI does not remember your project**. Every new session, the AI starts from zero. Without deliberate design, you fall into the cycle of "explaining what the project is about" every single time.

Our solution uses four interlocking Markdown files as the AI's "onboarding system":

### 2.1 Four-Layer Documentation Architecture

| File | Role | Who Reads It |
|------|------|-------------|
| `AI-PROMPT.md` | Copy-paste prompt template: one sentence describing "what I need you to do" | Human pastes to AI |
| `AI-GUIDE.md` | Complete collaboration manual: environment, code patterns, pitfall checklist, screenshot policy | AI reads it, then can immediately write code |
| `CONTRIBUTING.md` | Rule checklist: commit format, file organization, hard prohibitions | AI must comply before writing code |
| `PROGRESS.md` | AI's "only memory": current state, completed tasks, pending items, every pitfall encountered | Must-read at the start of every conversation |

**Key insight**: `PROGRESS.md` is the core of the entire system. Its design principles:

- **Real-time updates**: Write to it immediately after every compile, every test run, every fix
- **Written for AI, not humans**: Contains `frida --help` discoveries, Thread.sleep crash root causes, what to do next — context humans don't need to write down but is critical for AI
- **"When context is lost, this file is your only memory"** — the very first line at the top of our `PROGRESS.md`

```markdown
# PROGRESS.md — AI Session State

> Read this first. Update it in real-time — after every compile,
> every test run, every fix. When context is lost, this file is
> your only memory. Keep it current.
```

### 2.2 The "Claude Layer" Instruction System

Beyond project documentation, we established a three-tier instruction system:

```
~/.claude/CLAUDE.md          ← Global behavioral rules (language, output path, sub-agent strategy)
D:\MYPROJS\.claude\CLAUDE.md ← Workspace overrides (Conda environment, Bash paths)
Memory System (10 files)      ← Lessons AI learned from mistakes
```

- **Global CLAUDE.md** defines behavioral principles: think before coding, simplicity first, surgical changes, goal-driven execution, chat in Chinese but write in English
- **Workspace CLAUDE.md** provides technical constraints: Conda environment paths, Bash tool quirks
- **Memory System** (detailed in the next section) records recurring patterns from the project

These three layers form an "AI Constitution" — global principles take precedence, project constraints layer on top, and memory provides experience.

### 2.3 Team Collaboration by Design

This project has 4 people, each with 10 non-overlapping TCs. We structured tasks via `TASK-BRIEF.md` into:

```
Plan → Code → Compile → Run → Document → Commit → Push
```

Each member drops files into pre-created directories and updates their own section of `PROGRESS.md`. No branches, no PRs — since TC ranges don't overlap, pushing directly to main avoids merge conflicts entirely.

The key is **eliminating conflict possibility upfront** — not using Git tools to resolve conflicts, but using process design so conflicts never arise. This "design away the problem" mindset runs throughout Vibe Coding.

---

## 3. Memory System: The Meta-Cognitive Loop of Learning from Mistakes

The most distinctive innovation of Vibe Coding is the **Memory System**. It is not code comments or documentation — it is persistent rules encoded by the AI after reflecting on its own mistakes.

### 3.1 Memory Structure

Each memory contains three essential elements:

```yaml
name: stop-and-pivot
description: "When the same error keeps recurring, stop iterating — consult official docs and be ready to abandon the current approach"
metadata:
  type: feedback          # user | feedback | project | reference
---
Body: describe the problem, root cause, and correct approach.

Why: why this lesson matters
How to apply: concrete steps when encountering similar situations
Links: [[cli-help-first]]    # link to related memories
```

### 3.2 Ten Core Memories

Below are the ten memories accumulated during the AntennaPod project, organized by theme:

#### Cognitive Layer: How to Think

**[[constraints-first]]**
> When fixing multi-constraint problems, list ALL hard requirements before touching code. Symptom-by-symptom fixes ping-pong between breaking the other.

Human feedback arrives one symptom at a time ("the colors are wrong"), rarely naming the full constraint set. If you only fix what was just reported, you revert or break the constraint you fixed last round. **Before coding, ask yourself**: this round's requirements + previous round's requirements + hard limits = all constraints?

**[[stop-and-pivot]]**
> When the same error persists after 3+ attempts — stop. The problem is not the implementation details; the approach itself is wrong.

Frida Python API tried 5 times, all returning `Java is not defined`. Thirty seconds of `frida --help` revealed the `-q -t` non-interactive mode. **Lesson**: repeated failure is not insufficient effort — it's the wrong direction.

**[[verify-before-build]]**
> When depending on complex external systems, verify each assumption with minimal test scripts before building the full system.

We wrote 7 progressive test scripts (`test.js` → `test7.js`) in the Frida project, each verifying one assumption: Is Java bridge available? Can methods be hooked? Does setTimeout fire in `-q` mode? Can delayed enumeration find classes? — Unfortunately, all of these came after writing 1000+ lines of orchestrator code. **Lesson**: the correct order is verify → build, not build → verify.

#### Action Layer: How to Do Things

**[[cli-help-first]]**
> When integrating CLI tools, check `--help` before online docs. `--help` is faster, version-accurate, and guaranteed to match the installed version.

After hours debugging Frida Python API and subprocess approaches, we discovered `-q` (quiet mode) and `-t <N>` (auto-exit timeout) — both plainly listed in `frida --help`.

**[[api-drift-check]]**
> When tutorial/courseware API doesn't match the installed library version, verify before coding. Major version bumps frequently break method signatures.

Androguard's `get_vm_method()` became `get_method()` in 4.x, while the tutorial used the 3.x API. One `dir()` probe saves hours of debugging.

**[[migration-checklist-first]]**
> When upgrading any tool across a major version, query the full migration guide first and fix all breaking changes in one pass.

AGP 8→9 had 9 distinct breaking changes — fixing them one at a time wasted 9 CI rounds when one migration-guide read would have sufficed.

**[[local-test-before-push]]**
> After build configuration changes, run the project's test suite locally before pushing. One local run replaces N CI rounds.

A local test run takes 30 seconds and shows every error at once. Pushing to CI exposes one error per round — a push → wait → fix → push loop.

**[[bash-not-powershell]]**
> Claude Code's Bash tool runs on Git Bash (MinGW), not PowerShell. Windows backslash paths get mangled as escape characters.

`D:\anaconda3\envs` becomes `D:anaconda3envs` — `\a` is the bell character, `\e` is the escape character. Always use forward slashes: `D:/anaconda3/envs`. This is an environment-level pitfall, not a code-level one.

#### Review Layer: How to Find Errors

**[[silent-errors-are-dangerous]]**
> Bugs that don't crash — string truthiness, falsy-zero, type coercion — survive "it runs" checks and produce wrong results silently.

Python's `if "false":` is truthy, `if count:` skips the valid value `0`. These bugs don't crash, don't error, and produce output that looks "almost right." Only structured code review catches them.

**[[review-by-asset-type]]**
> Parallel review agents partition by asset type (code/docs/assets), not by review dimension. Different types surface disjoint issues.

Code reviewers catch naming violations and import issues; doc reviewers catch stale statuses and cross-file inconsistencies; asset reviewers catch orphan files and broken references. The three found 16 issues with near-zero overlap — dimension-based review would have plenty of duplication.

### 3.3 Memory Evolution Path

Every memory goes through a standard "evolution" process:

```
Make a mistake → Fix manually → Recognize the pattern → Write memory → Link to other memories
→ Promote to project rule → (eventually) Merge into CLAUDE.md
```

For example, `[[stop-and-pivot]]` and `[[cli-help-first]]` are two expressions of the same Frida debugging experience — the former is the cognitive strategy (when to stop), the latter is the action strategy (what to check after stopping). They link to each other, forming a knowledge network.

**The essence of the Memory System**: it turns the AI's "now I know" into "next time I'll also know." Normal AI conversations are amnesiac — every new session is blank. The Memory System lets AI genuinely learn from experience.

---

## 4. Core Patterns: Five Validated Collaboration Principles

From the memory system, I distill five essential Vibe Coding principles:

### Principle 1: Constraints Before Code

Bad Vibe Coding: start writing code as soon as you receive requirements. Good Vibe Coding: list all constraints with the AI first, reach consensus, then act.

In the Frida project, constraints included:
- Cannot modify app source code (hook only) ✓
- Must work in a real Android runtime ✓
- Must auto-discover classes and methods (no hardcoding) ✓
- Results must be visualized in the browser ✓
- Frida version 17.10.1 already installed ✓

Once these were written down clearly, the approach choice became natural — Python API fails constraint 2 (Java bridge unavailable), CLI subprocess satisfies all.

**Action guide**: Before giving AI instructions, describe three things: **(1) What is the input (2) What is the output (3) What must absolutely not be done**. These three sentences are more useful than 500 words of description.

### Principle 2: Incremental Verify, Not Big Bang

Bad Vibe Coding: have AI write the entire module in one shot, then debug. Good Vibe Coding: break into minimal verifiable units, verify each independently, then proceed.

Frida project's 7 progressive test scripts:

| Script | Verified Assumption | Result |
|--------|-------------------|--------|
| test.js | Java bridge available under CLI? | ✅ |
| test2.js | Can hook MainActivity methods? | ✅ |
| test3.js | `setTimeout` fires in `-q -t` mode? | ✅ |
| test4.js | `enumerateLoadedClasses` callback fires? | ✅ (but 0 AntennaPod classes at t=0) |
| test5.js | Delayed enumeration finds AntennaPod classes? | ✅ (451 classes at t=3s) |
| test6.js | `Java.perform()` works inside `setTimeout`? | ✅ |
| test7.js | SleepTimerType enum alone doesn't crash? | ✅ |

Each test ≤30 lines of JS, verifying one assumption. The full orchestrator came only after all passed.

**Action guide**: When receiving a complex task, first ask the AI to list the assumptions it needs to verify, then verify each with a minimal script. Don't write "real code" until all assumptions are verified.

### Principle 3: Stop at Third Failure

This is the core of `[[stop-and-pivot]]`. The third failure in the same direction is not a "almost there" signal — it's a "wrong road" signal.

Frida Python API attempt log:

| Attempt | Method | Result |
|---------|--------|--------|
| 1 | `device.attach(pid)` + `create_script()` | `Java is not defined` |
| 2 | `device.attach('AntennaPod Debug')` | Same |
| 3 | `device.spawn([package])` | Same |
| 4 | `runtime='v8'` | Same |
| 5 | Manual bridge protocol loading | `Java` still undefined |

By attempt 5 we finally discovered `Java` is auto-injected by Frida's C core; the Python API doesn't do this. Should have stopped at attempt 3.

**Action guide**: Set a hard rule — same approach fails 3 times, the 4th must be a fundamentally different approach. Write this into CLAUDE.md.

### Principle 4: State File is AI's Hard Drive

The AI's context window is "RAM" — volatile, limited, cleared with each new conversation. `PROGRESS.md` is "hard drive" — persistent, appendable, surviving across sessions.

A good `PROGRESS.md` contains:

```
- Right Now: current state (device, version, last update time)
- Done: completed tasks (with checkboxes)
- Blockers & Decisions: every pitfall and its solution
- Lessons Learned: core lessons from this session
- Quick Resume: fast recovery steps for next session
- Command Cheatsheet: frequently used commands (saves re-deriving each time)
```

**Key practice**: Don't "record after finishing" — record as you go. Update after every compile, every fix, every pivot. An outdated PROGRESS.md is more dangerous than no PROGRESS.md — it will mislead the next AI session.

### Principle 5: Human Decides, AI Executes

This seems simple but is frequently broken in practice. AI tends to "make decisions on its own" — choose an API, skip verification steps, write an implementation that "looks right" but is actually wrong.

In our project, the human role is:

- **Architecture decider**: Python API or CLI subprocess? → Human decides
- **Quality gatekeeper**: 19 of these 24 screenshots are duplicates → Human judges
- **Domain expert**: `SortOrder` is an enum, not a class; `feed_id` is not `id` → Human knows
- **Constraint setter**: Don't modify main source, don't use `git add -A`, must use English → Human sets

AI's role is:

- **Executor**: Write code following constraints, run tests, update docs
- **Pattern matcher**: Extract templates from existing tests and apply them
- **Documentation synchronizer**: Keep code, PROGRESS.md, and test-cases.md consistent
- **Error debugger**: Compile fail → fix → retry loop

**Key insight**: The most effective Vibe Coding is not "AI does everything" — it's "human makes all decisions, AI does all execution." When this boundary blurs — AI makes decisions for the human, or the human gets mired in execution details — efficiency starts to decline.

---

## 5. Frida Case Study: A Complete Vibe Coding Anatomy

The Frida dynamic call graph subsystem is a complete Vibe Coding specimen — from requirement to delivery across 3 days, 7 pivots, dozens of failures and fixes. Here is the full timeline analysis.

### 5.1 Requirements & Architecture Design (Day 1)

**User intent**: "Based on frida-trace, implement code to build a method call graph" + "make results previewable in the browser"

**Architecture decisions**:
- Two components: `frida_hooks.js` (JS payload injected into Android runtime) + `frida_callgraph.py` (Python orchestrator)
- Dynamic hook strategy: `Java.enumerateLoadedClasses()` + `getDeclaredMethods()`
- Caller identification: `Thread.currentThread().getStackTrace()` (later proven wrong)
- Output: matplotlib charts + interactive HTML (vis-network + Chart.js)

### 5.2 Python API Dead End (Day 1–2)

**Problem**: `frida.get_usb_device().attach(pid).create_script(js)` never makes `Java` available in the script runtime.

**5 failed attempts** (see Principle 3 table).

**Root cause** (discovered via `frida --help`): The `Java` global is created by Frida's C core when it detects an ART/Dalvik process. The CLI tool `frida -U` triggers this detection; the Python API's raw `create_script()` does not.

**Pivot**: Abandoned Python API, switched to CLI subprocess approach (`frida -U -f <pkg> -l script.js -q -t <N>`).

**Vibe coding lesson**: **APIs are not equivalent**. The Python binding and CLI tool of the same product do not provide identical runtime environments. Never assume consistent behavior — verify with CLI first.

### 5.3 Buffering Hell (Day 2)

**New problem**: Pipe buffering. `proc.stdout.readline()` blocks because frida's C stdout uses full buffering when writing to a pipe.

**Fix attempts**:
1. Threaded non-blocking read — frida never produces enough output to trigger a flush
2. `stdbuf -oL` — doesn't work on Windows
3. **Final fix**: `frida -o logfile.txt` to write directly to file, read after exit

**Vibe coding lesson**: **Subprocess pipes are traps**. Cross-language (Python ↔ C ↔ JS) stdout buffering behavior is unpredictable. File I/O is dumber but more reliable.

### 5.4 Native Crashes & Incremental Diagnosis (Day 2–3)

**Problem**: `Java.enumerateLoadedClasses()` freezes on certain classes — not a JS exception, but a C/native-level crash, completely invisible to JS try-catch.

**Diagnostic process** (`test.js` → `test7.js` progressive verification):

| Discovery | Script |
|-----------|--------|
| Java bridge works under CLI | test.js |
| Method hooking works | test2.js |
| `setTimeout` fires in `-q -t` mode | test3.js |
| `enumerateLoadedClasses` callback fires | test4.js |
| 0 AntennaPod classes loaded at t=0 | test4.js |
| 451 AntennaPod classes available at t=3s | test5.js |
| Hook script must delay scanning | test5.js insight |
| `Java.perform()` works inside `setTimeout` | test6.js |
| SleepTimerType enum alone doesn't crash | test7.js |

**Safety measures implemented**:
- `attempted >= 25` hard limit (prevents freezing on problematic classes)
- `isEnum()` / `isInterface()` checks (skip problematic types)
- `$$ExternalSynthetic` / `BuildConfig` / `Manifest$` pattern filtering
- `MAX_PER_CLASS = 15` (limit methods per class to avoid overload)

**Vibe coding lesson**: **Native crashes are silent**. JS try-catch cannot catch C-level crashes. The only diagnostic method is comparing working vs. non-working versions with progressively tighter limits.

### 5.5 Thread.sleep Crash (Day 3)

**Problem**: `Java.use('java.lang.Thread')` crashes with `TypeError: Cannot read properties of null (reading 'sleep')`.

**Root cause**: Frida 17.10.1 bug on Android 12.

**Fix**: Use `Java.use('java.lang.Exception')` + `new Exception().getStackTrace()` instead. This is the standard Android pattern and completely avoids `java.lang.Thread`.

**Vibe coding lesson**: **Tool bugs are real**. When a core API call crashes, don't assume your code is wrong — search for known issues with that tool, then find an alternative.

### 5.6 Visualization & Data Richness (Day 3)

**User feedback**: "Why is this graph so simple?" → Changed from class-level to method-level network graph (9 nodes → 21 nodes)
**User feedback**: "Stats Charts are blank" → Chart.js initialized in hidden panel with zero canvas size; fixed with lazy init on tab switch

**Final deliverables**:

| File | Description |
|------|------------|
| `frida_hooks_cli.js` | CLI-compatible JS hook (Exception-based stack trace) |
| `frida_callgraph.py` | Python orchestrator (CLI subprocess + ADB interaction + HTML + matplotlib) |
| `frida-callgraph.html` | Interactive browser report (vis-network + Chart.js + edge table) |
| `frida-callgraph-stats.{png,pdf}` | Top callers/callees bar charts |
| `frida-callgraph-edges.{png,pdf}` | Top call edges ranking |
| `frida-callgraph-heatmap.{png,pdf}` | Package-level call density heatmap |
| `frida-callgraph.json` | Structured JSON export |

**Trace results**: 17 classes, 93 methods hooked, 14 unique call edges, 80 total method calls.

### 5.7 Frida Case Vibe Coding Summary

Vibe Coding characteristics of the entire Frida sub-project:

1. **Human sets direction, AI does execution**: Human defined "build call graph" and "browser visualization"; AI designed architecture, wrote code, debugged
2. **Human sets constraints, AI explores within them**: Can't modify app source, must use Frida 17.10.1, must auto-discover
3. **Human identifies pivot timing**: "If you keep making mistakes, stop and think first" — this is human judgment
4. **AI records experience**: The Frida case directly spawned 4 memories (`[[stop-and-pivot]]`, `[[cli-help-first]]`, `[[verify-before-build]]`, `[[api-drift-check]]`)
5. **Human stays in the feedback loop**: Every deliverable was reviewed by human ("why is the graph so simple," "charts are blank"), then AI iterated

---

## 6. The Human Role: Irreplaceability in AI Collaboration

The central paradox of Vibe Coding is: **the more powerful AI becomes, the more critical human judgment becomes**. Because AI can produce more code faster, but it also amplifies errors faster.

### 6.1 Five Things Humans Must Do

**(1) Define "Done"**
AI tends to stop when it "looks done." Humans must define verifiable completion criteria:
- "Write a test" → vague
- "TC-011 passes 4/4 test cases, runs on MuMu emulator, screenshot saved, PROGRESS.md updated" → verifiable

**(2) Recognize Patterns**
AI sees individual errors. Humans see "this is the third time the Python API has failed — maybe the approach itself is wrong." Pattern recognition → pivot decision — this is a uniquely human capability.

**(3) Set Constraints**
AI tends toward "if it can be done, do it." Humans must say "what must not be done":
- Don't modify `app/src/main/`
- Don't use `git add -A`
- Screenshot quality > quantity
- Must be in English

**(4) Review Output**
AI output "looking right" ≠ being right. In our project:
- 19 of 24 initial screenshots were duplicates → human caught this
- `SortOrder` was treated by AI as a constructable class → human corrected (it's an enum)
- `feed_id` was treated as `id` by AI → human corrected (cursor column names differ)

**(5) Maintain Memory**
AI won't proactively say "I should remember this lesson." Humans must identify which lessons are worth preserving and trigger memory writes.

### 6.2 Three Things Humans Should Not Do

**(1) Don't get mired in execution details**
If AI's code has compilation errors, let AI fix them itself. Humans don't need to understand every line — they need to ensure tests pass, docs are updated, and logic is correct.

**(2) Don't skip documentation updates**
"Code first, documentation later" = documentation never. In Vibe Coding, documentation is not a supplement — it is the AI's sole context for the next session.

**(3) Don't make decisions for the AI**
When you tell AI "try X," you're making the decision the AI should be making itself. A better prompt is: "The current approach failed. Propose 3 alternative approaches and compare them."

---

## 7. Boundaries of Vibe Coding: When It Doesn't Fit

Based on this project's practice, Vibe Coding works best in these scenarios:

| Suitable | Less Suitable |
|----------|--------------|
| Structured tasks (test cases, CRUD, scripts) | Open-ended exploration ("research this for me") |
| Work with clear constraints | Work where constraints themselves are unknown |
| Incrementally verifiable tasks | Tasks requiring deep context understanding to judge correctness |
| Work with existing patterns to replicate | Architecture creation from absolute scratch |
| Multi-person collaboration needing doc sync | Solo rapid prototyping |

### Key Boundary Conditions

1. **Vibe Coding needs "the right first step"**: If AI's initial approach direction is wrong, all subsequent iterations are on the wrong track. This is why "constraints before code" and "incremental verification" are so important.

2. **Vibe Coding has extreme dependency on documentation**: Without PROGRESS.md, AI-GUIDE.md, and the memory system, Vibe Coding degenerates into "re-explain every time" — essentially manual coding with autocomplete.

3. **Vibe Coding cannot replace domain knowledge**: If the human doesn't understand the Android testing framework, doesn't understand how Frida works, doesn't understand the Gradle build system — Vibe Coding will produce large amounts of "looks right but is wrong" code. Human domain knowledge is the quality floor.

---

## 8. Conclusion: Vibe Coding as an Engineering Discipline

After three months of practice on the AntennaPod project, my understanding of Vibe Coding evolved from "let AI write code" to "design a system where AI can reliably write code."

This system includes:

- **Project architecture**: AI-PROMPT.md → AI-GUIDE.md → CONTRIBUTING.md → PROGRESS.md four-layer onboarding documentation
- **Memory system**: 10 lessons learned, structurally encoded, interlinked, continuously evolving
- **Workflow**: Plan → Code → Compile → Run → Document → Commit → Push complete cycle
- **Feedback loop**: Every error → understand root cause → encode as memory → promote to rule → merge into CLAUDE.md

Vibe Coding is not magic. It is an **engineering discipline** — one that requires you to think more clearly than traditional coding about "what am I doing," "what are my constraints," and "what counts as done." AI makes your hands faster, but it does not make your mind clearer.

**The single most important piece of advice**: If you take away only one thing from this reflection, take this — **before starting Vibe Coding, spend 30 minutes writing PROGRESS.md and AI-GUIDE.md**. That 30-minute investment pays off in every subsequent AI session.

---

## Appendix: Project Key Metrics

| Metric | Data |
|--------|------|
| Project duration | 2026-05-28 ~ 2026-06-07 (approximately 10 days) |
| Team size | 4 people (1 Lead + 3 Members) |
| Total test cases | 40 (TC-001 ~ TC-040) |
| Testing method types | 7 types (Espresso, UIAutomator, Unit, Integration, Manual, Performance, Static Analysis) |
| Automated test pass rate | 100% (all Espresso/UIAutomator/Unit/Integration/Performance TCs) |
| Frida call graph coverage | 17 classes, 93 methods hooked, 14 unique call edges |
| Memory accumulation | 10 structured memories |
| CI/CD | Per-push compile + unit test + doc check |
| Total code volume | ~5,000 lines Kotlin + ~1,500 lines Python/JS + ~20 documents |