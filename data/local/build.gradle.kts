// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
}

// 本机存储的适配器：SharedPreferences、Keystore、快照编解码。
// 只实现 core 的端口，不反向依赖 widget/app。
android {
	namespace = "cn.qnxg.weihuda.data.local"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()
	(findProperty("compileSdkMinor") as String?)?.let { compileSdkMinor = it.toInt() }
	(findProperty("buildToolsVersion") as String?)?.let { buildToolsVersion = it }

	defaultConfig {
		minSdk = libs.versions.androidMinSdk.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

dependencies {
	api(project(":core"))
	implementation(libs.androidx.core.ktx)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.android)

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.test.junit)
}
