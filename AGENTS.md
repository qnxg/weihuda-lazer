# weihuda!lazer - Agent Guide

开发前先阅读 [ARCHITECTURE.md](./ARCHITECTURE.md)，以便了解项目架构 ，开发时需要遵循它的规划。

本项目是面向湖南大学学生的桌面小组件库。

## 分层约束

依赖方向：`app → widget → data:* → core`。

- `core` 只放实体、端口和用例。**不允许** `import android.*`、
  `io.ktor.*`、`kotlinx.serialization.*`；`core` 的依赖表里也不该出现它们。
- 任何外部能力都从 `core/port/` 出去的接口进来；实现放在 `data:remote`（网络）或
  `data:local`（磁盘、Keystore）。
- 后端字段名只出现在 `data:remote/dto/` 与 `Mapping.kt`；`core.entity` 用领域词汇。
- `app` 不 import `io.ktor`、`androidx.glance`、`data.*`；它只认 `WidgetLibrary`（含各卡片门面）与
  `widget.presentation.*`。
- `widget` 除门面与 presenter 之外一律 `internal`（Android 按类名反射创建的 receiver / worker 除外，见
  ARCHITECTURE.md）；不要把 Glance 类型泄漏给 `app`。

## 技术约束

- 工具链固定为 Gradle 9.9.0（wrapper）+ AGP 9.2.1 + Kotlin 2.4.21 + JDK 21 + compileSdk
  36；AGP 上限由 Android Studio 决定（Studio 2026.1 → AGP 9.2.1），升 AGP 前先确认 IDE 是否支持；AGP
  9 起 Kotlin 由 AGP 内置，**不要**再加 `org.jetbrains.kotlin.android`
  插件，Compose 编译器插件仍要保留。升级任意一项都要先跑通全部测试与
  `:app:lintDebug`、`:app:assembleDebug`，并在真机验证。
- Kotlin / Gradle KTS 缩进使用 **tab**（`.editorconfig` 的
  `[*.{kt,kts}]`），提交前不要用 IDE 的“重排为空格”。
- 不要在某个模块里漏挂 `org.jetbrains.kotlin.plugin.compose`：Glance 模块自己写
  `@Composable`，缺少 Compose 编译器插件会报
  `Couldn't inline method call: CompositionLocal.get current`。
- Kotlin + Jetpack **Glance**（`androidx.glance:glance-appwidget`），不使用 Compose
  UI 写小组件，也不使用 WebView 截图方案。
- 小组件布局只使用 RemoteViews 允许的能力：`Text` / `Image` / `Box` / `Row` / `Column` / `Spacer` 与
  `background`、`cornerRadius`、`clickable`。
- 数据刷新统一走 WorkManager：周期任务最短 15 分钟，`updatePeriodMillis`
  恒为 0；手动刷新使用去重的一次性任务。课表与流量按 15 分钟，学期倒计时按天（学期日期一天之内不变）。状态切换走 `AlarmManager` 边界闹钟
  （开课前 20 分钟、开始、结束 + 1s、午夜），边界闹钟只重绘快照、不取数；
  Android 12+ 需要 `SCHEDULE_EXACT_ALARM`，未授予时退回 `setAndAllowWhileIdle`，可能晚几分钟。
- 小组件渲染路径**不发网络请求、不写磁盘**，只读 `SnapshotStore`
  里的快照；刷新失败时保留当天上一次成功的数据。
- 令牌用 Android Keystore（AES-256-GCM）加密后存 SharedPreferences，不使用已废弃的
  `androidx.security:security-crypto`；应用保持 `allowBackup="false"`。
- `core` 与 `data:remote` 保持纯 JVM，保证可在 JVM 上单测，并为 KMP 迁移留出空间。
- 许可为 `MIT OR Apache-2.0` 双许可；SPDX 文件头由根 `build.gradle.kts` 的
  Spotless 统一写入，文案在 `config/checkstyle/license-header.txt`。补头用
  `./gradlew spotlessApply`，不要手工改单个文件的头，也不要再引入 hawkeye
  之类的第二套头工具——两套识别规则会互相覆盖。

## 数据与接口

- 接口契约以 `weihuda_backend`
  已实现的接口为准（`docs/openapi.yaml`），不跟随尚未落地的 Web 草稿契约。
- 鉴权头是裸 JWT：`Authorization: <token>`，没有 `Bearer` 前缀。
- `/info/smester` 与 `/hdjw/class-start-date` 是公开接口（openapi 里 `security: []`），
  倒计时卡片因此不依赖登录；不要把令牌硬塞进不需要鉴权的请求。
- `day` 语义：周一为 1 … 周日为 7（后端历史版本出现过 0 表示周日，读取时兜底）。
- `time`
  是小节次 1…12；连续小节在展示前合并为一节课，课程名、课程代码、自定义 ID、教师、地点、班级任一不同都必须拆开。
