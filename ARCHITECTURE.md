# 微生活小组件的架构

# 警告：该文档由 AI 生成，阅读时小心胃内容物涌出。

这个仓库是湖南大学「微生活」的 Android 桌面小组件客户端。产品形态决定了它的骨架：
**小组件的渲染必须永远有内容**，而内容只在后台的少数几个时刻才可能更新。所以取数与渲染被一条边界隔开——边界以内是课表规则，边界以外是 Android、HTTP 和 JSON 细节。

结构大致遵循 Clean Architecture（Robert C. Martin）的 Dependency Rule：**源码依赖只向内指**。 `core`
不认识 Android，`data:*` 不认识界面，`widget` 与 `app`
只通过门面互相看见。这条规则不靠自觉，靠 Gradle 模块图——想违规就得先改 `settings.gradle.kts`。

---

## 模块与依赖方向

```text
                    ┌──────────────┐
                    │     app      │  Compose 界面 + 组合根
                    └──────┬───────┘
                           │ 只依赖门面
                    ┌──────▼───────┐
                    │    widget    │  Glance 表现层 + WorkManager + 组件门面
                    └──┬────────┬──┘
             ┌─────────▼──┐  ┌──▼───────────┐
             │ data:remote│  │ data:local   │  适配器（HTTP / 存储）
             └─────────┬──┘  └──┬───────────┘
                       └────┬───┘
                    ┌───────▼───────┐
                    │     core      │  实体 + 用例 + 端口（纯 JVM）
                    └───────────────┘
```

| 模块          | 类型            | 放什么                                                                  | 明确不能出现的东西                                  |
| ------------- | --------------- | ----------------------------------------------------------------------- | --------------------------------------------------- |
| `core`        | Kotlin JVM      | 实体（`entity/`）、端口（`port/`）、用例（`usecase/`）                   | `android.*`、`io.ktor.*`、`kotlinx.serialization.*` |
| `data:remote` | Kotlin JVM      | 微生活后端适配器、DTO、信封拆包、字段映射                               | `android.*`、Glance、Compose                        |
| `data:local`  | Android library | SharedPreferences、Keystore、快照 JSON                                  | Glance、WorkManager、界面                           |
| `widget`      | Android library | presenter / 视图模型、Glance 视图、Worker、`WidgetLibrary` 与各卡片门面 | 直接暴露 Glance 类型给 `app`                        |
| `app`         | Android app     | Compose 界面、`AppState`、应用组合根                                    | `io.ktor.*`、直接依赖任何 `data:*`                  |

三条硬规则：

1. `core` 的 `import` 只有 `kotlin.*`、`java.time.*`、`cn.qnxg.weihuda.core.*`。想在实体里
   `@Serializable`，就是把 JSON 格式写进了内圈——那是 `data:local` 的事。
2. `app` 不依赖 `data:*`，也不认识 ktor。它只有一个入口：`WidgetLibrary`（应用级配置，下面挂着每张卡片的
   `CourseWidgetComponent` / `NetworkWidgetComponent` / `CampusCardWidgetComponent` / `ElectricityWidgetComponent`）。
3. `widget` 的公开面只有：`WidgetLibrary`（登录、设置、快照与 HTTP 的装配）与每张卡片的门面
   （`CourseWidgetComponent`、`NetworkWidgetComponent`、`CountdownWidgetComponent`、
   `CampusCardWidgetComponent`、`ElectricityWidgetComponent`）、`presentation.*`
   （视图模型、presenter、尺寸与颜色规则），以及 Android 必须能反射创建的类（`CourseWidget`、
   `NetworkWidget`、`CountdownWidget`、`CampusCardWidget`、`ElectricityWidget`、
   `CourseWidgetReceiver`、`NetworkWidgetReceiver`、`CountdownWidgetReceiver`、
   `CampusCardWidgetReceiver`、`ElectricityWidgetReceiver`、`PinResultReceiver`、
   `RefreshWorker`、`NetworkRefreshWorker`、`CountdownRefreshWorker`、`CampusCardRefreshWorker`、
   `ElectricityRefreshWorker`、`CountdownTickReceiver`、`RefreshAction`、`NetworkRefreshAction`
   ——它们由 `AndroidManifest.xml` / WorkManager / AlarmManager 按类名实例化，不能 `internal`）。
   其余一律 `internal`。

