# Vibe Coding Report — Frida Dynamic Call Graph

> **Project**: AntennaPod Mobile Testing
> **Feature**: Frida-based runtime method call graph
> **Date**: 2026-06-05 ~ 2026-06-07
> **Author**: Tianyu Yao (Lead), with Claude Code (AI assistant)

---

## 1. User Prompts & Intent

| # | User Prompt | Intent |
|---|------------|--------|
| 1 | "基于 frida-trace，实现代码，构建方法的调用图（use context7）" | Build a dynamic call graph using Frida runtime tracing |
| 2 | "可以让结果在浏览器中预览" | Add interactive browser-based visualization |
| 3 | "你帮我完成全部的任务" | Execute the full pipeline (trace + visualize) |
| 4 | "一直犯错的话可以先停下来思考一下解决方案" | Stop blind iteration; consult official docs |
| 5 | "先暂停一下，做一个 frida 调试的日志文件" | Document debug state before continuing |
| 6 | "可以写在 progress 里" | Integrate debug log into PROGRESS.md |
| 7 | "我需要一份 vibe coding 的报告" | Retrospective document of the collaboration |
| 8 | "经常犯同一个错记得要跳出原有的思路，查询文件（use context7 or github）" | Use official docs before repeated attempts |
| 9 | "你忘记具体内容的话，可以看我们的文档" | Reference project docs (CLAUDE.md, PROGRESS.md, etc.) |
| 10 | "你只能操作我们测试的应用" | Only interact with AntennaPod, not system UI |
| 11 | "全部都是英文填写的吗，跟前面的规范要一样" | Ensure all outputs follow project English-only convention |
| 12 | "用我们的 brain-admin skill 整理完善一下我们的 memory" | Standardize memory file formats |
| 13 | "为什么打开是空白的" | Debug blank HTML report in browser |
| 14 | "这个图为什么这么简单，而且 Stats Charts 是空白" | Improve graph complexity and fix chart rendering |
| 15 | "还能做的更复杂且完善吗" | Maximize call graph richness |

---

## 2. Execution Timeline

### Phase 1: Architecture Design (2026-06-05)

**User prompt**: "基于 frida-trace，实现代码，构建方法的调用图"

**What I did**:
1. Read project documentation (README, AI-GUIDE, PROGRESS, TASK-BRIEF, test docs)
2. Explored existing static call graph code (`automation/callgraph.py` — Androguard-based)
3. Searched Frida documentation via Context7 (`/frida/frida-website`, `/websites/frida_re`)
4. Designed a two-component architecture:
   - `frida_hooks.js` — JavaScript payload injected into Android runtime
   - `frida_callgraph.py` — Python orchestrator (device management, data collection, visualization)

**Key design decisions**:
- Use `Java.enumerateLoadedClasses()` + `getDeclaredMethods()` for dynamic hooking
- Use `Thread.currentThread().getStackTrace()` for caller identification
- Use `send()` / `recv()` for JS ↔ Python communication
- Generate matplotlib charts + interactive HTML with vis-network & Chart.js

### Phase 2: The Python API Dead End (2026-06-05 ~ 2026-06-06)

**Problem**: `frida.get_usb_device().attach(pid).create_script(js)` never makes `Java` available.

**What I tried** (5+ iterations, all failed):
| Attempt | Method | Result |
|---------|--------|--------|
| 1 | `device.attach(pid)` + `create_script()` | `ReferenceError: Java is not defined` |
| 2 | `device.attach('AntennaPod Debug')` | Same error |
| 3 | `device.spawn([package])` + `create_script()` | Same error |
| 4 | `runtime='v8'` parameter | Same error |
| 5 | Manual bridge loading (`frida:load-bridge` → `frida:bridge-loaded` protocol) | Bridge eval'd but `Java` still undefined |

**Root cause discovered** (via `frida --help`): The `Java` global is created by Frida's C core, not by `java.js`. The CLI tool auto-injects it; the Python API's raw `create_script()` does not.

**User feedback**: "一直犯错的话可以先停下来思考一下解决方案，不要盲目尝试执行（use context7 or github）"

