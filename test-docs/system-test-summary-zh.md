# AntennaPod 移动测试项目 — 系统测试进度总结

**项目**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**被测应用**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**日期**: 2026-06-11

---

## 1. 总体进度

| 指标 | 数值 |
|------|------|
| 系统测试 TC 总数 | **20** |
| 系统测试方法总数 | **75** |
| 通过 | **75** |
| 失败 | **0** |
| 通过率 | **100%** |
| 涉及成员 | 4 人 |
| Sprint 跨度 | Sprint 1 ~ Sprint 4 |

系统测试按框架分为两类：

| 框架 | 用途 | TC 数 | 测试方法数 |
|------|------|-------|-----------|
| Espresso | 应用内 UI 自动化（单进程） | 13 | 54 |
| UIAutomator | 跨应用/系统 UI 自动化（多进程） | 7 | 21 |

---

## 2. 分成员贡献明细

### 2.1 Tianyu Yao（组长）— Sprint 1：Core Foundation

| TC-ID | 标题 | 测试数 | 框架 | 状态 |
|-------|------|--------|------|------|
| TC-001 | App Launch & Main Screen | 6 | Espresso | ✅ 通过 |
| TC-002 | Subscribe to Podcast | 4 | Espresso | ✅ 通过 |
| TC-003 | Play Episode | 4 | Espresso | ✅ 通过 |
| TC-004 | Queue Management | 4 | Espresso | ✅ 通过 |
| TC-005 | Search & Discovery | 4 | Espresso | ✅ 通过 |
| TC-006 | OPML Import via System File Picker | 3 | UIAutomator | ✅ 通过 |
| **小计** | | **25** | | **100%** |

**测试覆盖**：
- 应用启动：底部导航栏、AppBar、内容区域、首页默认 Tab、多 Tab 切换、截图采集
- 订阅：播客订阅流程、Feed 列表验证、订阅确认
- 播放：Episode 播放入口、播放器控件结构、媒体 UI 元素
- 队列：队列访问、Episode 列表、队列管理控件
- 搜索：搜索入口、发现页元素、搜索框交互
- OPML 导入：跨应用系统文件选择器、UIAutomator 底部导航、Package 检测

---

### 2.2 Jianheng Sun — Sprint 2：Subscription & Discovery

| TC-ID | 标题 | 测试数 | 框架 | 状态 |
|-------|------|--------|------|------|
| TC-011 | Browse Discovery Page | 4 | Espresso | ✅ 通过 |
| TC-012 | Subscribe from Discovery | 4 | Espresso | ✅ 通过 |
| TC-013 | Unsubscribe & Delete | 4 | Espresso | ✅ 通过 |
| TC-014 | Share Feed URL | 3 | UIAutomator | ✅ 通过 |
| TC-015 | Feed Refresh | 3 | UIAutomator | ✅ 通过 |
| **小计** | | **18** | | **100%** |

**测试覆盖**：
- 浏览发现：订阅 Tab 导航、底部导航验证、发现页 UI 结构、SwipeRefresh
- 从发现订阅：发现→订阅流程、Feed 添加确认、订阅列表更新
- 取消订阅：Feed 移除、删除确认、列表移除后状态
- 分享 URL：跨应用分享 Intent、系统分享面板、Feed URL 提取
- Feed 刷新：后台刷新触发、下拉刷新（系统 UI）、刷新状态检测

---

### 2.3 Yuanbing Wang — Sprint 3：Playback & Downloads

| TC-ID | 标题 | 测试数 | 框架 | 状态 |
|-------|------|--------|------|------|
| TC-021 | Play / Pause Episode Controls | 4 | Espresso | ✅ 通过 |
| TC-022 | Playback Speed Adjustment | 4 | Espresso | ✅ 通过 |
| TC-023 | Download Episode for Offline Playback | 4 | Espresso | ✅ 通过 |
| TC-024 | Audio Focus & Playback Notification | 3 | UIAutomator | ✅ 通过 |
| TC-025 | Background Playback Continuity | 3 | UIAutomator | ✅ 通过 |
| **小计** | | **18** | | **100%** |

