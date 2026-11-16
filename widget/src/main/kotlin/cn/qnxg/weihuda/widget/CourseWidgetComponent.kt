package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.RefreshTodayCourses
import cn.qnxg.weihuda.core.usecase.RefreshTodayCoursesInteractor
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.data.local.settings.SharedPreferencesSettingsStore
import cn.qnxg.weihuda.data.local.snapshot.SharedPreferencesSnapshotStore
import cn.qnxg.weihuda.data.remote.DEFAULT_BASE_URL
import cn.qnxg.weihuda.data.remote.WeihudaBackend
import cn.qnxg.weihuda.data.remote.weihudaHttpClient
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

/**
 * 「课程小组件」这个组件的公开门面，同时是它自己的组合根。
 *
 * 应用界面（小组件库、课程设置、账号）只需要这一个入口：它把用例、适配器和 Glance
 * 都藏在内部，所以换存储、换渲染框架、加接口都不会渗到 app 里去。
 * Worker 也在同一个进程里复用它，避免出现两套装配。
 */
class CourseWidgetComponent private constructor(context: Context) {

	private val appContext: Context = context.applicationContext

	private val settingsStore = SharedPreferencesSettingsStore(appContext)
	private val snapshotStore = SharedPreferencesSnapshotStore(appContext)

	// 引擎在这里选择：data:remote 是纯 JVM 模块，不认识 Android。
	private val http: HttpClient by lazy { weihudaHttpClient(Android.create()) }

	private val widgetId: WidgetId = CourseWidget.widgetId

	/** 界面上「后端地址」留空时用的默认值。 */
	val defaultBaseUrl: String get() = DEFAULT_BASE_URL

	suspend fun settings(): Settings = settingsStore.load()

	suspend fun saveSettings(settings: Settings) = settingsStore.save(settings)

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取课表」。 */
	suspend fun readSnapshot(): TodayCourses? = snapshotStore.readTodayCourses(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): TodayCourses {
		val snapshot = newInteractor().refresh()
		CourseWidget().updateAll(appContext)
		return snapshot
	}

	/** 节流后的一次性刷新（点小组件里的「刷新」）。 */
	fun requestRefresh() = WidgetRefresh.enqueueNow(appContext)

	/** 应用启动时保证周期任务存在。 */
	fun ensureScheduled() = WidgetRefresh.ensurePeriodic(appContext)

	/** 最后一个小组件被移除后调用，停止后台刷新。 */
	fun cancelScheduled() = WidgetRefresh.cancelAll(appContext)

	/** 请求把小组件固定到桌面；返回 false 表示当前启动器不支持一键添加。 */
	fun pinToHomeScreen(): Boolean {
		val manager = AppWidgetManager.getInstance(appContext)
		val provider = ComponentName(appContext, CourseWidgetReceiver::class.java)
		return manager.isRequestPinAppWidgetSupported &&
			manager.requestPinAppWidget(provider, null, null)
	}

	/**
	 * 装配一次刷新链路。
	 *
	 * 后端地址是设置项，换地址后必须换适配器，所以每次刷新按当前设置新建一遍；
	 * HTTP 客户端自身是共享的，重建的只是那个无状态的适配器对象。
	 */
	private suspend fun newInteractor(): RefreshTodayCourses {
		val settings = settingsStore.load()
		val backend = WeihudaBackend(http, settings.baseUrl.ifBlank { DEFAULT_BASE_URL })
		return RefreshTodayCoursesInteractor(
			settings = settingsStore,
			snapshots = snapshotStore,
			semesters = backend,
			courses = backend,
			students = backend,
			clock = CampusClock.clock(),
			widgetId = widgetId,
		)
	}

	companion object {
		@Volatile
		private var instance: CourseWidgetComponent? = null

		fun of(context: Context): CourseWidgetComponent =
			instance ?: synchronized(this) {
				instance ?: CourseWidgetComponent(context).also { instance = it }
			}
	}
}