**Pivot**: Abandoned Python API. Switched to CLI subprocess approach.

### Phase 3: CLI Subprocess + Buffering Hell (2026-06-06)

**Solution**: Use `frida -U -f <pkg> -l script.js -q -t <N>` as subprocess, parse stdout JSON.

**Key discovery** (from `frida --help`):
- `-q` = quiet mode (no REPL prompt)
- `-t N` = keep running N seconds then auto-exit
- These flags together enable **non-interactive** frida execution

**New problem**: Pipe buffering. `proc.stdout.readline()` blocks because frida's C stdout uses full buffering when writing to a pipe.

**Fix attempts**:
1. Threaded non-blocking read — frida never produces enough output to flush
2. `stdbuf -oL` — didn't work on Windows
3. **Final fix**: `frida -o logfile.txt` to write output to a file directly, read after exit

### Phase 4: Native Crashes During Enumeration (2026-06-06 ~ 2026-06-07)

**Problem**: `Java.enumerateLoadedClasses()` freezes when `onMatch` calls `hookClass()` on too many classes.

**Root cause**: Some AntennaPod classes (enums, interfaces, D8/R8-generated `$$ExternalSynthetic` lambdas) cause Frida's Java bridge to crash at the C/native level. This crash is NOT catchable by JavaScript try-catch.

**Diagnostic process** (incremental test files: `frida_test.js` through `frida_test7.js`):
1. `test.js` — Verify Java bridge works via CLI ✅
2. `test2.js` — Verify method hooking on MainActivity ✅  
3. `test3.js` — Verify `setTimeout` works in `-q -t` mode ✅
4. `test4.js` — Verify `Java.enumerateLoadedClasses().onComplete` fires ✅ (20K classes, 0 AntennaPod at t=0)
5. `test5.js` — Verify DELAYED enumeration finds AntennaPod classes ✅ (451 classes at t=3s)
6. `test6.js` — Verify `Java.perform()` inside `setTimeout` works ✅
7. `test7.js` — Verify `SleepTimerType` enum doesn't crash alone ✅

**Key insight from test5.js**: At spawn time (t=0), 0 AntennaPod classes are loaded. After 3-4 seconds, 451 classes are available. The hook script must delay its scan.

**Safety measures implemented**:
- `attempted >= 25` hard limit in `onMatch` (prevents freezing on problematic classes beyond index 25)
- `isEnum()` / `isInterface()` checks (skip problematic types)
- `$$ExternalSynthetic` / `BuildConfig` / `Manifest$` pattern filtering
- `MAX_PER_CLASS = 15` (limit methods per class to avoid overload)

### Phase 5: Thread.sleep Crash (2026-06-07)

**Problem**: `Java.use('java.lang.Thread')` crashes with `TypeError: Cannot read properties of null (reading 'sleep')`.

**Root cause**: Frida 17.10.1 bug on Android 12 — `Thread.currentThread().getStackTrace()` triggers native crash.

**Fix**: Use `Java.use('java.lang.Exception')` + `new Exception().getStackTrace()` instead. This is the standard Android pattern and avoids `java.lang.Thread` entirely.

### Phase 6: Visualization & Browser Preview (2026-06-07)

**User prompt**: "可以让结果在浏览器中预览"

**Implementation**:
1. Generate self-contained HTML with embedded JSON data
2. vis-network (interactive graph) + Chart.js (bar charts) + sortable edge table
3. Auto-open in browser via `webbrowser.open()`

**Problems encountered**:
- `file://` protocol can't load CDN scripts → local HTTP server (port 8765)
- `vis-network` CDN URL 404 → switched to `vis.js 4.21.0` on cdnjs
- Chart.js initialized in hidden panel (zero canvas size) → lazy init on tab switch
- Missing `vis.min.css` → added

### Phase 7: Data Richness (2026-06-07)

**User feedback**: "这个图为什么这么简单" / "还能做的更复杂且完善吗"

**Improvements**:
- Changed from class-level to **method-level** network graph (21 nodes vs 9)
- Increased hook coverage from 20 classes to 25 (15 classes → 17 classes)
- Added ADB-simulated interaction (bottom nav taps, swipes) during trace
- Generated all output formats: HTML, JSON, PNG, PDF

