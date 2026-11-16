// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.spotless)
}

// 文件头的事实来源：文案在 config/checkstyle/license-header.txt，规则只写在这里，
// 各模块不再重复声明；改完之后跑 ./gradlew spotlessApply。
private val licenseHeaderFile = rootProject.file("config/checkstyle/license-header.txt")

// kotlinGradle 没有内建 delimiter，而构建脚本首行不是 package，得自己给一个能命中所有脚本的正则。
private val scriptHeaderDelimiter = "(import |plugins |pluginManagement |dependencyResolutionManagement )"

fun SpotlessExtension.enforceLicenseHeader() {
	kotlin {
		// AGP 9 的 Kotlin 源码集不被 Spotless 识别，省略 target 会静默漏掉 src/main/kotlin。
		target("src/**/*.kt")
		licenseHeaderFile(licenseHeaderFile)
	}
	kotlinGradle {
		target("*.gradle.kts")
		licenseHeaderFile(licenseHeaderFile, scriptHeaderDelimiter)
	}
}

spotless {
	enforceLicenseHeader()
}

subprojects {
	apply(plugin = "com.diffplug.spotless")
	extensions.configure<SpotlessExtension> { enforceLicenseHeader() }
}
