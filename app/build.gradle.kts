// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 调试期用的令牌，不进仓库、不进 release。只从环境变量读，仓库里不写任何本机路径：
// WEIHUDA_DEBUG_JWT 直接给令牌，WEIHUDA_DEBUG_JWT_FILE 给存令牌的文件。
// 都没设置就是空串，等同于没配置。
val debugJwt: String = run {
	val direct = System.getenv("WEIHUDA_DEBUG_JWT")?.trim().orEmpty()
	if (direct.isNotEmpty()) {
		direct
	} else {
		System.getenv("WEIHUDA_DEBUG_JWT_FILE")
			?.takeIf { it.isNotBlank() }
			?.let(::File)
			?.takeIf { it.isFile }
			?.readText()
			?.trim()
			.orEmpty()
	}
}

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "cn.qnxg.weihuda.lazer"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()
	// 只装了 android-36.1 的环境（如 Android 16 QPR2 预览）可在本地 gradle.properties 里
	// 设置 compileSdkMinor=1、buildToolsVersion=36.1.0，仓库默认按标准 android-36 构建。
	(findProperty("compileSdkMinor") as String?)?.let { compileSdkMinor = it.toInt() }
	(findProperty("buildToolsVersion") as String?)?.let { buildToolsVersion = it }

	defaultConfig {
		applicationId = "cn.qnxg.weihuda.lazer"
		minSdk = libs.versions.androidMinSdk.get().toInt()
		targetSdk = libs.versions.androidTargetSdk.get().toInt()
		versionCode = 1
		versionName = "0.2.0"
		// release 恒为空串：构建产物里不嵌任何令牌。debug 在下面覆盖。
		buildConfigField("String", "DEBUG_JWT", "\"\"")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
		debug {
			buildConfigField("String", "DEBUG_JWT", "\"$debugJwt\"")
		}
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

// app 只有界面与组合根：不直接依赖任何 data 模块，也不认识 ktor / Glance。
// 需要数据时通过 :widget 的门面（CourseWidgetComponent）拿。
dependencies {
	implementation(project(":core"))
	implementation(project(":widget"))

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.kotlinx.coroutines.android)

	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui.tooling.preview)
	debugImplementation(libs.androidx.compose.ui.tooling)
}