### 顶层目录为什么是「层」而不是「业务」

Clean Architecture 的 Screaming
Architecture 一节主张：新同事第一眼看到仓库时，应该看出这是「课表」而不是「Android 项目」。这个仓库的顶层是
`core` / `data` / `widget` / `app`，看起来更像层——但第二层开始就是这个系统的业务了：

```text
core/usecase/RefreshTodayCourses.kt      ← 这个应用会做的事
core/entity/CourseSession.kt             ← 这个应用关于世界的知识
data/remote/WeihudaBackend.kt            ← 数据从哪来
widget/presentation/CourseWidgetPresenter.kt ← 桌面上会显示什么
```

之所以不把 `today-courses/`
放到顶层：小组件库里的每一张卡片都要复用同一套实体、同一套快照与刷新机制、同一套设计令牌。按业务切顶层会让这些共享物无处安放，最后变成
`common/` 之类的垃圾桶。用可行性换来的做法是——**组件边界按业务切在 `widget` 内部**
（每张卡片一个 presenter + 视图 + Worker + 门面），实体与用例则按层共享。

### 为什么 `widget` 里既有表现层又有门面

按「package by component」把**功能**当成组件：`widget`
就是「课程小组件」这个组件，它对外是一个干净的接口，内部怎样分文件是它自己的事。组合根随之放在组件内，于是 Worker（后台入口）和应用内界面（前台入口）用的是同一份装配代码，不会出现「桌面刷新用一套适配器、应用内刷新用另一套」这种只在真机上才暴露的偏差。

`app` 于是退化成两层：界面（`ui/`）与它自己的组合根（`LazerApplication` +
`MainActivity`）。这正是 Clean
Architecture 里 Main 的位置——**最脏的一层**，所有具体实现的名字只在这里出现。

界面只有四屏：`ui/home`（使用者与卡片网格）、`ui/login`（学号 + 密码换令牌）、
`ui/course`（单个卡片的设置与预览）、`ui/settings`（账号状态、后端地址与版本，所有卡片共用）。
令牌与后端地址属于应用级配置，只在这里改一次；卡片设置页只放该卡片自己的选项。

「添加到桌面」走 `AppWidgetManager.requestPinAppWidget`，但它的返回值只表示请求已交给桌面。
MIUI 桌面的 `AddItemActivity` 会检查 AppOps `INSTALL_SHORTCUT`（用户界面里叫「桌面快捷方式」），
默认对第三方应用是拒绝：它拉起后立刻以 `has no permission` 自行结束，既不弹窗也不回调。
所以结果用「实例数是否增加」复核，失败时提示去应用设置里放开这个权限，并留一条长按桌面的手动路径。

---

## 一次登录走过的路

```text
LoginScreen（学号 + 密码）
        │
        ▼
SignInInteractor.signIn()                          用例：校验、落盘
        ├── AuthGateway.signIn()                    端口 → data:remote
        │     ├── GET  /bind/pow?stuId=             PoW 票据（难度由服务端下发）
        │     ├── sha256("{ticket}:{answer}")       实体之外的纯计算，在 Dispatchers.Default 上跑
        │     ├── POST /bind                        密码 RSA-2048/PKCS#1 v1.5 加密后提交
        │     └── GET  /token?code=                 换 JWT
        └── SettingsStore.save()                    端口 → data:local（Keystore 加密令牌）
        │
        ▼
AppState：回到首页并立刻刷新
```

密码只在这一次请求里出现，不落盘；本机只存令牌。未登录时 `TodayCourses.State` 是
`SIGNED_OUT`，桌面与应用内都不展示任何课表，只给登录引导。

登录用的 `code` 走的是后端的调试通道（`code=testing`）。副作用是该学号的 openid 会被改写到
固定值上，小程序侧的绑定随之失效——后端补上 `POST /auth/login` 后换掉 `AuthGateway` 的实现即可，
界面与用例不用动。

---

## 一次刷新走过的路

