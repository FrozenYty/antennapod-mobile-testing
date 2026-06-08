# AntennaPod 移动测试项目 — 单元测试进度总结

**项目**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**被测应用**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**日期**: 2026-06-08

---

## 1. 总体进度

| 指标 | 数值 |
|------|------|
| 单元测试 TC 总数 | **8** |
| 单元测试方法总数 | **132** |
| 通过 | **132** |
| 失败 | **0** |
| 通过率 | **100%** |
| 涉及成员 | 4 人 |
| Sprint 跨度 | Sprint 1 ~ Sprint 4 |

---

## 2. 分成员贡献明细

### 2.1 Tianyu Yao（组长）— Sprint 1：Core Foundation

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-007 | Feed Entity Field Validation | 17 | 纯 JUnit | ✅ 通过 |
| TC-008 | FeedItem & FeedMedia Entity Validation | 34 | 纯 JUnit | ✅ 通过 |
| **小计** | | **51** | | **100%** |

**测试覆盖**：
- Feed 实体字段：构造函数、Title 解析、Identifier 解析、equals/hashCode、SortOrder 验证、Custom Title、isLocalFeed、setItems
- FeedItem 实体：构造函数、状态转换（played/new）、识别值、双向引用、下载状态、Tag 管理、Auto Download、pubDate、图片 fallback
- FeedMedia 实体：构造函数、下载状态、Position 管理、文件存在性、equals/hashCode、播放状态、本地文件可用性

---

### 2.2 Jianheng Sun — Sprint 2：Subscription & Discovery

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-016 | Feed URL Parsing & Normalization | 24 | Robolectric | ✅ 通过 |
| TC-017 | Subscription Sort & Filter Logic | 23 | Robolectric | ✅ 通过 |
| **小计** | | **47** | | **100%** |

**测试覆盖**：
- URL 解析：协议保留/自动添加/转换（feed/itpc/pcast）、AntennaPod deeplink 提取、空白裁剪、大小写不敏感、urlEquals
- 排序过滤：FeedOrder 往返、SortOrder scope 验证、FeedItemFilter 匹配（Played/Unplayed/New/Queued/Favorite/HasMedia）、SubscriptionsFilter 序列化

> ⚠ 两个 TC 均需 `@RunWith(RobolectricTestRunner::class)`，因为被测类依赖 `android.util.Log.d()` 和 `android.text.TextUtils`。

---

### 2.3 Yuanbing Wang — Sprint 3：Playback & Downloads

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-026 | Playback State Machine Logic | 11 | 纯 JUnit | ✅ 通过 |
| TC-027 | Download Queue Priority Logic | 10 | 纯 JUnit | ✅ 通过 |
| **小计** | | **21** | | **100%** |

**测试覆盖**：
- 播放状态机：全部 10 个 PlayerStatus 唯一性、`isAtLeast()` 层级关系（Playing/Stopped/Paused/Initialized/Error/Indeterminate）、null 安全、valueOf 往返、边界值
- 下载队列：默认状态、setDownloaded 标记/清除、本地文件可用性、URL 清除行为、equals/hashCode、duration/size setter、position 跟踪

---

### 2.4 Xintao Wang — Sprint 4：Settings & System

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-035 | User Preferences Read / Write Logic | 8 | Robolectric | ✅ 通过 |
| TC-036 | Storage Path Validation & Sanitization | 5 | Robolectric | ✅ 通过 |
| **小计** | | **13** | | **100%** |

**测试覆盖**：
- 用户偏好：Theme 往返、Playback 持久化、Feed Refresh Interval、Mobile Data 开关、Notification Buttons、Default Page、Bottom Navigation、Proxy Config
- 存储路径：默认路径、自定义根路径 & 子文件夹、Null type 行为、无效路径 fallback、路径替换

> ⚠ 两个 TC 均需 `@RunWith(RobolectricTestRunner::class)`，因为 `UserPreferences` 依赖 `android.content.Context` + `SharedPreferences`。

---

## 3. 贡献对比一览

