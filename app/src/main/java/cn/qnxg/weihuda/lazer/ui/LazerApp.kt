// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import cn.qnxg.weihuda.lazer.ui.countdown.CountdownSettingsScreen
import cn.qnxg.weihuda.lazer.ui.course.CourseSettingsScreen
import cn.qnxg.weihuda.lazer.ui.home.HomeScreen
import cn.qnxg.weihuda.lazer.ui.login.LoginScreen
import cn.qnxg.weihuda.lazer.ui.network.NetworkSettingsScreen
import cn.qnxg.weihuda.lazer.ui.settings.SettingsScreen
import cn.qnxg.weihuda.widget.WidgetLibrary

/**
 * 应用的最外层：一个很浅的导航栈（首页 → 某个卡片的设置 / 登录 → 应用设置）。
 *
 * 只有四屏、且没有深链接与多任务栈，用 navigation-compose 换来的只是额外依赖；
 * 加卡片时在这里多一个分支即可，真要复杂起来再换库也不会影响下层。
 */
@Composable
fun LazerApp(
	library: WidgetLibrary,
	debugToken: String? = null,
	fallbackToken: String? = null,
	onFallbackTokenApplied: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val miuiHome = remember(context) { usesMiuiHome(context) }
	val state = remember(library, scope, miuiHome) { AppState(library, scope, miuiHome) }

	// 注入用的令牌只在启动那一次生效：重组不该把用户后来手动登录的账号再顶掉。
	LaunchedEffect(state) {
		state.load(
			debugToken = debugToken,
			fallbackToken = fallbackToken,
			onFallbackApplied = onFallbackTokenApplied,
		)
	}

	BackHandler(enabled = state.screen != Screen.Home) { state.back() }

	when (state.screen) {
		Screen.Home -> HomeScreen(
			settings = state.settings,
			snapshot = state.snapshot,
			networkSnapshot = state.networkSnapshot,
			countdownSnapshot = state.countdownSnapshot,
			notice = state.notice,
			onOpenLogin = { state.open(Screen.Login) },
			onOpenCourseSettings = { state.open(Screen.CourseSettings) },
			onOpenNetworkSettings = { state.open(Screen.NetworkSettings) },
			onOpenCountdownSettings = { state.open(Screen.CountdownSettings) },
			onOpenSettings = { state.open(Screen.Settings) },
		)

		Screen.Login -> LoginScreen(
			submitting = state.signingIn,
			error = state.loginError,
			onBack = { state.back() },
			onForgotPassword = { context.openUrl(state.passwordResetUrl) },
			onSubmit = state::signIn,
		)

		Screen.CourseSettings -> CourseSettingsScreen(
			snapshot = state.snapshot,
			notice = state.notice,
			busy = state.busy,
			pinBlocked = state.pinBlocked,
			canOpenAppSettings = miuiHome,
			onPin = state::pinCourse,
			onRefresh = state::refreshCourse,
			onOpenAppSettings = { context.startActivity(appDetailsIntent(context)) },
			onOpenExactAlarmSettings = { context.openExactAlarmSettings() },
		)

		Screen.NetworkSettings -> NetworkSettingsScreen(
			snapshot = state.networkSnapshot,
			notice = state.notice,
			busy = state.busy,
			pinBlocked = state.pinBlocked,
			canOpenAppSettings = miuiHome,
			onPin = state::pinNetwork,
			onRefresh = state::refreshNetwork,
			onOpenAppSettings = { context.startActivity(appDetailsIntent(context)) },
		)

		Screen.CountdownSettings -> CountdownSettingsScreen(
			snapshot = state.countdownSnapshot,
			notice = state.notice,
			busy = state.busy,
			pinBlocked = state.pinBlocked,
			canOpenAppSettings = miuiHome,
			onPin = state::pinCountdown,
			onRefresh = state::refreshCountdown,
			onOpenAppSettings = { context.startActivity(appDetailsIntent(context)) },
		)

		Screen.Settings -> SettingsScreen(
			settings = state.settings,
			notice = state.notice,
			busy = state.busy,
			defaultBaseUrl = state.defaultBaseUrl,
			onSave = state::saveAndRefresh,
			onOpenLogin = { state.open(Screen.Login) },
			onSignOut = state::signOut,
		)
	}
}

private const val MIUI_HOME = "com.miui.home"

/** MIUI 桌面把一键添加挡在「桌面快捷方式」权限后面，只有它能给出这个提示。 */
private fun usesMiuiHome(context: Context): Boolean {
	val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
	val resolved = context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
	return resolved?.activityInfo?.packageName == MIUI_HOME
}

/** Android 12+ 的「闹钟与提醒」特殊权限页；没有该页时退回应用详情，至少能看到权限入口。 */
private fun Context.openExactAlarmSettings() {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
	val intent = Intent(
		Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
		Uri.fromParts("package", packageName, null),
	)
	runCatching { startActivity(intent) }
		.onFailure { runCatching { startActivity(appDetailsIntent(this)) } }
}

private fun appDetailsIntent(context: Context): Intent =
	Intent(
		Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
		Uri.fromParts("package", context.packageName, null),
	)

/** 没有匹配的浏览器时静默失败：这只是个辅助入口，不值得单独报错。 */
private fun Context.openUrl(url: String) {
	runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