- 周次计算：`floor((今天 - 学期开始日期) / 7) + 1`，一周从周日开始。
- 状态阈值：开课前 20 分钟内为「即将开始」，开始到结束为「上课中」，之后为「已结束」；状态在渲染时用当前时刻计算，**不要**写进快照。
- 应用内**不内置演示数据**：未登录就是空态，用 `TodayCourses.State.SIGNED_OUT` 表达，界面给登录引导；
  不要为了「装好即见效果」再塞一份假课表，假课会被当成当天安排。

## 视觉

- 品牌色 `#318CCB`；页面背景 `#F4F6F9`；正文 `#1B2733`；次要文字 `#6A7883`；分割线 `#EDF1F5`。
- 深色：卡片 `#1A222C`、正文 `#E6EDF3`、次要 `#93A1AE`、分割线 `#2B3542`、错误 `#FF7B72`；
  小组件品牌文字用 `#5FAEE3`，应用顶栏仍用 `#318CCB` 以保住白字；状态色相应提亮：
  上课中 `#6ECB73`，即将开始 `#F5A623`，未开始 `#8A97A3`，已结束 `#65717D`。
- 状态色：上课中 `#2E7D32`，即将开始 `#E08600`，未开始 `#4A5560`，已结束 `#9AA5AF`，错误 `#D64545`。
- 圆角 18dp（小组件）/ 14dp（应用卡片），不使用重阴影；小组件浅色白底、深色深灰蓝卡片。
- 状态不能只靠颜色表达，4×3 / 4×4 形态必须显示状态文字。
- 倒计时的天数面板与图标底片用 `brandSoft`（浅色 `#EDF4FB`、深色 `#22303C`），数字用 `brand`；
  它是固定色值而不是 alpha，避免启动器对半透明色的处理差异。
- 校园网卡片的进度条与「正常 / 无欠费」用 `WidgetPalette.success`（浅色 `#2E7D32`、深色 `#6ECB73`），
  进度槽复用分割线色。
- 课程行用「状态色左轨 + 时间竖排 + 名称 + `第 N 节 | 地点 | 教师`」；4×3 / 4×4 两行一节课，
  4×2 单行，2×2 只放聚焦的一节。
- 文本超长按字符截断（Glance 1.2 的 `Text` 没有 `overflow` 参数），完整信息保留在应用内。
- 颜色令牌有两份：`app/ui/theme/Theme.kt`（Compose 应用）与
  `widget/presentation/WidgetPalette.kt`（小组件，深浅成对，预览也读它）。加令牌时两处都要加。
- 应用与小组件都跟随系统深浅色：应用侧 `LazerColors` 通过 CompositionLocal 解析，小组件侧
  `WidgetPalette` 深浅成对。小组件用 Glance 的 day/night `ColorProvider`，API 31+ 由启动器即时解析，
  26–30 要等下一次组件更新；不要用 `ColorProvider(R.color.…)`，进程间解析不一致。

## 注释

- 注释应阐述“为什么”（why），而非“是什么”（what）：即核心意图、约束条件、被舍弃的方案，或是值得警示的后果。注意避免使用幼稚的语气，也不要过度解释。
- 如果注释只是在复述代码逻辑，那么该修改的其实是代码本身——更清晰的命名或更精简的函数远胜于注释；尽量通过优化代码减少注释产生。
- 文档注释应仅涵盖函数、模块或 crate 的使用方法，切勿涉及实现细节。
- 注意修正或删除不准确或具有误导性的注释。当注释与代码不一致时，应以代码为准。
- 注释应紧邻其所描述的代码，且两者之间的关联必须清晰明确。不要在局部注释中引用系统其他远端的内容，也不要复述显而易见的事实。
- 仅保留开发所必需的信息。切勿在注释中记录变更历史（这些信息应由提交记录或 PR 负责）、保留被注释掉的代码、在注释中署名，或仅仅因为某个元素存在就为其添加注释。
- 仅需为公开的 crate
  API 编写文档；无需为每个私有辅助函数都添加说明——一个命名得当的短函数本身就已足够说明问题。
- 文件头（如版权声明或 SPDX 标识）属于例外情况，这类注释并非针对代码逻辑本身；这类注释不手写，由 Spotless 生成，见「技术约束」。

## 质量门槛

- `core` 的周次、合并、状态、用例编排、失败语义必须有单元测试； `data:remote`
  的信封与映射、`data:local` 的快照往返、`widget` 的 presenter 文案同样要有。
- 提交前运行：

  ```bash
  ./gradlew spotlessCheck
  ./gradlew :core:test :data:remote:test :data:local:testDebugUnitTest :widget:testDebugUnitTest
  ./gradlew :app:lintDebug :app:assembleDebug
  ```

- 在真机上验证三种尺寸的渲染、刷新按钮、点击进入应用与失败态（见
  [CONTRIBUTING.md](./CONTRIBUTING.md)）。
- 新增小组件形态或新页面时，同步更新 [ARCHITECTURE.md](./ARCHITECTURE.md)、[README.md](./README.md)
  与本文件。
