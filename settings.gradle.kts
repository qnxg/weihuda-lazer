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

include(":core")
include(":widget")
include(":app")