**测试覆盖**：
- 播放/暂停：底部导航、Queue 条目、播放 UI 基础设施、More 菜单访问
- 播放速度：速度控件入口、速度对话框、滑块/预设验证
- 下载：Episode 浏览、下载操作、离线播放就绪、More 菜单导航
- 音频焦点：通知通道注册、系统通知访问、应用级通知基础设施
- 后台播放：Home 键连续性、最近应用导航、进程存活、系统级应用检测

---

### 2.4 Xintao Wang — Sprint 4：Settings & System

| TC-ID | 标题 | 测试数 | 框架 | 状态 |
|-------|------|--------|------|------|
| TC-031 | Theme & Display Settings | 4 | Espresso | ✅ 通过 |
| TC-032 | Storage & Network Preferences | 4 | Espresso | ✅ 通过 |
| TC-033 | Runtime Permission Handling | 3 | UIAutomator | ✅ 通过 |
| TC-034 | Notification Channel Settings | 3 | UIAutomator | ✅ 通过 |
| **小计** | | **14** | | **100%** |

**测试覆盖**：
- 主题显示：设置入口、用户界面分区、主题控件、着色主题偏好
- 存储网络：下载分区、存储选项、移动数据开关、自动下载偏好
- 运行时权限：Manifest 声明验证、系统设置应用页面、权限 Surface 检测
- 通知通道：系统设置通道注册、通道列表可访问性、通知管理

---

## 3. 贡献对比一览

| 成员 | Sprint | 模块 | TC 数 | 测试方法数 | 占比 |
|------|--------|------|-------|-----------|------|
| **Tianyu Yao** | 1 | Core Foundation | 6 | **25** | 33.3% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 5 | **18** | 24.0% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 5 | **18** | 24.0% |
| **Xintao Wang** | 4 | Settings & System | 4 | **14** | 18.7% |
| **合计** | | | **20** | **75** | **100%** |

```
Tianyu Yao      █████████████████████████████████████████████  25 (33.3%)
Jianheng Sun    ████████████████████████████████████           18 (24.0%)
Yuanbing Wang   ████████████████████████████████████           18 (24.0%)
Xintao Wang     ██████████████████████████████                 14 (18.7%)
```

---

## 4. 框架分布

| 框架 | 测试数 | 占比 | TC 列表 |
|------|--------|------|---------|
| Espresso（应用内 UI，单进程） | 54 | 72.0% | TC-001~005, TC-011~013, TC-021~023, TC-031~032 |
| UIAutomator（跨应用，多进程） | 21 | 28.0% | TC-006, TC-014~015, TC-024~025, TC-033~034 |

---

## 5. 测试环境

| 组件 | 取值 |
|------|------|
| 主设备 | MuMu 模拟器（ALN-AL00，Android 12，x86_64，127.0.0.1:7555） |
| 辅助设备 | Pixel_7 AVD（API 37）— UIAutomator TC-024, TC-025 |
| Espresso API 限制 | API ≤ 34（`InputManager.getInstance()` 在 API 37 中被移除） |
| Activity 启动方式 | `ActivityTestRule(Activity::class.java, false, false)` + 手动 `launchActivity()` |
| 截图采集 | `TestHelper.saveScreenshot()` → `/storage/emulated/0/Download/screenshots/` |

---

## 6. 文件清单

