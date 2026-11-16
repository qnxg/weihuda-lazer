// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.jvm)
}

// core 只有实体、用例和端口：不碰 Android、HTTP、JSON。
// 任何 `import io.ktor.*` / `android.*` / `kotlinx.serialization.*` 出现在这里都是架构违规。
kotlin {
	jvmToolchain(17)
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

dependencies {
	testImplementation(libs.kotlinx.coroutines.core)
	testImplementation(libs.junit)
	testImplementation(libs.kotlin.test)
}
