package cn.qnxg.weihuda.lazer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.widget.CourseWidgetComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** 应用里的页面。加新小组件时先在这里加一个设置页。 */
sealed interface Screen {
	/** 小组件库：所有卡片的入口。 */
	data object Library : Screen

	/** 今日课程组件的设置。 */
	data object CourseSettings : Screen

	/** 账号与令牌：跨所有组件共用。 */
	data object Account : Screen
}

/**
 * 界面状态与动作。
 *
 * 它只知道 [CourseWidgetComponent] 这一个门面，所以界面里没有一处直接碰 HTTP、存储或 Glance。
 * 每个动作都在最后把最新快照读回来，界面上看到的数字与桌面上看到的一致。
 */
class AppState(
	private val component: CourseWidgetComponent,
	private val scope: CoroutineScope,
) {
	var screen by mutableStateOf<Screen>(Screen.Library)
		private set

	var settings by mutableStateOf<Settings?>(null)
		private set

	var snapshot by mutableStateOf<TodayCourses?>(null)
		private set

	var busy by mutableStateOf(false)
		private set

	/** 一行操作结果，[isError] 决定它的颜色。 */
	var notice by mutableStateOf<Notice?>(null)
		private set

	val defaultBaseUrl: String get() = component.defaultBaseUrl

	fun open(target: Screen) {
		screen = target
	}

	fun back(): Boolean {
		if (screen == Screen.Library) return false
		screen = Screen.Library
		return true
	}

	fun load() {
		scope.launch {
			settings = component.settings()
			snapshot = component.readSnapshot()
		}
	}

	/** 手动刷新一次（不改设置）。 */
	fun refresh() {
		scope.launch { refreshNow() }
	}

	/**
	 * 保存后立刻刷新。
	 *
	 * 两者必须在同一个协程里按顺序发生：刷新会重新读设置，先 launch 出去再刷新
	 * 会读到旧配置（改了后端地址却用旧地址取数）。
	 */
	fun saveAndRefresh(next: Settings) {
		scope.launch {
			component.saveSettings(next)
			settings = next
			refreshNow()
		}
	}

	private suspend fun refreshNow() {
		busy = true
		runCatching { component.refresh() }
			.onSuccess { result ->
				snapshot = result
				notice = result.failure?.let { Notice("刷新失败：$it", isError = true) }
					?: Notice("已刷新（${if (result.isDemo) "演示数据" else "实时数据"}）", isError = false)
			}
			.onFailure {
				notice = Notice("刷新失败：${it.message ?: "未知错误"}", isError = true)
			}
		busy = false
	}

	fun pinToHomeScreen() {
		notice = if (component.pinToHomeScreen()) {
			Notice("请在弹出的系统弹窗里确认添加", isError = false)
		} else {
			Notice("当前桌面不支持一键添加，请长按桌面空白处 → 添加小组件 → 今日课程", isError = false)
		}
	}

	/** 退出登录：清掉令牌并退回演示数据，桌面立刻回到有内容的状态。 */
	fun signOut() {
		scope.launch {
			val current = settings ?: component.settings()
			component.saveSettings(current.copy(credentials = Credentials("")))
			component.refresh()
			settings = component.settings()
			snapshot = component.readSnapshot()
			notice = Notice("已退出登录，桌面回到演示数据", isError = false)
		}
	}
}

data class Notice(val text: String, val isError: Boolean)