```
app-under-test/antennapod/app/src/androidTest/java/de/danoeh/antennapod/
├── espresso/
│   ├── TC001_AppLaunchTest.kt                              ← Tianyu Yao (6 tests)
│   ├── TC002_SubscribePodcastTest.kt                       ← Tianyu Yao (4 tests)
│   ├── TC003_PlayEpisodeTest.kt                            ← Tianyu Yao (4 tests)
│   ├── TC004_QueueManagementTest.kt                        ← Tianyu Yao (4 tests)
│   ├── TC005_SearchDiscoveryTest.kt                        ← Tianyu Yao (4 tests)
│   ├── TC011_BrowseDiscoveryTest.kt                        ← Jianheng Sun (4 tests)
│   ├── TC012_SubscribeDiscoveryTest.kt                     ← Jianheng Sun (4 tests)
│   ├── TC013_UnsubscribeDeleteTest.kt                      ← Jianheng Sun (4 tests)
│   ├── TC021_PlayPauseControlsTest.kt                      ← Yuanbing Wang (4 tests)
│   ├── TC022_PlaybackSpeedAdjustmentTest.kt                ← Yuanbing Wang (4 tests)
│   ├── TC023_DownloadEpisodeForOfflinePlaybackTest.kt      ← Yuanbing Wang (4 tests)
│   ├── TC031_ThemeDisplaySettingsTest.kt                   ← Xintao Wang (4 tests)
│   └── TC032_StorageNetworkPreferencesTest.kt              ← Xintao Wang (4 tests)
├── uiautomator/
│   ├── TC006_OpmlImportTest.kt                             ← Tianyu Yao (3 tests)
│   ├── TC014_ShareFeedUrlTest.kt                           ← Jianheng Sun (3 tests)
│   ├── TC015_FeedRefreshTest.kt                            ← Jianheng Sun (3 tests)
│   ├── TC024_AudioFocusPlaybackNotificationTest.kt         ← Yuanbing Wang (3 tests)
│   ├── TC025_BackgroundPlaybackContinuityTest.kt           ← Yuanbing Wang (3 tests)
│   ├── TC033_RuntimePermissionHandlingTest.kt              ← Xintao Wang (3 tests)
│   └── TC034_NotificationChannelSettingsTest.kt            ← Xintao Wang (3 tests)
└── utils/
    └── TestHelper.kt                                       ← 共享工具类
```

---

## 7. 全项目测试分布（供参考）

| 测试方法 | TC 数 | 测试方法数 | 占比 |
|----------|-------|-----------|------|
| **Espresso（UI 自动化）** | **13** | **54** | **32.5%** |
| 单元测试（JUnit） | 8 | 132 | 20.0% |
| **UIAutomator（跨应用/系统 UI）** | **7** | **21** | **17.5%** |
| 集成测试（SQLite/DB） | 6 | 37 | 15.0% |
| Manual（手动探索） | 4 | 80 步骤 | 10.0% |
| Performance（性能基准） | 2 | 7 | 5.0% |
| **合计** | **40** | **251 自动化 + 80 手动** | **100%** |

---

## 8. 关键经验

| 类别 | 要点 |
|------|------|
| API 37 不兼容 | `InputManager.getInstance()` 在 API 37 中被移除——Espresso 测试必须使用 API ≤ 34 的模拟器 |
| `ActivityScenarioRule` vs `ActivityTestRule` | `ActivityScenarioRule` 在 MuMu 上不可用——改用 `ActivityTestRule(false, false)` 手动启动 |
| Resource ID 歧义 | `R.id.toolbar` 在 View 层级中有 2 个匹配——改用唯一 ID 如 `R.id.appbar` |
| 空容器视图 | `subscriptions_grid` 无 Feed 时 `globalVisibleRect` 为空——改用 `R.id.swipeRefresh` |
| 底部导航隐藏项 | `bottom_navigation_addfeed` / `bottom_navigation_episodes` 不一定可见——使用 `bottom_navigation_more` |
| 截图持久化 | 测试 APK 卸载时截图会被删除——保存至 `/storage/emulated/0/Download/screenshots/` |
| UIAutomator 超时 | `Until.hasObject()` / `Until.findObject()` 需要显式超时（5000ms+）以等待系统 UI 转场 |

---

## 9. 结论

**系统测试 100% 完成。** 4 名成员在 4 个 Sprint 中共交付 20 个系统测试 TC（13 Espresso + 7 UIAutomator）、75 个测试方法，均全部通过。Espresso 覆盖应用内 UI 流程：启动（6）、订阅管理（12）、播放控件（12）、设置导航（8）。UIAutomator 验证跨应用交互：文件选择器（3）、分享/刷新（6）、通知/音频焦点（6）、权限/通道管理（6）。测试在 MuMu 模拟器（Android 12）和 Pixel_7 AVD（API 37）上运行，根据框架兼容性要求选择设备。
