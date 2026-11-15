import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // Glance 是 Compose 风格 API，本模块自己写 @Composable，必须挂 Compose 编译器插件
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cn.qnxg.weihuda.widget"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    // 本机只装了 android-36.1（Android 16 QPR2），AGP 8.13 支持次版本 SDK
    compileSdkMinor = 1
    buildToolsVersion = "36.1.0"

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

dependencies {
    api(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
