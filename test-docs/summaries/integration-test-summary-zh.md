# AntennaPod 移动测试项目 — 集成测试进度总结

**项目**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**被测应用**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**日期**: 2026-06-11

---

## 1. 总体进度

| 指标 | 数值 |
|------|------|
| 集成测试 TC 总数 | **6** |
| 集成测试方法总数 | **37** |
| 通过 | **37** |
| 失败 | **0** |
| 通过率 | **100%** |
| 涉及成员 | 4 人 |
| Sprint 跨度 | Sprint 1 – Sprint 4 |

---

## 2. 分成员贡献明细

### 2.1 Tianyu Yao（组长）— Sprint 1：Core Foundation

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-009 | PodDBAdapter Schema & Table Creation | 8 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **8** | | **100%** |

**测试覆盖**：
- 数据库 Schema 初始化及表创建验证
- Feeds 表：插入/检索、自增 ID
- FeedItems 表：插入/检索（含 Feed 外键）
- FeedMedia 表：插入（含 Item 引用）、Media ID 生成
- Queue 表：插入队列条目
- Favorites 表：插入收藏条目
- DownloadLog 表：插入日志记录
- SimpleChapters 表：插入章节标记

---

### 2.2 Jianheng Sun — Sprint 2：Subscription & Discovery

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-018 | Feed & FeedItem DAO Query Correctness | 8 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **8** | | **100%** |

**测试覆盖**：
- Feed CRUD：全字段插入、状态转换（STATE_ARCHIVED）、自定义标题持久化、唯一 ID 生成
- FeedItem 查询：多条目检索、排序（DATE_NEW_OLD）、Feed 级别隔离、队列条目创建

---

### 2.3 Yuanbing Wang — Sprint 3：Playback & Downloads

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-028 | FeedMedia DAO Read / Write Integrity | 6 | AndroidJUnit4 | ✅ 通过 |
| TC-029 | Episode Download Status Tracking | 7 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **13** | | **100%** |

**测试覆盖**：
- FeedMedia DAO：基本插入与 ID 生成、全字段检索、多条插入唯一 ID、Item-Media 关联、下载日志跟踪、播放位置存储
- 下载状态：成功/失败下载日志持久化、多条目有序性、Queue-Media 引用完整性、本地文件 URL 持久化、clearDownloadLog、clearQueue

---

### 2.4 Xintao Wang — Sprint 4：Settings & System

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-037 | Data Export & Import Integrity | 4 | AndroidJUnit4 | ✅ 通过 |
| TC-038 | Episode Cache Table Cleanup | 4 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **8** | | **100%** |

**测试覆盖**：
- OPML 导出/导入：往返字段保真、过滤未订阅/已归档 Feed、DBReader URL 过滤（排除归档 & 本地）、多 Feed 往返
- 缓存清理：clearDownloadLog 清除全部记录、clearOldDownloadLog 仅保留近期条目、clearQueue 清除队列、removeFeedItems 级联删除 Media 及下载日志

---

## 3. 贡献对比一览

| 成员 | Sprint | 模块 | TC 数 | 测试方法数 | 占比 |
|------|--------|------|-------|-----------|------|
| **Tianyu Yao** | 1 | Core Foundation | 1 | **8** | 21.6% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 1 | **8** | 21.6% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 2 | **13** | 35.1% |
| **Xintao Wang** | 4 | Settings & System | 2 | **8** | 21.6% |
| **合计** | | | **6** | **37** | **100%** |

```
Tianyu Yao      ████████████████████████                      8 (21.6%)
Jianheng Sun    ████████████████████████                      8 (21.6%)
Yuanbing Wang   █████████████████████████████████████████    13 (35.1%)
Xintao Wang     ████████████████████████                      8 (21.6%)
```

---

## 4. 测试架构

| 组件 | 说明 |
|------|------|
| 运行器 | `AndroidJUnit4`（所有 TC 在真机/模拟器上运行） |
| 数据库 | 真实 SQLite，通过 `PodDBAdapter.init(context)` 初始化——无 Mock |
| 隔离性 | `@After` 中调用 `PodDBAdapter.tearDownTests()` 清空数据 |
| 数据准备 | 通过 `insertTestData()` + `ContentValues` 精确控制插入内容 |
| 断言方式 | 基于 Cursor 的持久化数据验证 |

---

## 5. 文件清单

```
app-under-test/antennapod/app/src/androidTest/java/de/danoeh/antennapod/
└── integration/
    ├── TC009_PodDBAdapterSchemaTest.kt              ← Tianyu Yao (8 tests)
    ├── TC018_FeedItemDaoTest.kt                     ← Jianheng Sun (8 tests)
    ├── TC028_FeedMediaDaoReadWriteIntegrityTest.kt  ← Yuanbing Wang (6 tests)
    ├── TC029_EpisodeDownloadStatusTrackingTest.kt   ← Yuanbing Wang (7 tests)
    ├── TC037_DataExportImportIntegrityTest.kt       ← Xintao Wang (4 tests)
    └── TC038_EpisodeCacheCleanupTest.kt             ← Xintao Wang (4 tests)
```

---

## 6. 全项目测试分布（供参考）

| 测试方法 | TC 数 | 测试方法数 | 占比 |
|----------|-------|-----------|------|
| Espresso（UI 自动化） | 13 | 54 | 32.5% |
| 单元测试（JUnit） | 8 | 132 | 20.0% |
| UIAutomator（跨应用/系统 UI） | 7 | 21 | 17.5% |
| **集成测试（SQLite/DB）** | **6** | **37** | **15.0%** |
| Manual（手动探索） | 4 | 80 步骤 | 10.0% |
| Performance（性能基准） | 2 | 7 | 5.0% |
| **合计** | **40** | **251 自动化 + 80 手动** | **100%** |

---

## 7. 关键经验

| 类别 | 要点 |
|------|------|
| 真实数据库，无 Mock | 集成测试使用真机 SQLite——能捕获 Mock 无法发现的 Schema 偏移 |
| `PodDBAdapter(context)` 已弃用 | 必须使用 `PodDBAdapter.init(context)` + `getInstance()` |
| Cursor 列名差异 | Feed 游标使用 `feed_id`（SELECT_KEY_FEED_ID），而非 `id`（KEY_ID） |
| `getItemsOfFeedCursor` 列 Join | 可能省略可选列（如 `size`）——需用 `if (idx >= 0)` 保护 |
| DownloadLog 表结构 | 该表无 `download_url` 列——已从 ContentValues 中移除 |
| 测试隔离 | `@After` 中 `tearDownTests()` 确保每个测试从干净数据库开始 |

---

## 8. 结论

**集成测试 100% 完成。** 4 名成员在 4 个 Sprint 中共交付 6 个集成测试 TC、37 个测试方法，均全部通过。覆盖 PodDBAdapter 完整 CRUD 生命周期：Schema 创建（8）、DAO 查询正确性（8）、Media 读写完整性与下载跟踪（13）、数据导出/导入与缓存清理（8）。所有测试在真机 SQLite 上运行，确保无 Mock/生产环境偏差。