---

## 3. Pivot Points (Critical Decision Changes)

| # | Original Approach | Why Abandoned | New Approach |
|---|------------------|---------------|-------------|
| 1 | Python `frida` API (`session.create_script`) | Java bridge never available | CLI subprocess (`frida -q -t -o`) |
| 2 | `Thread.currentThread().getStackTrace()` | Native crash in Frida 17.10.1 | `Exception.getStackTrace()` |
| 3 | Pipe stdout from subprocess | Full buffering, no real-time output | `frida -o logfile.txt` |
| 4 | Hook ALL AntennaPod classes | Native freeze in `enumerateLoadedClasses` | Safety limit `attempted >= 25` + enum/interface filtering |
| 5 | Immediate enumeration at spawn | 0 classes loaded at t=0 | `setTimeout(scan, 4000)` delayed scan |
| 6 | class-level network graph | Too sparse (9 nodes) | method-level graph (21 nodes) |
| 7 | `file://` HTML | CDN blocked | local HTTP server |

---

## 4. Final Deliverables

| File | Type | Description |
|------|------|-------------|
| `automation/frida_hooks_cli.js` | Code | Frida JS hook payload (CLI-compatible, Exception-based stack trace) |
| `automation/frida_callgraph.py` | Code | Python orchestrator (CLI subprocess + ADB interaction + HTML + matplotlib) |
| `test-docs/callgraphs/frida-callgraph.html` | Report | Interactive browser report (vis-network + Chart.js + edge table) |
| `test-docs/callgraphs/frida-callgraph-stats.{png,pdf}` | Chart | Top callers/callees bar charts |
| `test-docs/callgraphs/frida-callgraph-edges.{png,pdf}` | Chart | Top call edges ranking |
| `test-docs/callgraphs/frida-callgraph-heatmap.{png,pdf}` | Chart | Package-level call density heatmap |
| `test-docs/callgraphs/frida-callgraph.json` | Data | Structured JSON export |
| `automation/README.md` | Doc | Updated with Frida section |
| `automation/requirements.txt` | Config | Added `frida-tools>=12.0` |
| `PROGRESS.md` | Doc | Complete debug journal |
| `memory/stop-and-pivot.md` | Memory | Lesson learned: stop iterating, consult docs |

### Trace Results

| Metric | Value |
|--------|-------|
| Classes hooked | 17 |
| Methods hooked | 93 |
| Unique caller→callee edges | 14 |
| Total method calls captured | 80 |
| Network graph nodes (method-level) | 21 |
| Network graph edges | 14 |

### Call Graph Topology (Top Edges)

```
MainActivity.updateMainBackCallbackEnabledState ──→ UserPreferences.getDefaultPage (×19)
EpisodeItemListAdapter.updateItems ──→ SelectableAdapter.onSelectedItemsUpdated (×10)
SubscriptionFragment.lambda$... ──→ UserPreferences.getFeedOrder (×5)
QueueFragment.onCreateView ──→ SelectableAdapter.setOnSelectModeListener (×5)
```

---

## 5. Lessons Learned

1. **CLI `--help` is often better than documentation.** The `-q -t` flags that saved the project were discovered via `frida --help`, not Context7 or the Frida website.

2. **Python API ≠ CLI behavior.** The `frida` Python module and the `frida` CLI tool do NOT provide identical environments. Always verify with CLI first.

3. **Incremental testing is essential.** The `test.js` → `test7.js` progression was the right approach — but should have been done before writing 1000+ lines of orchestrator code.

4. **Native-level crashes are invisible to JS try-catch.** The `Java.enumerateLoadedClasses()` freeze was diagnosed only by comparing working vs. non-working versions with progressive limits.

5. **The `file://` protocol is useless for CDN-dependent HTML.** Always test with a local HTTP server.

6. **When stuck after 3+ attempts, the approach IS wrong.** The `stop-and-pivot` memory was written specifically because of this project's Python API dead-end phase.
