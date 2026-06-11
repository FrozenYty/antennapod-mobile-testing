# AntennaPod 移动测试项目 — 性能测试进度总结

**项目**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**被测应用**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**日期**: 2026-06-11

---

## 1. 总体进度

| 指标 | 数值 |
|------|------|
| 性能测试 TC 总数 | **2** |
| 性能测试方法总数 | **7** |
| 通过 | **7** |
| 失败 | **0** |
| 通过率 | **100%** |
| 涉及成员 | 2 人 |
| Sprint 跨度 | Sprint 2, Sprint 4 |

---

## 2. 分成员贡献明细

### 2.1 Jianheng Sun — Sprint 2：Subscription & Discovery

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-019 | Feed Parsing Speed Benchmark | 3 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **3** | | **100%** |

**测试覆盖**：
- Feed 插入基准：20 次迭代，平均 < 100ms
- Feed 查询基准：20 次全游标扫描，平均 < 50ms
- FeedItem 带外键插入：20 次迭代，平均 < 200ms

**性能阈值**：

| 操作 | 迭代次数 | 阈值 | 实际结果 |
|------|----------|------|----------|
| Feed 插入 | 20 | < 100ms avg | ✅ 通过 |
| Feed 查询（全量扫描） | 20 | < 50ms avg | ✅ 通过 |
| FeedItem 插入 | 20 | < 200ms avg | ✅ 通过 |

---

### 2.2 Xintao Wang — Sprint 4：Settings & System

| TC-ID | 标题 | 测试数 | 运行器 | 状态 |
|-------|------|--------|--------|------|
| TC-039 | App Startup Time & Memory Footprint | 4 | AndroidJUnit4 | ✅ 通过 |
| **小计** | | **4** | | **100%** |

**测试覆盖**：
- MainActivity 冷启动：< 5000ms
- PreferenceActivity 启动：< 3000ms
- 重复 MainActivity 启动（3 次）：平均 < 3000ms
- 启动后内存占用：< 256MB

**性能阈值**：

| 操作 | 阈值 | 实际结果 |
|------|------|----------|
| MainActivity 冷启动 | < 5000ms | ✅ 通过 |
| PreferenceActivity 启动 | < 3000ms | ✅ 通过 |
| 重复启动平均（3x） | < 3000ms | ✅ 通过 |
| 启动后内存 | < 256MB | ✅ 通过 |

---

## 3. 贡献对比一览

| 成员 | Sprint | 模块 | TC 数 | 测试方法数 | 占比 |
|------|--------|------|-------|-----------|------|
| **Jianheng Sun** | 2 | Subscription & Discovery | 1 | **3** | 42.9% |
| **Xintao Wang** | 4 | Settings & System | 1 | **4** | 57.1% |
| **合计** | | | **2** | **7** | **100%** |

```
Jianheng Sun    ███████████████████████████████████           3 (42.9%)
Xintao Wang     ████████████████████████████████████████████  4 (57.1%)
```

---

## 4. 测试架构

| 组件 | 说明 |
|------|------|
| 运行器 | `AndroidJUnit4`（真机/模拟器执行） |
| 计时方式 | `System.nanoTime()` — 手动测量（未配置 Macrobenchmark/Microbenchmark 库） |
| 内存测量 | `Runtime.getRuntime()` — GC 后 totalMemory 减 freeMemory |
| 空闲同步 | 测量前调用 `waitForIdleSync()` 确保 Activity 完全渲染 |
| 迭代次数 | DB 操作 20 次，Activity 启动 3 次循环 |
| 数据库 | 真实 SQLite + `tearDownTests()` 清理 |

---

## 5. 文件清单

```
app-under-test/antennapod/app/src/androidTest/java/de/danoeh/antennapod/
└── performance/
    ├── TC019_FeedParsingBenchmarkTest.kt        ← Jianheng Sun (3 tests)
    └── TC039_StartupMemoryBenchmarkTest.kt      ← Xintao Wang (4 tests)
```

---

## 6. 全项目测试分布（供参考）

| 测试方法 | TC 数 | 测试方法数 | 占比 |
|----------|-------|-----------|------|
| Espresso（UI 自动化） | 13 | 54 | 32.5% |
| 单元测试（JUnit） | 8 | 132 | 20.0% |
| UIAutomator（跨应用/系统 UI） | 7 | 21 | 17.5% |
| 集成测试（SQLite/DB） | 6 | 37 | 15.0% |
| Manual（手动探索） | 4 | 80 步骤 | 10.0% |
| **性能测试（基准）** | **2** | **7** | **5.0%** |
| **合计** | **40** | **251 自动化 + 80 手动** | **100%** |

---

## 7. 关键经验

| 类别 | 要点 |
|------|------|
| 无 Benchmark 库 | 项目无 `androidx.benchmark` 依赖——`System.nanoTime()` 手动计时对阈值断言已足够 |
| 内存测量前 GC | 调用 `Runtime.getRuntime().gc()` 后再读内存，结果更稳定 |
| `waitForIdleSync()` | 计时前必须调用——确保 Activity 完全渲染再取样 |
| 阈值设计 | 保守阈值（插入 100ms、查询 50ms、冷启动 5s、内存 256MB）在模拟器上可靠通过且留有真机余量 |
| 迭代次数 | DB 操作 20 次平均化 I/O 波动；启动 3 次给出热缓存信号 |

---

## 8. 结论

**性能测试 100% 完成。** 2 名成员交付 2 个性能测试 TC、7 个测试方法，均全部通过。TC-019 验证数据库操作吞吐量（Feed 插入 < 100ms、查询 < 50ms、Item 插入 < 200ms，20 次迭代取平均）。TC-039 验证应用启动延迟（冷启动 < 5s、热启动 < 3s）及内存占用（启动后 < 256MB）。所有测量使用 `System.nanoTime()` 手动计时，阈值保守，适用于 CI 回归检测。
