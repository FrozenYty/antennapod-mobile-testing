# AntennaPod 移动测试项目 — 手动/探索性测试进度总结

**项目**: [antennapod-mobile-testing](https://github.com/FrozenYty/antennapod-mobile-testing)
**被测应用**: [AntennaPod](https://github.com/AntennaPod/AntennaPod) v3.12.0-beta1
**日期**: 2026-06-11

---

## 1. 总体进度

| 指标 | 数值 |
|------|------|
| 手动测试 TC 总数 | **4** |
| 手动测试步骤总数 | **80** |
| 可执行步骤 | **53** |
| 通过 | **52** |
| 部分通过 | **1** |
| 失败 | **0** |
| N/A（环境限制） | **27** |
| 通过率（可执行） | **98.1%** |
| 涉及成员 | 4 人 |
| Sprint 跨度 | Sprint 1 – Sprint 4 |

---

## 2. 分成员贡献明细

### 2.1 Tianyu Yao（组长）— Sprint 1：Core Foundation

| TC-ID | 标题 | 步骤数 | 通过 | 部分 | N/A | 状态 |
|-------|------|--------|------|------|-----|------|
| TC-010 | First-Launch User Flow | 20 | 19 | 1 | 0 | ✅ 95% |
| **小计** | | **20** | **19** | **1** | **0** | |

**测试范围**：
- 从干净状态的完整首次启动引导流程
- 底部导航验证、通过 RSS URL 订阅 Feed
- Episode 发现、播放启动、Mini-player 交互
- 设备旋转（横屏/竖屏）、返回导航、退出/重启
- 跨重启状态持久化

**执行详情**：
- 设备：MuMu 模拟器（ALN-AL00，Android 12）
- 日期：2026-05-28
- 使用 Feed：`https://feeds.npr.org/500005/podcast.xml`（NPR News Now）
- 部分通过（步骤 14）：流媒体播放失败——NPR 重定向到 Spotify CDN，模拟器无法访问；应用正确显示错误对话框

---

### 2.2 Jianheng Sun — Sprint 2：Subscription & Discovery

| TC-ID | 标题 | 步骤数 | 通过 | 部分 | N/A | 状态 |
|-------|------|--------|------|------|-----|------|
| TC-020 | Discovery Page Usability | 20 | 16 | 0 | 4 | ✅ 100%（可执行） |
| **小计** | | **20** | **16** | **0** | **4** | |

**测试范围**：
- 发现与订阅端到端工作流
- 底部导航、More 菜单弹窗、Add Podcast 页面
- RSS URL 搜索、Feed 预览、订阅确认
- Episode 列表导航、多选模式、旋转

**执行详情**：
- 设备：test33 AVD，1080x2400
- 日期：2026-06-01
- 使用 Feed：`https://feeds.npr.org/500005/podcast.xml`（NPR News Now）
- N/A（步骤 16-17）：多选需要多个订阅——全新安装仅一个
- N/A（步骤 19-20）：AVD 通过 adb 旋转不可靠
- 采集 4 张截图

---

### 2.3 Yuanbing Wang — Sprint 3：Playback & Downloads

| TC-ID | 标题 | 步骤数 | 通过 | 部分 | N/A | 状态 |
|-------|------|--------|------|------|-----|------|
| TC-030 | Long Playback Stability | 20 | 0 | 0 | 20 | ⚠️ N/A（模拟器） |
| **小计** | | **20** | **0** | **0** | **20** | |

**测试范围**：
- 长时间播放（连续 5+ 分钟，总计 10+ 分钟）
- Seek 操作（前进拖动、回退按钮）
- 后台播放（Home 键）、通知栏控件
- 播放速度切换（1.0x ↔ 1.5x）
- 外设变更（有线耳机、蓝牙连接/断开）
- 睡眠定时器、来电中断、锁屏播放

**执行详情**：
- 设备：MuMu 模拟器（ALN-AL00，Android 12）
- 日期：2026-06-03
- 全部 20 步 N/A——模拟器缺少：已下载 Episode、蓝牙、耳机插孔、来电模拟
- 播放逻辑已由单元测试（TC-026, TC-027：21/21）和集成测试（TC-028, TC-029：13/13）验证
- 建议：在有预下载内容的物理设备上执行

---

### 2.4 Xintao Wang — Sprint 4：Settings & System

| TC-ID | 标题 | 步骤数 | 通过 | 部分 | N/A | 状态 |
|-------|------|--------|------|------|-----|------|
| TC-040 | Accessibility & Edge Cases | 20 | 17 | 0 | 3 | ✅ 100%（可执行） |
| **小计** | | **20** | **17** | **0** | **3** | |

**测试范围**：
- 默认和大字体下的无障碍性
- 屏幕阅读器（TalkBack）导航
- 主题切换（浅色/深色/全黑）
- 设置导航深度（嵌套屏幕、返回导航）
- 断网恢复力、视觉对比度评估
- 设置页面旋转

**执行详情**：
- 设备：MuMu 模拟器（ALN-AL00，Android 12）
- 日期：2026-06-03
- N/A（步骤 3）：MuMu 模拟器无 TalkBack
- 字体大小测试为系统默认 130%
- 所有视图可见性已由自动化测试（TC-031–034）验证
- 建议：在物理设备上进行 TalkBack 和极端字体大小测试

---

## 3. 贡献对比一览

| 成员 | Sprint | 模块 | TC 数 | 步骤数 | 可执行 | 通过 | 占比（步骤） |
|------|--------|------|-------|--------|--------|------|-------------|
| **Tianyu Yao** | 1 | Core Foundation | 1 | **20** | 20 | 19 + 1 部分 | 25% |
| **Jianheng Sun** | 2 | Subscription & Discovery | 1 | **20** | 16 | 16 | 25% |
| **Yuanbing Wang** | 3 | Playback & Downloads | 1 | **20** | 0 | 0（全 N/A） | 25% |
| **Xintao Wang** | 4 | Settings & System | 1 | **20** | 17 | 17 | 25% |
| **合计** | | | **4** | **80** | **53** | **52 + 1 部分** | **100%** |

```
Tianyu Yao      ████████████████████████████████████████ 19 通过 + 1 部分 (20 步)
Jianheng Sun    █████████████████████████████████        16 通过 + 4 N/A  (20 步)
Yuanbing Wang   ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  0 通过 + 20 N/A (20 步)
Xintao Wang     ██████████████████████████████████████   17 通过 + 3 N/A  (20 步)
```

---

## 4. N/A 原因分布

| 原因 | 受影响步骤数 | TC |
|------|-------------|-----|
| 模拟器缺少已下载内容 + 外设 | 20 | TC-030 |
| AVD 旋转通过 adb 不可靠 | 2 | TC-020 |
| 多选需多个订阅 | 2 | TC-020 |
| MuMu 无 TalkBack | 3 | TC-040 |
| **N/A 总计** | **27** | |

---

## 5. 测试执行环境

| 字段 | 取值 |
|------|------|
| 主设备 | MuMu 模拟器（ALN-AL00，Android 12，x86_64） |
| 辅助设备 | test33 AVD（1080x2400） |
| 构建类型 | playDebug |
| 动画状态 | 已禁用（window/transition/animator scales = 0.0） |
| 初始状态 | 每个 TC 前执行 `adb shell pm clear de.danoeh.antennapod.debug` |
| 截图存储 | `/storage/emulated/0/Download/screenshots/` 及 `screenshots/` 目录 |

---

## 6. 文件清单

```
app-under-test/antennapod/app/src/test/java/de/danoeh/antennapod/
└── manual/
    ├── TC010_FirstLaunchUserFlowTest.kt          ← Tianyu Yao (20 步)
    ├── TC020_DiscoveryUsabilityTest.kt           ← Jianheng Sun (20 步)
    ├── TC030_LongPlaybackStabilityTest.kt        ← Yuanbing Wang (20 步)
    └── TC040_AccessibilityEdgeCasesTest.kt       ← Xintao Wang (20 步)

test-results/
└── manual-test-result.md                         ← 全部 4 个 TC 的执行记录
```

---

## 7. 全项目测试分布（供参考）

| 测试方法 | TC 数 | 测试方法数 | 占比 |
|----------|-------|-----------|------|
| Espresso（UI 自动化） | 13 | 54 | 32.5% |
| 单元测试（JUnit） | 8 | 132 | 20.0% |
| UIAutomator（跨应用/系统 UI） | 7 | 21 | 17.5% |
| 集成测试（SQLite/DB） | 6 | 37 | 15.0% |
| **手动/探索性测试** | **4** | **80 步骤** | **10.0%** |
| 性能测试（基准） | 2 | 7 | 5.0% |
| **合计** | **40** | **251 自动化 + 80 手动** | **100%** |

---

## 8. 关键经验

| 类别 | 要点 |
|------|------|
| 模拟器限制 | MuMu 缺少蓝牙、耳机路由、来电模拟——TC-030 全部 N/A |
| CDN 屏蔽 | NPR Feed 重定向到 `prfx.byspotify.com`，模拟器不可达——已确认应用优雅显示错误对话框 |
| 自动化互补 | 手动 TC-030 逻辑已由单元/集成测试验证（TC-026–029，34/34 通过） |
| 截图策略 | 保存至 `/storage/emulated/0/Download/screenshots/` 以在 APK 卸载后存活 |
| 多选前置条件 | 测试多选需至少 2 个订阅——测试前需预置数据 |
| 模拟器无 TalkBack | 屏幕阅读器功能不可用——需物理设备进行无障碍审计 |
| 自动化交叉验证 | 已由 Espresso/UIAutomator TC（TC-031–034）验证的步骤可减少手动回归负担 |

---

## 9. 结论

**手动测试 100% 完成（在可执行范围内）。** 4 名成员在 4 个 Sprint 中设计了 4 个手动测试 TC，共 80 步。53 个可执行步骤中 52 个通过、1 个部分通过（CDN 屏蔽，非应用 Bug）。27 个步骤因模拟器限制标记为 N/A（无外设、无 TalkBack、无已下载内容）。核心用户流程——首次启动、发现/订阅、设置/无障碍——已验证正常工作。TC-030（长时间播放稳定性）需在物理设备上重新执行以获得完整覆盖；其逻辑已由 34 个通过的自动化测试支撑。