```text
Glance 点击 / WorkManager / 应用内按钮
        │
        ▼
WidgetLibrary.courseWidget.refresh()                组合根：按当前设置装配
        │
        ▼
RefreshTodayCoursesInteractor.refresh()             用例：编排
        ├── SettingsStore.load()                    端口 → data:local（无令牌即输出 SIGNED_OUT）
        ├── SnapshotStore.readTodayCourses()        端口 → data:local
        ├── SemesterGateway.current()               端口 → data:remote
        ├── CourseGateway.timetable()               端口 → data:remote
        ├── StudentGateway.current()（失败可容忍）   端口 → data:remote
        ├── Course.sessionsOn()                     实体：按周次过滤 + 合并连续小节
        └── SnapshotStore.writeTodayCourses()       端口 → data:local
        │
        ▼
TodayCourses（输出数据）
        │
        ├───────────────► CourseWidget().updateAll()      桌面重绘
        └───────────────► AppState.snapshot               应用内展示
                                 │
                                 ▼
                     CourseWidgetPresenter.present(snapshot, now)
                                 │
                                 ▼
                     CourseWidgetView（全部是字符串与状态）
                                 │
                    ┌────────────┴────────────┐
                    ▼                         ▼
              WidgetContent（Glance）   CourseWidgetPreview（Compose）
```

「校园网流量」卡片走同一条路：组合根换成 `WidgetLibrary.networkWidget`，用例换成
`RefreshNetworkTrafficInteractor`（`GET /netflow`），视图换成 `NetworkWidgetPresenter` / `NetworkWidgetView`。
差别只在快照的有效期：课表按天，流量按月。

「学期倒计时」卡片是这条路的第三种变体：用例换成 `RefreshSemesterCountdownInteractor`
（`GET /info/smester`，**公开接口，不需要令牌**），所以它没有 `SIGNED_OUT` 态——未登录也有内容。
快照里存的是学期的绝对日期（开学日、假期开始日、下学期开学日），**存不下「还有几天」**：
阶段与天数由 `Semester.countdownOn(today)` 在渲染时算，跨天由午夜闹钟重绘，取数则按天一次。

「校园卡余额」与「宿舍电量」是两张只有 2×2 的单值卡片，走的是同一条路再简化一层：
`CampusCardWidgetComponent` + `RefreshCardBalanceInteractor`（`GET /pt/card-info`）、
`ElectricityWidgetComponent` + `RefreshElectricityBalanceInteractor`（`GET /electricity`）。
两者的信息量相同（一个数字），所以共用 `MetricCardView` / `MetricCardContent` 这一套视图，
presenter 各自负责货币符号与「度」的写法。这两个用例失败时保留上一次成功的读数、不设时间窗口：
余额与电量是存量，过期的读数仍然是一个真实存在过的数字，卡片上的「刷新失败」已经说明它不是刚取的。

边界上的数据形状是分开的，三处都不共用类型：

| 边界                    | 类型                 | 谁定义                | 备注                                             |
| ----------------------- | -------------------- | --------------------- | ------------------------------------------------ |
| 后端 JSON → 适配器      | `*Dto`               | `data:remote`         | 只有适配器认识 `courseName` / `stuId` 这类字段名 |
| 适配器 → 用例           | `Course`、`Semester` | `core.entity`         | 领域词汇；后端改名不影响它                       |
| 用例 → 外界（输出数据） | `TodayCourses`       | `core.usecase`        | 已合并、已过滤，仍无任何格式化字符串             |
| 适配器 → 用例（流量）   | `CampusNetwork`      | `core.entity`         | GB、元、0..1 的比例，不含文案                    |
| 用例 → 外界（流量）     | `NetworkTraffic`     | `core.usecase`        | 三态 + 账户数字；格式化留给 presenter            |
| 适配器 → 用例（余额）   | `CampusCard`         | `core.entity`         | 只有余额（元），卡号与流水属于账单页             |
| 适配器 → 用例（电量）   | `DormElectricity`    | `core.entity`         | 只有度数；后端给的是「186.50度」这种字符串       |
| 用例 → 外界（两张卡片） | `CardBalance`、`ElectricityBalance` | `core.usecase` | 三态 + 数字；格式化留给 presenter |
| 落盘                    | `SnapshotDto`        | `data:local`          | 带 `version`，格式演进不惊动内圈；两张卡片各一份 |
| 用例 → 视图             | `CourseWidgetView`   | `widget.presentation` | 文案、排序、状态都定好了；视图只摆放             |

---

## 快照：桌面永不空白的原因

