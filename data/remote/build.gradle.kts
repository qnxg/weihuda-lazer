// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
}

// 后端接口的适配器：把 HTTP + JSON 翻译成 core 的实体与端口。
// 放在纯 JVM 模块里，所以它不能依赖 Android，也就不能被塞进 UI 逻辑。
kotlin {
	jvmToolchain(17)
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

dependencies {
	api(project(":core"))
	api(libs.ktor.client.core)
	api(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.test)
}
