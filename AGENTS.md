# Android 小组件开发约束

## 产品目标

- 面向湖南大学学生的桌面小组件，首个能力是「今日课程」，后续扩展考试、校园卡、电费等卡片。
- 小组件只做**只读摘要**：详情、设置、登录全部在应用内完成。
- 任何状态下都必须有内容：无课、刷新失败、未配置令牌都不能出现空白画布。

## 技术约束

- 工具链固定为 Gradle 9.3.0（wrapper）+ AGP 8.13.2 + Kotlin 2.4.21 + JDK 21 + compileSdk 36；
  升级任意一项都要先跑通 `:core:test`、`:app:lintDebug`、`:app:assembleDebug` 并在真机验证。
- 不要在某个模块里漏挂 `org.jetbrains.kotlin.plugin.compose`：Glance 模块自己写 `@Composable`，
  缺少 Compose 编译器插件会报 `Couldn't inline method call: CompositionLocal.get current`。
- Kotlin + Jetpack **Glance**（`androidx.glance:glance-appwidget`），不使用 Compose UI 写小组件，也不使用 WebView 截图方案。
- 小组件布局只使用 RemoteViews 允许的能力：`Text` / `Image` / `Box` / `Row` / `Column` / `Spacer` 与 `background`、`cornerRadius`、`clickable`。
- 刷新统一走 WorkManager：周期任务最短 15 分钟，`updatePeriodMillis` 恒为 0；手动刷新使用去重的一次性任务。
- 小组件进程**不发网络请求**，只读 `WidgetStore` 中的快照；刷新失败时保留当天上一次成功的数据。
- 令牌用 Android Keystore（AES-256-GCM）加密后存 SharedPreferences，不使用已废弃的 `androidx.security:security-crypto`。
- `core` 模块保持纯 JVM：不引入 Android API，保证可在 JVM 上单测，并为 KMP 迁移留出空间。

## 数据与接口

- 接口契约以 `weihuda_backend` 已实现的接口为准（`docs/openapi.yaml`），不跟随尚未落地的 Web 草稿契约。
- 鉴权头是裸 JWT：`Authorization: <token>`，没有 `Bearer` 前缀。
- `day` 语义：周一为 1 … 周日为 7（后端历史版本出现过 0 表示周日，读取时兜底）。
- `time` 是小节次 1…12；连续小节在展示前合并为一节课，课程名、课程代码、自定义 ID、教师、地点、班级任一不同都必须拆开。
- 周次计算：`floor((今天 - 学期开始日期) / 7) + 1`，一周从周日开始。
- 状态阈值：开课前 20 分钟内为「即将开始」，开始到结束为「上课中」，之后为「已结束」。

## 视觉

- 品牌色 `#318CCB`；页面背景 `#F4F6F9`；正文 `#1B2733`；次要文字 `#6A7883`；分割线 `#EDF1F5`。
- 状态色：上课中 `#2E7D32`，即将开始 `#E08600`，未开始 `#4A5560`，已结束 `#9AA5AF`，错误 `#D64545`。
- 圆角 18dp（小组件）/ 14dp（应用卡片），不使用重阴影；小组件为白底卡片。
- 状态不能只靠颜色表达，4×4 形态必须显示状态文字。
- 文本超长按字符截断（Glance 1.2 的 `Text` 没有 `overflow` 参数），完整信息保留在应用内。

## 质量门槛

- `core` 的周次、合并、状态、快照序列化必须有单元测试。
- 提交前运行 `./gradlew :core:test :app:assembleDebug`。
- 在真机上验证三种尺寸的渲染、刷新按钮、点击进入应用与失败态。
- 新增小组件形态时，同步更新 README 与本文档。