小组件的渲染与取数是两个时间点。系统随时可能要求重绘（开机、换尺寸、换主题、被启动器回收后再拉起），而网络随时可能不通。如果渲染时去取数，桌面就会在等网络时空白。

于是取数结果写进**快照**（`SnapshotStore`），渲染只读快照：

- 没有快照 → 「正在获取课表」+ 提示点按进入应用，仍然是一张完整的卡片。
- 有快照 → 正常的课表。
- 刷新失败 → 保留**当天**上一次成功的数据，把失败原因写进快照，卡片上多一行提示。跨天就丢弃旧课表：昨天的课留在今天的卡片上是错误信息，比空白更糟。
- 没有登录 → 快照是 `SIGNED_OUT`，卡片显示「未登录 / 登录校园账号以使用所有组件」。
  应用里没有演示数据：空着比给一份假的课表诚实，也避免用户把假课当成当天安排。

快照按 `WidgetId` 分键存储（`today-courses`、`campus-network`、`semester-countdown`、`campus-card`、
`dorm-electricity`）。加一种新卡片就是加一个 id
和一组读写方法，而不是往同一份快照里塞字段——考试卡片与课程卡片的刷新节奏、数据来源、失败语义都不同，
塞在一起只会互相牵制。

有效期也随卡片而异：课表快照只在当天有效，跨天就丢弃；流量快照按月结算，所以刷新失败时保留的是
**同一个月**上一次成功的结果——上个月的数字留在卡片上会被当成这个月的用量；学期快照不按时间过期，
假期与开学都是绝对日期，旧数据最多是「不知道校历改了」，而阶段仍然按当前日期重算；余额与电量是存量，
旧读数不会因为时间流逝变成「另一个东西」，所以只要有过成功就留着，由「刷新失败」标出它不是刚取的。

`SnapshotDto.version`
让格式能演进：读到不认识的版本就当没有快照，桌面退回「正在获取课表」，而不是显示错位的数据或崩溃。

---

## 状态在渲染时算，不写进快照

`CourseSession`
**没有 status 字段**。状态（未开始 / 即将开始 / 上课中 / 已结束）取决于「看表的时刻」，而快照可能已经放了十五分钟：

```text
08:00  刷新写入快照        status 若写进快照 = 上课中
08:20  用户看向桌面        真实状态 = 上课中   ✓（碰巧一致）
09:45  用户看向桌面        真实状态 = 已结束   ✗ 快照里仍是「上课中」
```

所以 `statusAt(now)`
在 presenter 里现算。这也是「实体的规则」与「快照的数据」分开的直接好处：时间相关的判断没有机会被固化。

`TodayCourses.refreshedAt` 随快照保存，但不再上屏，也不参与状态判断；页脚只保留刷新失败提示。

## 状态在边界点重绘

只做到「渲染时现算」还不够：如果界面一直不重绘，状态就停在最后一次渲染，最多落后 15 分钟。
`WidgetStatusAlarm` 在下面这些时刻排一次 `AlarmManager` 闹钟：

- 开课前 20 分钟（未开始 → 即将开始）
- 开始时刻（即将开始 → 上课中）
- 结束时刻 + 1 秒（上课中 → 已结束；`statusAt` 在结束那一刻仍算上课中）
- 当天午夜（唤醒后交给刷新链路取新一天的快照）

闹钟到点只做两件事：让 `CourseWidgetReceiver` 重新渲染快照，然后按快照里的课程排下一次。
**不取数**，所以边界切换不依赖网络。`nextStatusBoundary` 是纯函数，边界顺序在 JVM 上测。

Android 12+ 需要 `SCHEDULE_EXACT_ALARM`（系统设置里的「闹钟与提醒」）。没有授予时退回
`setAndAllowWhileIdle`，系统省电策略可能把它推迟几分钟；`CourseSettingsScreen` 提供入口。
最后一个小组件被移除后取消闹钟。

---

## Humble Object：presenter / 视图

RemoteViews 无法在单元测试里断言，所以它必须是 humble 的： `WidgetContent` 只把 `CourseWidgetView`
里的字符串摆到屏幕上，不做判断。

判断全部在 `CourseWidgetPresenter`：

