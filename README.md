# weihuda!lazer

湖南大学微生活的 Android 桌面小组件库。

## 现在能做什么

- **今日课程小组件**（2×2 / 4×2 / 4×3 / 4×4）：周次、「即将开始 / 上课中 / 已结束」状态、
  今日重点课程、手动刷新，15 分钟周期刷新，失败时保留当天上一次成功的数据。
- **登录校园账号**：学号 + 密码，密码用 RSA 加密后交给后端校验，本机只保存 Keystore 加密后的令牌。
  未登录时首页显示「未登录 / 登录校园账号以使用所有组件」，桌面卡片同样只给登录引导——
  应用里没有演示数据。
- **小组件库首页**：按课程与考试 / 校园生活 / 体测分组，已实现的卡片可进入设置，规划中的置灰。
- **应用内预览**：三种尺寸与桌面共用同一个 presenter，看到的就是桌面上的样子。

## 构建

```bash
./gradlew :app:assembleDebug
```

工具链固定在 Gradle 9.9.0 + AGP 9.2.1 + Kotlin 2.4.21 + JDK 21 + compileSdk 36，
详见 [AGENTS.md](./AGENTS.md)。

## 文档

- [ARCHITECTURE.md](./ARCHITECTURE.md)：分层、刷新与登录链路、快照与渲染的边界
- [docs/roadmap.md](./docs/roadmap.md)：已完成与下一步
- [CONTRIBUTING.md](./CONTRIBUTING.md)：真机验证清单

## 许可

以 **MIT 或 Apache-2.0** 双许可发布，任选其一：

- [LICENSE-MIT](./LICENSE-MIT)
- [LICENSE-APACHE](./LICENSE-APACHE)

除你明确声明的部分外，你提交的贡献按惯例视为同时采用上述两种许可。
