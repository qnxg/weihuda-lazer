import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	jvmToolchain(17)
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

dependencies {
	api(libs.kotlinx.serialization.json)
	api(libs.ktor.client.core)
	implementation(libs.ktor.client.android)
	implementation(libs.kotlinx.coroutines.core)

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.test)
}