- 文案（`第 10 周` / `假期`、`周一 11/16`、`今日共 2 节`、`刷新失败 · …`、`第 1–2 节 | 地点 | 教师`）
- 顺序与聚焦（2×2 聚焦正在上或下一节，全部结束后退回最后一节）
- 状态（`statusAt(now)`）
- 空态文案（`今日无课` / `刷新失败` / `正在获取课表`）

应用内预览（`CourseWidgetPreview`）用的是**同一个 presenter**，只是画在 Compose 里。颜色也共用
`widget.presentation.WidgetPalette`（深浅成对），所以「预览和桌面说的不一样」这类问题不会出现，
而 presenter 的行为可以在 JVM 上测。

尺寸规则集中在 `WidgetLayout`：2×2 聚焦一节课，4×2 单行，4×3 两行一节课（行高随课程数在 32–48dp
之间撑开），4×4 用参考课表条目的 48dp 两行布局。截断长度、行数这类**与尺寸有关**的东西留在视图
（`WidgetContent` 里的 `clip`、按 `LocalSize` 决定行数）：那是 RemoteViews 的能力边界，不是业务规则。

深色不是另一套视图：应用侧 `LazerColors` 用 CompositionLocal 在 `LazerTheme` 里切换，
小组件侧 `WidgetPalette` 的每个令牌是 `(light, dark)`，Glance 交给 day/night `ColorProvider`，
Compose 预览用 `isSystemInDarkTheme()` 解析。API 31+ 的 RemoteViews 会同时携带两组色值，
系统换主题即刻生效；API 26–30 只能在下一次刷新时重算。

---

## 用例与失败语义

| 用例                     | 输入                     | 输出                         | 失败时                                |
| ------------------------ | ------------------------ | ---------------------------- | ------------------------------------- |
| `RefreshTodayCourses`    | 无（读设置）             | `TodayCourses` 并写入快照    | 返回带 `failure` 的输出，**不抛异常** |
| `RefreshNetworkTraffic`  | 无（读设置）             | `NetworkTraffic` 并写入快照  | 同上，保留同月的旧数据                |
| `RefreshSemesterCountdown` | 无（公开接口，不读设置） | `SemesterCountdown` 并写入快照 | 同上，保留上一次的学期日期            |

失败不走异常，因为调用方是后台任务：抛出去只会变成 WorkManager 的重试风暴，而用户看到的东西没有区别。具体的失败语义：

- 学期 / 课表请求失败 → 整次刷新失败，保留当天旧快照。
- 学生信息失败 → **不算失败**，只是没有名字（标题里那点装饰不值得让整次刷新失败）。
- `CancellationException` 一律重新抛出，绝不吞掉：吞掉会让取消失效。

用例只依赖 `Clock`（`CampusClock.clock()`），所以周次、日期、状态都能用固定时钟测。

---

## 刷新与排期

**数据刷新**统一走 WorkManager，`updatePeriodMillis` 恒为 `0`；**状态重绘**走 AlarmManager：

| 场景               | 任务                                  | 策略                                |
| ------------------ | ------------------------------------- | ----------------------------------- |
| 周期刷新           | `PeriodicWorkRequest` 15 分钟 / 1 天  | `KEEP`→`UPDATE`，网络约束，指数退避 |
| 点卡片「刷新」     | 一次性任务                            | `enqueueUniqueWork(REPLACE)` 去重   |
| 应用启动           | 一次性任务                            | 同上，保证「装好就有内容」          |
| 状态 / 日期边界    | `AlarmManager` 单次闹钟               | 精确优先，只重绘；跨天时触发刷新    |
| 最后一个实例被移除 | `cancelAll` + `cancel`                | 停止周期任务、状态闹钟与日期闹钟    |

周期任务默认 15 分钟；学期倒计时按**天**，因为学期日期一天之内不会变，15 分钟一次的轮询只会放大请求。
它的天数跳动交给 `CountdownDayAlarm`：校区时区的午夜排一次闹钟，到点只重绘快照——快的填充宽度、
`还有 47 天` 这类数字都不需要新数据。

Worker 只做一件事：交给对应卡片的门面刷新（`WidgetLibrary.of(context).courseWidget.refresh()` /
`.networkWidget.refresh()` / `.campusCardWidget.refresh()` / `.electricityWidget.refresh()`）。
它自己不装配适配器、不发请求、不清洗错误——那些都发生在用例与组件门面里。

