# 路线图

## 已完成（0.1.0）

- 今日课程小组件（2×2 / 4×2 / 4×3 / 4×4）
- 学期周次、课程合并、状态计算与快照缓存
- WorkManager 15 分钟刷新 + 手动刷新 + 失败保留旧数据
- Keystore 加密令牌、演示数据模式
- 设置页、一键添加到桌面、应用内三尺寸预览

## 已完成（0.2.0 架构重构）

- 按 Clean Architecture 拆分为 `core` / `data:remote` / `data:local` / `widget` / `app`，
  依赖方向由 Gradle 模块图强制，详见 [ARCHITECTURE.md](../ARCHITECTURE.md)
- 应用改为**小组件库**结构：库列表 → 组件设置 → 账号，三屏各自独立，后续卡片按同一骨架接入
- 状态从快照移到渲染时计算，解决「快照里的『上课中』停留十五分钟」的问题
- 应用内预览与桌面共用同一个 presenter，文案不再有两套
- 测试按层铺开：实体/用例、接口契约、快照格式、presenter 文案

## 下一步

### 1. 打通 Android 登录（阻塞项）

后端目前只有微信 jscode 登录，Android 拿不到 code。可选方案：

- **A（推荐）**：新增 `POST /auth/login`：学号 + 前端 RSA 加密密码 + PoW，内部复用 `/bind` 已有的
  `CasToken::acquire_by_login` 校验，通过后签发 JWT；需要短信二次验证时先返回受限 `tfa_pending` 令牌，
  再用 `/tfa/send_sms` + `/tfa/verify` 换取正式令牌。
- **B**：给 `/auth-qrcode/info/{code}` 增加 JWT 下发，Android 端扫码登录（改动最小，但首次登录需要另一台设备）。
- C：手工粘贴 JWT（当前实现，仅用于调试）。

落地时的取值范围只到 `core/port` 新增一个 `AuthGateway` 与一个登录用例；
`app/ui/account` 的令牌输入框换成表单，`data:remote` 加接口实现，其余各层不动。

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
- 深色模式配色（应用与小组件各一套令牌）
- 单实例多小组件的独立设置（`GlanceStateDefinition` 存 per-widget 偏好）
- 仓库截图待重拍：`assets/*.png` 仍是 0.1.0 的单页设置界面
