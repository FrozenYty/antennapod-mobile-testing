# Vibe Coding 心得：一个 Android 测试项目的 AI 协作实践总结

> **作者**：Tianyu Yao（FrozenYty）
> **日期**：2026-06-17
> **项目**：[antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
> **AI 搭档**：Claude Code（Anthropic Claude Fable 5）

---

## 一、什么是 Vibe Coding

"Vibe Coding" 不是让 AI 替你写代码。它是一种**协作范式**——人定义方向、约束和目标，AI 负责执行层面的编码、调试和文档同步。人的注意力集中在"做对的事"，而不是"把事做对"。

在这个项目中，我和 Claude Code 协作完成了 4 人团队的 40 个测试用例、一个 Frida 动态调用图子系统、以及一整套项目基础设施（文档、CI、代码规范）。代码总量超过 5000 行 Kotlin + 1500 行 Python/JS。整个过程让我形成了一套可复制的方法论。

本文从六个维度总结这套方法论：**项目架构**、**记忆系统**、**核心模式**、**案例解剖**、**人的角色**、**未来展望**。

---

## 二、基础设施：为 AI 协作设计的项目架构

Vibe Coding 的第一个教训是：**AI 不会记住你的项目**。每次新会话，AI 都是从零开始。如果不做特殊设计，你就会陷入"每次都要解释一遍项目是干什么的"的循环。

我们的解决方案是用四个互相配合的 Markdown 文件构成 AI 的"入职系统"：

### 2.1 四层文档架构

| 文件 | 角色 | 谁来读 |
|------|------|--------|
| `AI-PROMPT.md` | 可复制的提示词模板，一句话描述"我需要你做什么" | 人类贴给 AI |
| `AI-GUIDE.md` | 完整的协作手册：环境、代码模式、陷阱清单、截图规范 | AI 读完后立刻能写代码 |
| `CONTRIBUTING.md` | 规则清单：提交格式、文件组织、硬性禁令 | AI 写代码前必须遵守 |
| `PROGRESS.md` | AI 的"唯一记忆"：当前状态、已完成任务、待办事项、所有踩过的坑 | 每次对话开头必读 |

**关键洞察**：`PROGRESS.md` 是整个系统的核心。它的设计原则是——

- **实时更新**：每次编译、每次测试运行、每次修复后都立刻写进去
- **面向 AI 而非人**：包含了 `frida --help` 的发现、Thread.sleep 崩溃的根因、下一步做什么——这些是人类不需要写下来的上下文，但对 AI 至关重要
- **"当上下文丢失时，这个文件是你唯一的记忆"**——这是我们写在 `PROGRESS.md` 顶部的第一句话

```markdown
# PROGRESS.md — AI Session State

> Read this first. Update it in real-time — after every compile, 
> every test run, every fix. When context is lost, this file is 
> your only memory. Keep it current.
```

### 2.2 "Claude 层"指令系统

除了项目文档，我们还建立了一个三级指令系统：

```
~/.claude/CLAUDE.md          ← 全局行为规则（语言、输出路径、子代理策略）
D:\MYPROJS\.claude\CLAUDE.md ← 工作区覆盖（Conda 环境、Bash 路径）
Memory System (10 个文件)     ← AI 从错误中学到的教训
```

- **全局 CLAUDE.md** 定义了行为准则：先思考再编码、简单优先、手术式修改、目标驱动执行、中文对话英文写作
- **工作区 CLAUDE.md** 提供了技术约束：Conda 环境路径、Bash 工具的特殊性
- **记忆系统**（下一节详述）记录了项目中反复出现的模式

这三层形成了一个"AI 宪法"——全局原则优先，项目约束叠加，记忆提供经验。

### 2.3 团队协作维度的设计

这个项目有 4 个人，每人 10 个 TC，互不重叠。我们通过 `TASK-BRIEF.md` 将任务结构化为：

```
Plan → Code → Compile → Run → Document → Commit → Push
```

每个成员的文件放在预创建的目录中，每人更新自己的 `PROGRESS.md` 段落。不需要分支、不需要 PR——因为 TC 范围互不重叠，直接推 main 就能避免合并冲突。

这个设计的关键是**提前消除冲突的可能性**——不是靠 Git 工具解决冲突，而是靠流程设计让冲突根本不会发生。Vibe Coding 中，这种"设计消除问题"的思维方式贯穿始终。

---

## 三、记忆系统：从错误中学习的元认知循环

Vibe Coding 最独特的创新是**记忆系统**（Memory System）。它不是代码注释，不是文档，而是 AI 对自己的错误进行反思后编码成的持久化规则。

### 3.1 记忆的结构

每条记忆包含三个要素：

```yaml
name: stop-and-pivot
description: "同一错误反复出现时停止迭代——查官方文档并准备放弃当前方案"
metadata:
  type: feedback          # user | feedback | project | reference
---
具体内容：描述问题、根因、正确做法。

Why: 为什么这个教训重要
How to apply: 下次遇到类似情况的具体步骤
Links: [[cli-help-first]]    # 链接到其他相关记忆
```

### 3.2 十条核心记忆

以下是我在 AntennaPod 项目中积累的十条记忆，按主题分类：

#### 认知层面：如何思考问题

**[[constraints-first]]（约束优先）**
> 修复多约束问题时，先列出所有硬性要求，再碰代码。逐条修修补补会导致 ping-pong——修了 A 坏了 B。

人给反馈是一次一个症状的（"颜色不对"），但很少一次性说出全部约束。如果你只修刚报告的问题，就会把上一轮修好的又弄坏。**动手前问自己**：这一轮的要求 + 上一轮的要求 + 硬性限制 = 全部约束是什么？

**[[stop-and-pivot]]（停止并转向）**
> 同一错误在 3+ 次尝试后依然出现——停。问题不是实现细节，是方案本身错了。

Frida Python API 试了 5 次都是 `Java is not defined`。30 秒的 `frida --help` 就发现了 `-q -t` 非交互模式。**教训**：重复失败不是不够努力，是方向错误。

**[[verify-before-build]]（验证后构建）**
> 依赖复杂外部系统时，先用最小脚本验证每个假设，再构建完整系统。

我们在 Frida 项目中写了 7 个递进的测试脚本（`test.js` → `test7.js`），每个验证一个假设：Java bridge 可用？方法 hook 可行？setTimeout 在 `-q` 模式下触发？延迟枚举能找到类？——可惜这些都是在写完 1000+ 行 orchestrator 之后才做的。**教训**：正确顺序是验证→构建，不是构建→验证。

#### 行动层面：怎么做事

**[[cli-help-first]]（CLI 帮助优先）**
> 集成 CLI 工具时，先看 `--help`，再查在线文档。`--help` 更快、版本精确、保证匹配已安装的版本。

Frida 花了数小时调试 Python API 和子进程方法后，才发现 `-q`（quiet 模式）和 `-t <N>`（自动退出超时）这两个救命标志——它们都清清楚楚地写在 `frida --help` 里。

**[[api-drift-check]]（API 漂移检查）**
> 教程/课程代码和已安装库的 API 不匹配时，先验证再编码。大版本升级经常破坏方法签名。

Androguard 的 `get_vm_method()` 在 4.x 变成了 `get_method()`，教程用的是 3.x 的 API。一行 `dir()` 探查能省几小时的调试。

**[[migration-checklist-first]]（迁移清单优先）**
> 跨大版本升级工具时，先查完整的迁移指南，一次性修复所有破坏性变更。

AGP 8→9 有 9 个独立的破坏性变更——逐条推 CI 修复浪费了 9 轮，一次迁移指南阅读就够了。

**[[local-test-before-push]]（本地测试后推送）**
> 修改依赖版本、构建脚本或编译器标志后，先在本地跑测试套件。一次本地运行代替 N 轮 CI。

本地运行 30 秒，一次性看到所有错误。推 CI 每次只暴露一个错误，N 轮下来就是 push → wait → fix → push 的循环。

**[[bash-not-powershell]]（Bash 不是 PowerShell）**
> Claude Code 的 Bash 工具运行在 Git Bash（MinGW）上，不是 PowerShell。Windows 反斜杠路径会被当作转义字符处理。

`D:\anaconda3\envs` 变成 `D:anaconda3envs`——`\a` 是响铃字符，`\e` 是转义字符。永远用正斜杠：`D:/anaconda3/envs`。这是环境层面的坑，不是代码层面的。

#### 审查层面：怎么发现错误

**[[silent-errors-are-dangerous]]（静默错误最危险）**
> 不崩溃的 bug——字符串真值、falsy-zero、类型强制——能通过"跑起来了"检查，静默地产生错误结果。

Python 的 `if "false":` 是真值，`if count:` 会跳过合法值 `0`。这些 bug 不会崩溃，不会报错，输出看起来"差不多对"。只有结构化代码审查才能发现。

**[[review-by-asset-type]]（按资产类型审查）**
> 并行审查代理按资产类型划分（代码/文档/截图），而不是按审查维度划分。不同类型暴露的问题几乎没有重叠。

代码审查者抓住命名违规和导入问题；文档审查者抓住过时状态和跨文件不一致；截图审查者抓住孤儿文件和破损引用。三者发现的 16 个问题几乎零重叠——按维度审查则会大量重复。

### 3.3 记忆的演进路径

每条记忆都经历了一个标准的"进化"过程：

```
犯错 → 手动修复 → 意识到模式 → 写 memory → 链接到其他 memory 
→ 升级为 project rule →（最终）融入 CLAUDE.md
```

比如 `[[stop-and-pivot]]` 和 `[[cli-help-first]]` 是同一个 Frida 调试经历的两种表述——前者是认知策略（什么时候该停），后者是行动策略（停了之后看什么）。它们互相链接，形成知识网络。

**记忆系统的本质**：它把 AI 的"这次我知道了"变成了"下次我也知道"。普通的 AI 对话是失忆的——每次新会话都是空白。记忆系统让 AI 真正从经验中学习。

---

## 四、核心模式：十条经过验证的协作原则

从记忆系统中，我提炼出五个最重要的 vibe coding 原则：

### 原则 1：约束先于代码（Constraints Before Code）

坏的 vibe coding：拿到需求就开始写代码。好的 vibe coding：先和 AI 一起列出所有约束，达成共识，再动手。

在 Frida 项目中，约束包括：
- 不能修改 app 源代码（只能 hook）✓
- 必须在真实 Android 运行时中工作 ✓
- 必须自动发现类和方法（不能硬编码）✓
- 结果必须在浏览器中可视化 ✓
- Frida 版本 17.10.1 已安装 ✓

把这些写清楚之后，方案选择就自然了——Python API 不满足约束 2（Java bridge 不可用），CLI 子进程满足。

**操作指南**：在给 AI 下指令之前，用三句话描述：**（1）输入是什么 （2）输出是什么 （3）什么绝对不能做**。这三句话比 500 字的描述更有用。

### 原则 2：增量验证，而非大爆炸(Incremental Verify, Not Big Bang)

坏的 vibe coding：让 AI 一次性写完整个模块，然后调试。好的 vibe coding：拆成最小可验证单元，每个单元独立验证后继续。

Frida 项目中，7 个递进测试脚本的结构：

| 脚本 | 验证假设 | 结论 |
|------|----------|------|
| test.js | CLI 下 Java bridge 可用？ | ✅ |
| test2.js | 能 hook MainActivity 方法？ | ✅ |
| test3.js | `setTimeout` 在 `-q -t` 下触发？ | ✅ |
| test4.js | `enumerateLoadedClasses` 回调触发？ | ✅（但 t=0 时无 AntennaPod 类）|
| test5.js | 延迟枚举能找到 AntennaPod 类？ | ✅（t=3s 时 451 个类）|
| test6.js | `Java.perform()` 在 `setTimeout` 内可用？ | ✅ |
| test7.js | SleepTimerType 枚举不会单独崩溃？ | ✅ |

每一个测试 ≤30 行 JS，验证一个假设。全部通过后再构建完整的 orchestrator。

**操作指南**：接到复杂任务时，先让 AI 列出它需要验证的假设清单，然后逐条用最小脚本验证。不要在验证完成前写"真正的代码"。

### 原则 3：停在第三次失败（Stop at Third Failure）

这是 `[[stop-and-pivot]]` 的核心内容。同一方向的第三次失败不是"快成功了"的信号——它是"路走错了"的信号。

Frida Python API 尝试记录：

| 尝试 | 方法 | 结果 |
|------|------|------|
| 1 | `device.attach(pid)` + `create_script()` | `Java is not defined` |
| 2 | `device.attach('AntennaPod Debug')` | 同上 |
| 3 | `device.spawn([package])` | 同上 |
| 4 | `runtime='v8'` | 同上 |
| 5 | 手动加载 bridge 协议 | `Java` 仍为 undefined |

到第 5 次才发现 `Java` 是 Frida C core 自动注入的，Python API 不做这件事。应该在第 3 次就停。

**操作指南**：设定一个硬性规则——同一方案失败 3 次，第 4 次必须是完全不同的方案。把这条写进 CLAUDE.md。

### 原则 4：状态文件是 AI 的硬盘（State File is AI's Hard Drive）

AI 的上下文窗口是"内存"——易失、有限、每次新对话就清空。`PROGRESS.md` 是"硬盘"——持久、可追加、跨会话保留。

好的 `PROGRESS.md` 包含：

```
- Right Now: 当前状态（设备、版本、最后更新时间）
- Done: 已完成的任务（带勾选框）
- Blockers & Decisions: 每一个踩过的坑和解决方案
- Lessons Learned: 本次会话的核心教训
- Quick Resume: 下次会话的快速恢复步骤
- Command Cheatsheet: 常用命令（省去每次重新推导）
```

**关键实践**：不是"做完再记录"，而是"边做边记录"。每次编译后更新，每次修复后更新，每次 pivot 后更新。一个过时的 PROGRESS.md 比没有 PROGRESS.md 更危险——它会误导下一个 AI 会话。

### 原则 5：人做决策，AI 做执行（Human Decides, AI Executes）

这条看似简单，但在实践中经常被打破。AI 倾向于"擅自做决定"——选择一个 API、跳过验证步骤、写一个"看起来对"但实际有问题的实现。

在我们的项目中，人的角色是：

- **架构决策者**：Python API 还是 CLI 子进程？→ 人决定
- **质量守门人**：这 24 张截图中有 19 张是重复的 → 人判断
- **领域专家**：`SortOrder` 是 enum 不是 class，`feed_id` 不是 `id` → 人知道
- **约束制定者**：不能改 main 源码、不能用 `git add -A`、必须英文 → 人设定

AI 的角色是：

- **执行者**：按照约束写代码、跑测试、更新文档
- **模式匹配器**：从已有测试中提取模板并应用
- **文档同步器**：确保代码和 PROGRESS.md/test-cases.md 保持一致
- **错误调试器**：编译失败→修复→重试的循环

**关键洞察**：最有效的 vibe coding 不是"AI 做所有事"，而是"人做所有决策，AI 做所有执行"。当这个边界模糊时——AI 替人做了决策，或者人陷入了执行细节——效率就开始下降。

---

## 五、Frida 案例：一次完整的 Vibe Coding 实战解剖

Frida 动态调用图子系统是 vibe coding 的完整样本——从需求到交付，经历了 3 天、7 次 pivot、数十次失败和修复。以下是完整的时间线分析。

### 5.1 需求与架构设计（Day 1）

**用户意图**："基于 frida-trace，实现代码，构建方法的调用图" + "可以让结果在浏览器中预览"

**架构决策**：
- 两个组件：`frida_hooks.js`（注入 Android 运行时的 JS payload）+ `frida_callgraph.py`（Python 编排器）
- 动态 hook 策略：`Java.enumerateLoadedClasses()` + `getDeclaredMethods()`
- 调用者识别：`Thread.currentThread().getStackTrace()`（后来证明这是错的）
- 输出：matplotlib 图表 + 交互式 HTML（vis-network + Chart.js）

### 5.2 Python API 死胡同（Day 1-2）

**问题**：`frida.get_usb_device().attach(pid).create_script(js)` 永远不能让 `Java` 在脚本运行时中可用。

**5 次尝试全部失败**（见原则 3 的表格）。

**根因**（通过 `frida --help` 发现）：`Java` 全局对象是 Frida C core 检测到目标为 ART/Dalvik 进程时自动创建的。CLI 工具 `frida -U` 触发这个检测，但 Python API 的原始 `create_script()` 不会。

**Pivot**：放弃 Python API，改用 CLI 子进程方案（`frida -U -f <pkg> -l script.js -q -t <N>`）。

**vibe coding 教训**：**API 不等价**。同一个工具的 Python 绑定和 CLI 工具不提供相同的运行时环境。永远不要假设它们行为一致——先用 CLI 验证。

### 5.3 缓冲地狱（Day 2）

**新问题**：管道缓冲。`proc.stdout.readline()` 阻塞，因为 frida 的 C stdout 在写入管道时使用全缓冲。

**尝试修复**：
1. 多线程非阻塞读取——frida 从不产生足够的输出来触发 flush
2. `stdbuf -oL`——Windows 上不工作
3. **最终修复**：`frida -o logfile.txt` 直接写文件，退出后读取

**vibe coding 教训**：**子进程管道是陷阱**。跨语言（Python ↔ C ↔ JS）的 stdout 缓冲行为不可预测。文件 I/O 更笨但更可靠。

### 5.4 原生崩溃与增量诊断（Day 2-3）

**问题**：`Java.enumerateLoadedClasses()` 在某些类上冻结——不是 JS 异常，是 C/原生级别的崩溃，JS 的 try-catch 完全不可见。

**诊断过程**（`test.js` → `test7.js` 递进验证）：

| 发现 | 脚本 |
|------|------|
| CLI 下 Java bridge 可用 | test.js |
| 方法 hook 可行 | test2.js |
| `setTimeout` 在 `-q -t` 下触发 | test3.js |
| `enumerateLoadedClasses` 回调触发 | test4.js |
| t=0 时 0 个 AntennaPod 类加载 | test4.js |
| t=3s 时 451 个 AntennaPod 类可用 | test5.js |
| Hook 脚本必须延迟扫描 | test5.js 洞察 |
| `Java.perform()` 在 `setTimeout` 内可用 | test6.js |
| SleepTimerType 枚举单独不崩溃 | test7.js |

**安全措施**：
- `attempted >= 25` 硬限制（防止在问题类上冻结）
- `isEnum()` / `isInterface()` 检查（跳过问题类型）
- `$$ExternalSynthetic` / `BuildConfig` / `Manifest$` 模式过滤
- `MAX_PER_CLASS = 15`（限制每个类的方法数）

**vibe coding 教训**：**原生崩溃是沉默的**。JS try-catch 不能捕获 C 层的崩溃。唯一的诊断方法是对比工作版本和非工作版本，逐步收紧限制。

### 5.5 Thread.sleep 崩溃（Day 3）

**问题**：`Java.use('java.lang.Thread')` 崩溃：`TypeError: Cannot read properties of null (reading 'sleep')`。

**根因**：Frida 17.10.1 在 Android 12 上的 bug。

**修复**：使用 `Java.use('java.lang.Exception')` + `new Exception().getStackTrace()` 替代。这是标准 Android 模式，完全避免了 `java.lang.Thread`。

**vibe coding 教训**：**工具 bug 是真实存在的**。当核心 API 调用崩溃时，不要假设自己的代码有问题——搜索该工具的已知问题，然后找替代方案。

### 5.6 可视化与数据丰富度（Day 3）

**用户反馈**："这个图为什么这么简单" → 从类级别网络图改为方法级别（9 nodes → 21 nodes）
**用户反馈**："Stats Charts 是空白" → Chart.js 在隐藏面板中初始化时画布尺寸为零，改为 tab 切换时懒初始化

**最终交付**：

| 文件 | 描述 |
|------|------|
| `frida_hooks_cli.js` | CLI 兼容的 JS hook（Exception 栈追踪） |
| `frida_callgraph.py` | Python 编排器（CLI 子进程 + ADB 交互 + HTML + matplotlib） |
| `frida-callgraph.html` | 交互式浏览器报告（vis-network + Chart.js + edge table） |
| `frida-callgraph-stats.{png,pdf}` | Top callers/callees 柱状图 |
| `frida-callgraph-edges.{png,pdf}` | Top call edges 排名 |
| `frida-callgraph-heatmap.{png,pdf}` | 包级调用密度热力图 |
| `frida-callgraph.json` | 结构化 JSON 导出 |

**跟踪结果**：17 个类、93 个方法 hooked、14 条唯一调用边、80 次总方法调用。

### 5.7 Frida 案例的 Vibe Coding 总结

整个 Frida 子项目的 vibe coding 特征：

1. **人设定方向，AI 做执行**：人定义"构建调用图"和"浏览器可视化"，AI 设计架构、写代码、调试
2. **人设定约束，AI 在约束内探索**：不能改 app 源码、只能用 Frida 17.10.1、必须自动发现
3. **人识别 pivot 时机**："一直犯错的话可以先停下来思考一下"——这是人的判断
4. **AI 记录经验**：Frida 案例直接催生了 4 条 memory（`[[stop-and-pivot]]`、`[[cli-help-first]]`、`[[verify-before-build]]`、`[[api-drift-check]]`）
5. **人在反馈循环中**：每个交付物都经过人评审（"图为什么这么简单"、"图表是空白"），然后 AI 迭代

---

## 六、人的角色：在 AI 协作中人的不可替代性

Vibe coding 的核心悖论是：**AI 越强大，人的判断力越重要**。因为 AI 能更快地产出更多代码，但也会更快地放大错误。

### 6.1 人必须做的五件事

**（1）定义"完成"**
AI 倾向于"看起来完成了"就停止。人必须定义可验证的完成标准：
- "写测试" → 模糊
- "TC-011 通过 4/4 测试用例，在 MuMu 模拟器上运行，截图已保存，PROGRESS.md 已更新" → 可验证

**（2）发现模式**
AI 看到的是单个错误。人看到的是"这已经是第三次用 Python API 失败了——可能是方案有问题"。模式识别 → pivot 决策——这是人独有的能力。

**（3）设置约束**
AI 倾向于"能做就做"。人必须说"不能做什么"：
- 不能改 `app/src/main/`
- 不能用 `git add -A`
- 截图质量>数量
- 必须用英文

**（4）审查输出**
AI 的输出"看起来对"不等于"对"。在我们的项目中：
- 24 张初始截图中有 19 张是重复的 → 人发现
- `SortOrder` 被 AI 当作可构造的 class → 人纠正（它是 enum）
- `feed_id` 被 AI 当作 `id` → 人纠正（cursor column 名不同）

**（5）维护记忆**
AI 不会主动说"我应该记住这个教训"。人必须识别哪些教训值得保留，并触发 memory 的写入。

### 6.2 人不该做的事

**（1）不要陷入执行细节**
如果 AI 写的代码有编译错误，让 AI 自己修复。人不需要理解每一行代码——只需要确保测试通过了、文档更新了、逻辑是对的。

**（2）不要跳过文档更新**
"先写代码，晚点补文档" = 永远不会补。在 vibe coding 中，文档不是附加品——它是 AI 下一次会话的唯一上下文。

**（3）不要替 AI 做决定**
当你对 AI 说"试试 X"时，你正在替 AI 做它应该自己做的决策。更好的说法是"当前方案失败，请提出 3 个替代方案并比较"。

---

## 七、Vibe Coding 的边界：什么场景不适合

经过这个项目的实践，我认为 vibe coding 在以下场景效果最好：

| 适合 | 不太适合 |
|------|----------|
| 结构化任务（测试用例、CRUD、脚本） | 开放式探索（"帮我研究一下"） |
| 有明确约束的工作 | 约束本身就是未知的 |
| 增量可验证的任务 | 需要大量上下文理解才能判断正确性的任务 |
| 有现有模式可复制的工作 | 完全从零创造架构 |
| 多人协作、需要文档同步的项目 | 个人快速原型 |

### 关键边界条件

1. **Vibe coding 需要"正确的第一次"**：如果 AI 的初始方案方向错了，后面的迭代都在错误的轨道上。这就是为什么"约束先于代码"和"增量验证"如此重要。

2. **Vibe coding 对文档依赖极强**：没有 PROGRESS.md、AI-GUIDE.md、memory 系统，vibe coding 就退化成"每次重新解释"—本质上是带自动补全的手动编码。

3. **Vibe coding 不能替代领域知识**：人不理解 Android 测试框架、不理解 Frida 的工作原理、不理解 Gradle 的构建系统——vibe coding 会产生大量"看起来对但实际错"的代码。人的领域知识是质量底线。

---

## 八、结语：Vibe Coding 作为一种工程纪律

经过 AntennaPod 项目三个月的实践，我对 vibe coding 的理解从"让 AI 写代码"演变为"设计一套系统，让 AI 能在其中可靠地写代码"。

这套系统包括：

- **项目架构**：AI-PROMPT.md → AI-GUIDE.md → CONTRIBUTING.md → PROGRESS.md 的四层入职文档
- **记忆系统**：10 条经验教训，结构化编码，互相链接，持续演进
- **工作流**：Plan → Code → Compile → Run → Document → Commit → Push 的完整循环
- **反馈环**：每个错误 → 理解根因 → 编码为 memory → 升级为 rule → 融入 CLAUDE.md

Vibe coding 不是魔法。它是一种**工程纪律**——需要你比传统编码更清楚地思考"我在做什么"、"我的约束是什么"、"什么算完成"。AI 让你的手更快，但不会让你的脑子更清楚。

**最重要的一个建议**：如果你只能从这篇心得中带走一件事，那就是——**在开始 vibe coding 之前，先花 30 分钟写 PROGRESS.md 和 AI-GUIDE.md**。这 30 分钟的投资会在接下来的每一次 AI 会话中获得回报。

---

## 附录：项目关键数据

| 指标 | 数据 |
|------|------|
| 项目周期 | 2026-05-28 ~ 2026-06-07（约 10 天） |
| 团队规模 | 4 人（1 Lead + 3 Members） |
| 测试用例总数 | 40 个（TC-001 ~ TC-040） |
| 测试方法类型 | 7 种（Espresso, UIAutomator, Unit, Integration, Manual, Performance, Static Analysis） |
| 自动化测试通过率 | 100%（所有 Espresso/UIAutomator/Unit/Integration/Performance TCs） |
| Frida 调用图覆盖 | 17 类、93 方法 hooked、14 条唯一调用边 |
| 记忆积累 | 10 条结构化记忆 |
| CI/CD | 每次 push 触发编译 + 单元测试 + 文档检查 |
| 代码总量 | ~5000 行 Kotlin + ~1500 行 Python/JS + ~20 份文档 |