# 参与开发

感谢您愿意改进 weihuda!lazer。我们对所有方式的贡献表示感谢。

## 修复 bug 或改进功能

通常情况下，直接着手处理这类事情并提交拉取请求即可。如果有相关的问题（issue），请确保在拉取请求描述中链接它，以便之后关闭该问题或链接以提供上下文。

## 实现新功能

建议先为任何类型的新功能开一个 issue，这样团队可以在任何实现工作开始之前提前告知该功能是否值得做。如果已经有一个功能 issue 开着，但不清楚它是否被视为已接受，请随时留言询问！

## AI 的使用

贡献中所有 AI 的使用都必须遵循 [AI 政策](./AI_POLICY.md)。

不遵循 AI 政策的贡献将被关闭。

## 开始之前

在开始之前，请阅读 README 并搜索现有的 issue。要快速了解项目的结构，请查看
[ARCHITECTURE.md](./ARCHITECTURE.md) 文档。您也可以加入我们的 QQ 群（1065050378）讨论开发相关话题。

我们推荐使用 Android
Studio 作为开发环境，您可以查阅相关文档以了解如何配置它。您可以参考下面的表格来了解开发环境要求。

| 依赖        | 版本                                                 |
| ----------- | ---------------------------------------------------- |
| JDK         | 21（最低 17）                                        |
| Gradle      | 9.9.0，wrapper 自带，不要用本机 Gradle               |
| AGP         | 9.2.1，上限由 Android Studio 决定                    |
| Kotlin      | 2.4.21，由 AGP 内置                                  |
| Android SDK | `platforms;android-36` + `build-tools;36.0.0` 或更高 |

配置好开发环境后就可以开始了：

```bash
git clone https://github.com/qnxg/weihuda-lazer.git
cd weihuda-lazer
./gradlew :app:assembleDebug
```

如果本机只装了 `android-36.1`（Android 16 QPR2），在 `~/.gradle/gradle.properties` 里加
`compileSdkMinor=1` 与 `buildToolsVersion=36.1.0` 即可；仓库本身仍按标准 `android-36` 构建。

## 检查

在提交拉取请求之前，请确保代码已格式化、通过 lint 检查，并且文档构建干净：

```bash
./gradlew spotlessCheck
./gradlew :core:test :data:remote:test :data:local:testDebugUnitTest :widget:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug
```

`.editorconfig` 已声明 Kotlin / KTS 用 **tab**
缩进，以便不同的开发者能选择他们喜欢的缩进长度，请注意编辑器的格式化设置。

## 文件头

每个 Kotlin 源文件与 Gradle 脚本都带三行文件头（项目名、版权、SPDX 标识）。文案只在
[`config/checkstyle/license-header.txt`](./config/checkstyle/license-header.txt)
里，由根 `build.gradle.kts` 的 Spotless 配置统一写入：

```bash
./gradlew spotlessApply   # 新建文件后补齐，或改完文案后重写全部文件头
./gradlew spotlessCheck   # CI 跑的检查，只报告不修改
```

不要手工改单个文件的文件头，也不要再引入第二个文件头工具（例如 hawkeye）：两套识别规则会互相覆盖，最后变成反复改头。

## 调试期注入令牌

你可以使用演示账号进行登录，但你也可以使用自己的令牌，以显示你自己的数据。

为了你的安全考虑，**请不要让令牌进入仓库**。

你可以使用这两种方式指定 JWT，它们都只在 Debug 构建中有效。：

1. **构建期**：用环境变量告诉 Gradle 令牌，再构建 debug 包。 使用 `WEIHUDA_DEBUG_JWT`
   指定令牌的值，或使用 `WEIHUDA_DEBUG_JWT_FILE` 指定一个包含令牌的文件 。在 Android
   Studio 里点运行时，把变量填进 Run Configuration 的 Environment variables。
2. **运行期**：使用以下命令以在不重新构建的情况下直接切换令牌：

   ```bash
   adb shell am start -n cn.qnxg.weihuda.lazer/.MainActivity -e weihuda_jwt "<token>"
   ```

## 测试

| 模块          | 位置                    | 测什么                                    |
| ------------- | ----------------------- | ----------------------------------------- |
| `core`        | `core/src/test/`        | 周次、小节合并、状态、用例编排与失败语义  |
| `data:remote` | `data/remote/src/test/` | 信封拆包、字段映射、错误翻译              |
| `data:local`  | `data/local/src/test/`  | 快照往返、损坏数据、未知版本              |
| `widget`      | `widget/src/test/`      | presenter 的文案、状态重算、边界时刻、空态 |
| `app`         | —                       | 界面只摆放 presenter 的输出，不写单元测试 |

## 版本与发布

待定
