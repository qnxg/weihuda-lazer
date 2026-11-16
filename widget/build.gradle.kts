import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.library)
	// Glance 是 Compose 风格 API，本模块自己写 @Composable，必须挂 Compose 编译器插件
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "cn.qnxg.weihuda.widget"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()
	// 只装了 android-36.1 的环境（如 Android 16 QPR2 预览）可在本地 gradle.properties 里
	// 设置 compileSdkMinor=1、buildToolsVersion=36.1.0，仓库默认按标准 android-36 构建。
	(findProperty("compileSdkMinor") as String?)?.let { compileSdkMinor = it.toInt() }
	(findProperty("buildToolsVersion") as String?)?.let { buildToolsVersion = it }

	defaultConfig {
		minSdk = libs.versions.androidMinSdk.get().toInt()
		consumerProguardFiles("consumer-rules.pro")
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

// widget 是「课程小组件」这个组件本身：表现层 + 刷新作业 + 组合根。
// app 只通过 CourseWidgetComponent 与 courseWidgetView 两个入口和它打交道。
dependencies {
	api(project(":core"))

	implementation(project(":data:remote"))
	implementation(project(":data:local"))

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.glance.appwidget)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.ktor.client.android)

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.test.junit)
}
