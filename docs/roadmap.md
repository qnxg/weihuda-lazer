# 路线图

## 已完成（0.1.0）

- 今日课程小组件（2×2 / 4×2 / 4×3 / 4×4）
- 学期周次、课程合并、状态计算与快照缓存
- WorkManager 15 分钟刷新 + 手动刷新 + 失败保留旧数据
- Keystore 加密令牌
- 设置页、一键添加到桌面、应用内三尺寸预览

## 已完成（0.2.0 架构重构）

- 按 Clean Architecture 拆分为 `core` / `data:remote` / `data:local` / `widget` / `app`，
  依赖方向由 Gradle 模块图强制，详见 [ARCHITECTURE.md](../ARCHITECTURE.md)
- 应用改为**小组件库**结构：首页 → 登录 / 组件设置 → 应用设置，四屏各自独立，后续卡片按同一骨架接入
- 首页改为「使用者 + 两列卡片网格」：已登录显示头像、姓名与学院（取自后端），未登录显示 person 图标 +「未登录 / 登录校园账号以使用所有组件」，按课程 / 校园生活 / 体测分组，规划中的卡片置灰
- 登录页：学号 + 密码（可切换明文）、忘记密码跳统一认证的找回密码页
- 删除内置演示数据：未登录就是未登录，`TodayCourses.State` 用 `SIGNED_OUT` 表达，避免假课表被当成当天安排
- 状态从快照移到渲染时计算，解决「快照里的『上课中』停留十五分钟」的问题
- 应用内预览与桌面共用同一个 presenter，文案不再有两套
- 测试按层铺开：实体/用例、接口契约、快照格式、presenter 文案

## 下一步

### 1. 打通 Android 登录（已部分完成）

`core/port/AuthGateway` + `core/usecase/SignIn` 已经把「学号 + 密码 → 令牌」抽成端口与用例，
登录页与数据适配器都已接上，任意学号都会走真实教务校验。`data:remote` 当前的实现走的是后端的
调试通道（`POST /bind` + `GET /token?code=testing`，即 `weihuda_backend` 里 `code == "testing"`
的固定 openid），代价是**该学号在微信小程序里的绑定会被改写**，需要回小程序重新绑定才能恢复。

剩下的就是把实现换成正式登录：

- **A（推荐）**：后端新增 `POST /auth/login`：学号 + RSA 加密密码 + PoW，内部复用 `/bind` 已有的
  `CasToken::acquire_by_login` 校验，通过后签发 JWT；需要短信二次验证时先返回受限 `tfa_pending` 令牌，
  再用 `/auth/tfa/send_sms` + `/auth/tfa/verify` 换取正式令牌。客户端只需换掉 `AuthGateway` 的实现。
- **B**：给 `/auth-qrcode/info/{code}` 增加 JWT 下发，Android 端扫码登录（改动最小，但首次登录需要另一台设备）。

同时建议：JWT 目前 `validate_exp=false` 且没有撤销机制，长期驻留的设备令牌应改为
「可列出 + 可撤销 + 可续期」的形态，避免手机丢失后无法失效。

### 2. 降低请求放大

15 分钟 × 每个小组件 1~5 个接口，在用户量上涨后是明显浪费：

- 新增 `GET /widget/summary`：一次返回今日课表、倒计时、电费、网费、校园卡余额
- 支持 `ETag` / `If-None-Match` 与 `Cache-Control`，无变化时 304
- 按设备令牌限流

落地方式：`data:remote` 的 `WeihudaBackend` 增加方法，`core` 增加一个聚合用例，
各卡片仍只读自己的快照。

### 3. 更多小组件形态

- 考试安排、校园卡 / 电费 / 网费摘要
- 「下一节课」独立小尺寸卡片
- 明日课程：在 4×3 以上高度且今日课程较少时补位，减少留白

每加一张卡片按 [ARCHITECTURE.md 的「加一个新小组件」](../ARCHITECTURE.md#加一个新小组件) 逐层走一遍。

### 4. 体验细节

- 小组件选择器使用真实预览图（`previewImage` 或 `previewLayout`）
- 「添加到桌面」在 MIUI 上需要用户先允许「桌面快捷方式」权限（AppOps `INSTALL_SHORTCUT`，默认拒绝第三方），
  应用已在失败时给出入口；后续补上桌面厂商差异的检测与更精确的权限页深链
- 深色模式配色（应用与小组件各一套令牌）
- 单实例多小组件的独立设置（`GlanceStateDefinition` 存 per-widget 偏好）
- 仓库截图待重拍：`assets/*.png` 仍是 0.1.0 的单页设置界面