四张卡片各自持有一个周期任务与一个一次性任务（`weihuda-course-refresh[-now]` /
`weihuda-network-refresh[-now]` / `weihuda-card-refresh[-now]` / `weihuda-electricity-refresh[-now]`），
排期策略只在 `WidgetScheduling` 里写一份：卡片之间的差别只是 worker 与任务名，改间隔时不会漏掉某一张。
唯一与别处不同的是电量：后端按「园区 + 楼栋 + 房间」缓存四小时，适配器传 `refresh=0` 用缓存，
十五分钟一次的后台刷新不会每回都去敲学校的电费系统。

---

## 安全与隐私

- 令牌用 Android Keystore 的 AES-256-GCM 加密后再存 SharedPreferences（不用已废弃的
  `androidx.security:security-crypto`）。
- `android:allowBackup="false"`：加密后的密文与密钥都不应离开本机。
- 鉴权头是**裸 JWT**（`Authorization: <token>`，没有 `Bearer` 前缀），与后端 `openapi.yaml` 一致。
- 小组件渲染路径**不发网络请求**，只读快照：桌面渲染进程拿不到令牌。
- 学期倒计时用的 `/info/smester` 在 openapi 里是 `security: []`，所以这张卡片未登录也有内容；
  令牌只出现在必须鉴权的接口上（课表、流量、学生信息）。
- 加解密失败返回空令牌 → 用例退回演示数据，不会崩在启动路径上。

详见 [SECURITY.md](./SECURITY.md)。

---

## 加一个新小组件

以「考试安排」为例，按依赖方向从内往外加：

1. `core/entity/`：`Exam` 等实体，以及需要的规则（是否临近、状态）；
2. `core/port/`：缺什么能力就加一个端口（例如 `ExamGateway`）；
3. `core/usecase/`：`RefreshExams` 输入端口 + `Exams` 输出数据 + 交互器，用已有的
   `SnapshotStore`（新方法 + 新 `WidgetId`）；
4. `data:remote`：DTO + 映射 + 实现 `ExamGateway`；
5. `data:local`：`SnapshotStore` 的新方法 + `SnapshotDto`（新 format 版本）；
6. `widget/`：`ExamWidget`（Glance）、`ExamWidgetReceiver`、`ExamPresenter` / `ExamView`、
   `ExamRefreshWorker`（排期复用 `WidgetScheduling`，只给一个任务名）；门面加 `ExamWidgetComponent`，
   并挂到 `WidgetLibrary` 上；
7. `app/ui/`：首页 grid 里把卡片从「规划中」改成可点，加它的设置页与预览（文案来自同一个 presenter）；
8. 文档：本文件、`README.md`、`AGENTS.md` 的「已实现的卡片」都要加。

漏掉任何一层，编译器会告诉你（端口没实现、方法不存在）；漏掉文档不会——所以这一步靠 review 守住。

## 加一个新接口

1. `core/port/` 里加方法或新端口，只在 `core.entity` 的词汇里描述；
2. `data:remote/dto/` 加 DTO，`Mapping.kt` 加转换，`WeihudaBackend` 实现它；
3. `data:remote/src/test/` 加一条契约测试（信封、字段映射、失败翻译）；
4. 用例里决定失败语义：整次失败，还是像学生信息那样可以容忍。

接口契约以 `weihuda_backend` 的 `docs/openapi.yaml` 为准，不跟随尚未落地的 Web 草稿契约。

---

## 测试策略

测试按「能被骗过的层次」分配，不是按文件配比：

| 位置          | 测什么                                       | 现在的测试                                 |
| ------------- | -------------------------------------------- | ------------------------------------------ |
| `core`        | 周次、小节合并、状态、失败快照语义、用例编排 | `TimetableTest`、`RefreshTodayCoursesTest`、`RefreshNetworkTrafficTest`、`SemesterCountdownTest`、`RefreshSemesterCountdownTest`、`RefreshCardBalanceTest`、`RefreshElectricityBalanceTest` |
| `data:remote` | 信封拆包、字段映射、错误翻译                 | `ApiContractTest`                          |
| `data:local`  | 快照往返、损坏数据、版本不认                 | `SnapshotJsonTest`、`NetworkSnapshotJsonTest`、`CountdownSnapshotJsonTest`、`CardSnapshotJsonTest`、`ElectricitySnapshotJsonTest` |
| `widget`      | presenter 的文案、状态重算、空态、尺寸规则   | `CourseWidgetPresenterTest`、`NetworkWidgetPresenterTest`、`CountdownWidgetPresenterTest`、`CardBalancePresenterTest`、`ElectricityPresenterTest`、`MetricCardLayoutTest`、`WidgetLayoutTest`、`CountdownWidgetLayoutTest` |
| `app`         | 不写单元测试：界面只摆放 presenter 的输出    | —                                          |

