package cn.qnxg.weihuda.lazer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cn.qnxg.weihuda.lazer.ui.account.AccountScreen
import cn.qnxg.weihuda.lazer.ui.account.withToken
import cn.qnxg.weihuda.lazer.ui.course.CourseSettingsScreen
import cn.qnxg.weihuda.lazer.ui.library.WidgetLibraryScreen
import cn.qnxg.weihuda.widget.CourseWidgetComponent

/**
 * 应用的最外层：一个很浅的导航栈（库 → 某个卡片的设置 → 账号）。
 *
 * 只有三屏、且没有深链接与多任务栈，用 navigation-compose 换来的只是额外依赖；
 * 加卡片时在这里多一个分支即可，真要复杂起来再换库也不会影响下层。
 */
@Composable
fun LazerApp(component: CourseWidgetComponent) {
	val scope = rememberCoroutineScope()
	val state = remember(component, scope) { AppState(component, scope) }

	LaunchedEffect(state) { state.load() }

	BackHandler(enabled = state.screen != Screen.Library) { state.back() }

	when (state.screen) {
		Screen.Library -> WidgetLibraryScreen(
			settings = state.settings,
			snapshot = state.snapshot,
			notice = state.notice,
			onOpenCourseSettings = { state.open(Screen.CourseSettings) },
			onOpenAccount = { state.open(Screen.Account) },
		)

		Screen.CourseSettings -> CourseSettingsScreen(
			settings = state.settings,
			snapshot = state.snapshot,
			notice = state.notice,
			busy = state.busy,
			defaultBaseUrl = state.defaultBaseUrl,
			onSaveAndRefresh = state::saveAndRefresh,
			onPin = state::pinToHomeScreen,
			onOpenAccount = { state.open(Screen.Account) },
		)

		Screen.Account -> AccountScreen(
			settings = state.settings,
			notice = state.notice,
			busy = state.busy,
			onSaveToken = { token ->
				state.settings?.let { state.saveAndRefresh(it.withToken(token)) }
			},
			onSignOut = state::signOut,
			onRefresh = state::refresh,
		)
	}
}
