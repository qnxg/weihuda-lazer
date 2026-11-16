package cn.qnxg.weihuda.lazer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.usecase.SignInResult
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.widget.CourseWidgetComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 应用里的页面。加新小组件时先在这里加一个设置页。 */
sealed interface Screen {
	/** 首页：所有卡片的入口。 */
	data object Home : Screen

	/** 登录：学号 + 密码换令牌。 */
	data object Login : Screen

	/** 今日课程组件的设置。 */
	data object CourseSettings : Screen

	/** 应用设置：账号、后端地址与版本。 */
	data object Settings : Screen
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
	/** 桌面是不是 MIUI：它把「一键添加」挡在「桌面快捷方式」权限后面，提示语要不同。 */
	private val miuiHome: Boolean = false,
) {
	var screen by mutableStateOf<Screen>(Screen.Home)
		private set

	var settings by mutableStateOf<Settings?>(null)
		private set

	var snapshot by mutableStateOf<TodayCourses?>(null)
		private set

	var busy by mutableStateOf(false)
		private set

	/** 登录中：与 [busy]（刷新中）分开，否则刷新会把登录按钮的文字也改掉。 */
	var signingIn by mutableStateOf(false)
		private set

	/** 登录页的错误文案；登录成功或重新进页时清掉。 */
	var loginError by mutableStateOf<String?>(null)
		private set

	/** 一行操作结果，[isError] 决定它的颜色。 */
	var notice by mutableStateOf<Notice?>(null)
		private set

	/** 一键添加被桌面拒绝：界面上多给一个「打开应用设置」的入口。 */
	var pinBlocked by mutableStateOf(false)
		private set

	val defaultBaseUrl: String get() = component.defaultBaseUrl

	val passwordResetUrl: String get() = component.passwordResetUrl

	/** 本次一键添加是否已有结论：桌面回执与实例数复核谁先到算谁的。 */
	private var pinSettled = false

	init {
		// 桌面真的放下实例后才会有这条广播；桌面静默拒绝时不会来。
		scope.launch {
			component.pinResults.collect {
				pinSettled = true
				pinBlocked = false
				notice = Notice("已添加到桌面", isError = false)
			}
		}
	}

	fun open(target: Screen) {
		if (target == Screen.Login) loginError = null
		screen = target
	}

	fun back(): Boolean {
		if (screen == Screen.Home) return false
		screen = Screen.Home
		return true
	}

	/**
	 * 读一次本机状态。
	 *
	 * [debugToken] 来自启动参数，总是覆盖；[fallbackToken] 来自构建配置，只在未登录时补上。
	 * [onFallbackApplied] 让调用方把「这个兜底令牌已经用过」记下来——不记的话，用户退出登录后
	 * 每次启动都会被它重新塞回去。
	 */
	fun load(
		debugToken: String? = null,
		fallbackToken: String? = null,
		onFallbackApplied: () -> Unit = {},
	) {
		scope.launch {
			settings = component.settings()
			snapshot = component.readSnapshot()

			val current = settings?.credentials?.token.orEmpty()
			val explicit = debugToken?.takeIf { it.isNotEmpty() && it != current }
			val fallback = fallbackToken?.takeIf { it.isNotEmpty() && current.isEmpty() }
			val injected = explicit ?: fallback
			if (injected != null) {
				component.saveSettings(component.settings().copy(credentials = Credentials(injected)))
				settings = component.settings()
				refreshNow()
			}
			if (fallback != null) onFallbackApplied()
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
				notice = when {
					result.failure != null -> Notice("刷新失败：${result.failure}", isError = true)
					// 没登录时刷新不产生任何请求，按钮不能看起来「点了没反应」。
					result.state == TodayCourses.State.SIGNED_OUT -> Notice("未登录，先登录再刷新", isError = true)
					else -> Notice("已刷新", isError = false)
				}
			}
			.onFailure {
				notice = Notice("刷新失败：${it.message ?: "未知错误"}", isError = true)
			}
		busy = false
	}

	fun pinToHomeScreen() {
		pinSettled = false
		pinBlocked = false
		scope.launch {
			val before = component.pinnedWidgetCount()
			if (!component.pinToHomeScreen()) {
				pinSettled = true
				notice = Notice("当前桌面不支持应用内添加，请长按桌面空白处添加小组件", isError = true)
				return@launch
			}

			// 返回值只代表请求发出，桌面可能静默拒绝，所以等一小段时间用实例数复核；
			// 真正确认仍以回执为准，回执先到就不再改文案。
			notice = Notice("已请求桌面添加…", isError = false)
			delay(PIN_CONFIRM_TIMEOUT_MS)
			if (pinSettled) return@launch
			pinSettled = true
			if (component.pinnedWidgetCount() <= before) {
				pinBlocked = true
				notice = Notice(if (miuiHome) PIN_MIUI else PIN_MANUAL, isError = true)
			} else {
				notice = Notice("已添加到桌面", isError = false)
			}
		}
	}

	/**
	 * 登录：成功就回首页并立刻刷新。登录的意义就是从无数据变成有数据，等用户再点一次刷新很彦。
	 */
	fun signIn(stuId: String, password: String) {
		scope.launch {
			signingIn = true
			loginError = null
			when (val result = component.signIn(stuId, password)) {
				is SignInResult.Failure -> loginError = result.message
				is SignInResult.Success -> {
					settings = component.settings()
					screen = Screen.Home
					refreshNow()
				}
			}
			signingIn = false
		}
	}

	/** 退出登录：清掉令牌并重算快照，桌面与首页立刻回到未登录态。 */
	fun signOut() {
		scope.launch {
			val current = settings ?: component.settings()
			component.saveSettings(current.copy(credentials = Credentials("")))
			component.refresh()
			settings = component.settings()
			snapshot = component.readSnapshot()
			notice = Notice("已退出登录", isError = false)
		}
	}
}

data class Notice(val text: String, val isError: Boolean)

/** 一键添加后等桌面落地的时长：MIUI 拒绝发生在毫秒级，真实确认也不会拖过这个量级太久。 */
private const val PIN_CONFIRM_TIMEOUT_MS = 1_500L

// 桌面菜单里这个入口的叫法不统一（小工具 / 小组件 / 小部件），所以只说长按后选名字。
private const val PIN_MANUAL = "桌面没有添加"

private const val PIN_MIUI = "桌面没有添加：需要允许「桌面快捷方式」"
