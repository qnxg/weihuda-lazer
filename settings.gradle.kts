// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
pluginManagement {
	repositories {
		// 国内镜像优先，官方仓库兜底：Google Maven 在国内网络下经常握手失败
		maven("https://maven.aliyun.com/repository/google")
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		maven("https://maven.aliyun.com/repository/google")
		maven("https://maven.aliyun.com/repository/public")
		google()
		mavenCentral()
	}
}

rootProject.name = "weihuda-lazer"

// 依赖方向只能由编译器保证：core ← data:* ← app，core ← widget ← app。
// 详见 ARCHITECTURE.md 的「模块与依赖方向」。
include(":core")
include(":data:remote")
include(":data:local")
include(":widget")
include(":app")