| 成员 | Sprint | 模块 | TC 数 | 测试方法数 | 占比 |
|------|--------|------|-------|-----------|------|
| **Tianyu Yao** | 1 | Core Foundation | 2 | **51** | 38.6% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 2 | **47** | 35.6% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 2 | **21** | 15.9% |
| **Xintao Wang** | 4 | Settings & System | 2 | **13** | 9.8% |
| **合计** | | | **8** | **132** | **100%** |

```
Tianyu Yao      ██████████████████████████████████████████  51 (38.6%)
Jianheng Sun    ███████████████████████████████████████     47 (35.6%)
Yuanbing Wang   ██████████████████████                       21 (15.9%)
Xintao Wang     ██████████████                               13 ( 9.8%)
```

---

## 4. 运行器分布

| 运行器 | 测试数 | 占比 | TC 列表 |
|--------|--------|------|---------|
| 纯 JUnit（无 Android 依赖） | 72 | 54.5% | TC-007, TC-008, TC-026, TC-027 |
| Robolectric（需 Android 上下文） | 60 | 45.5% | TC-016, TC-017, TC-035, TC-036 |

---

## 5. 文件清单

```
app-under-test/antennapod/app/src/test/java/de/danoeh/antennapod/
├── unit/
│   ├── TC007_FeedEntityTest.kt                 ← Tianyu Yao (17 tests)
│   ├── TC008_FeedItemFeedMediaTest.kt           ← Tianyu Yao (34 tests)
│   ├── TC016_FeedUrlParsingTest.kt              ← Jianheng Sun (24 tests)
│   ├── TC017_SortFilterLogicTest.kt             ← Jianheng Sun (23 tests)
│   ├── TC026_PlaybackStateMachineLogicTest.kt   ← Yuanbing Wang (11 tests)
│   ├── TC027_DownloadQueuePriorityLogicTest.kt   ← Yuanbing Wang (10 tests)
│   ├── TC035_UserPreferencesTest.kt             ← Xintao Wang (8 tests)
│   └── TC036_StoragePathValidationTest.kt       ← Xintao Wang (5 tests)
└── manual/
    ├── TC010_FirstLaunchUserFlowTest.kt          ← Tianyu Yao (20 steps)
    ├── TC020_DiscoveryUsabilityTest.kt           ← Jianheng Sun (20 steps)
    ├── TC030_LongPlaybackStabilityTest.kt        ← Yuanbing Wang (20 steps)
    └── TC040_AccessibilityEdgeCasesTest.kt       ← Xintao Wang (20 steps)
```

---

## 6. 全项目测试分布（供参考）

| 测试方法 | TC 数 | 测试方法数 | 占比 |
|----------|-------|-----------|------|
| Espresso（UI 自动化） | 13 | 54 | 32.5% |
| **单元测试（JUnit）** | **8** | **132** | **20.0%** |
| UIAutomator（跨应用/系统 UI） | 7 | 21 | 17.5% |
| Integration（SQLite/DB） | 6 | 37 | 15.0% |
| Manual（手动探索） | 4 | 80 步骤 | 10.0% |
| Performance（性能基准） | 2 | 7 | 5.0% |
| **合计** | **40** | **251 自动化 + 80 手动** | **100%** |

---

## 7. 关键经验

| 类别 | 要点 |
|------|------|
| 纯 JUnit vs Robolectric | 被测类若引用 `android.util.Log`、`TextUtils`、`Context`、`SharedPreferences` 则必须使用 Robolectric |
| CI 单元测试过滤 | `--tests "de.danoeh.antennapod.unit.*"` 只跑我们的包，避免触发 AntennaPod 上游已有测试 |
| 命名规范 | `TC<NNN>_<Title>Test.kt`，方法名 `descriptiveName_expectedBehavior` |
| @author 必备 | 每个测试类 KDoc 必须含 `@author Your English Name` |
| 纯文约束 | 代码/注释/文档/commit 全英文 |

---

## 8. 结论

**单元测试 100% 完成。** 4 名成员在 4 个 Sprint 中共交付 8 个单元测试 TC、132 个测试方法，均全部通过。覆盖 Feed 实体（51）、URL 解析与排序过滤（47）、播放状态机与下载队列（21）、用户偏好与存储路径（13）四大领域。CI 在每次 push 时自动运行全部单元测试以保证回归安全。