用例测试全部用假适配器 + 固定时钟，不启 Android、不发 HTTP。视图（RemoteViews）不做自动化测试，真机验证见
[CONTRIBUTING.md](./CONTRIBUTING.md)。

---

## 构建与工具链

Gradle 9.9.0（wrapper）+ AGP 9.2.1 + Kotlin 2.4.21 + JDK 21 + compileSdk 36。AGP
9 起 Kotlin 由 AGP 内置，**不要**再加
`org.jetbrains.kotlin.android`；Compose 编译器插件仍要保留（Glance 与应用内 Compose 都自己写
`@Composable`）。

```bash
./gradlew :core:test :data:remote:test :data:local:testDebugUnitTest :widget:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug
```

---

## 决策记录

| 决策                                     | 原因                                                                           |
| ---------------------------------------- | ------------------------------------------------------------------------------ |
| 分段模块而不是单模块分层                 | 依赖方向由编译器执行；`core` 能在 JVM 上跑，KMP 迁移时直接平移                 |
| HTTP 引擎由调用方注入                    | `data:remote` 保持纯 JVM；引擎（Android / OkHttp / MockEngine）是宿主的细节    |
| 快照与渲染分离                           | 桌面在任何时刻都必须有内容；渲染路径不发请求，也拿不到令牌                     |
| 状态不写进快照                           | 状态取决于当前时刻，写进快照必然过期                                           |
| 失败返回带 `failure` 的输出              | 后台任务抛异常只会换来无意义的重试                                             |
| 组合根放在 `widget` 组件内               | 前台（应用）与后台（Worker）共用同一份装配，杜绝两套适配器                     |
| 不用 navigation-compose                  | 四屏、无深链接；一个 `when` 比一个导航图更短，换库也不影响下层                 |
| 不用 `androidx.security:security-crypto` | 该库已废弃；平台 Keystore API 足够，且少一个依赖                               |
| 放弃 WebView 截图方案                    | 小组件不可交互、每次刷新要冷启 WebView，Android 17 还有 widget bitmap 内存上限 |

---

## 名词对照

| 本项目                            | 后端 / 小程序            | 说明                                       |
| --------------------------------- | ------------------------ | ------------------------------------------ |
| `Course`                          | `class-table` 的一条记录 | 一小节，不是「一门课」                     |
| `CourseSession`                   | —                        | 合并连续小节后的「一节课」，小组件展示单元 |
| `Semester.startDate`              | `smester.startDate`      | 第一周的周日（后端字段名就是少一个 e）     |
| `Course.dayOfWeek`                | `day`                    | 1=周一…7=周日；后端历史版本用 0 表示周日   |
| `Course.period`                   | `time`                   | 小节次 1…12                                |
| `TodayCourses.State`              | —                        | `SIGNED_OUT` / `LOADED` / `FAILED`          |
| `CampusNetwork`                   | `GET /netflow`           | 本月已用、套餐额度、锁定状态与欠费          |
| `NetworkTraffic`                  | —                        | 流量卡片的快照内容，按月有效                |
| `Semester`                        | `GET /info/smester`      | 学年、学期、开学日；假期与下学期开学日是绝对日期 |
| `SemesterCountdown`               | —                        | 学期卡片的快照内容，按天有效                |
| `CampusCard`                      | `GET /pt/card-info`      | 校园卡余额（元）                            |
| `CardBalance`                     | —                        | 余额卡片的快照内容                          |
| `DormElectricity`                 | `GET /electricity`       | 剩余电量（度）；后端返回的是带单位的字符串  |
| `ElectricityBalance`              | —                        | 电量卡片的快照内容                          |
