// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.RefreshTodayCourses
import cn.qnxg.weihuda.core.usecase.RefreshTodayCoursesInteractor
import cn.qnxg.weihuda.core.usecase.SignInInteractor
import cn.qnxg.weihuda.core.usecase.SignInResult
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.data.local.settings.SharedPreferencesSettingsStore
import cn.qnxg.weihuda.data.local.snapshot.SharedPreferencesSnapshotStore
import cn.qnxg.weihuda.data.remote.DEFAULT_BASE_URL
import cn.qnxg.weihuda.data.remote.PASSWORD_RESET_URL
import cn.qnxg.weihuda.data.remote.WeihudaBackend
import cn.qnxg.weihuda.data.remote.weihudaHttpClient
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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

	/** 登录页「忘记密码」的去处：地址定在数据层，界面只认门面。 */
	val passwordResetUrl: String get() = PASSWORD_RESET_URL

	suspend fun settings(): Settings = settingsStore.load()

	suspend fun saveSettings(settings: Settings) = settingsStore.save(settings)

	/**
	 * 登录并把令牌写进设置。
	 *
	 * 失败已经是用例里的 [SignInResult.Failure]，所以这里不抛异常。
	 */
	suspend fun signIn(stuId: String, password: String): SignInResult =
		SignInInteractor(newBackend(), settingsStore).signIn(stuId, password)

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
		// updateAll 在应用前台时可能不生效，见 CourseWidgetReceiver.requestRerender
		CourseWidgetReceiver.requestRerender(appContext)
		return snapshot
	}

	/** 节流后的一次性刷新（点小组件里的「刷新」）。 */
	fun requestRefresh() = WidgetRefresh.enqueueNow(appContext)

	/** 应用启动时保证周期任务存在。 */
	fun ensureScheduled() = WidgetRefresh.ensurePeriodic(appContext)

	/** 最后一个小组件被移除后调用，停止后台刷新。 */
	fun cancelScheduled() = WidgetRefresh.cancelAll(appContext)

	/** 一键添加的回执，只在桌面真的放下实例时触发；AppState 用它把「请求中」改成「已添加」。 */
	private val pinResultFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
	val pinResults: SharedFlow<Unit> = pinResultFlow.asSharedFlow()

	/**
	 * 请求把小组件固定到桌面；返回 false 表示当前启动器不接受应用内添加。
	 *
	 * 返回 true 只说明请求交给了桌面，不代表确认页真的出现：MIUI 桌面的
	 * `AddItemActivity` 会先拉起再立刻以「has no permission」自行结束，
	 * 既不弹窗也不回调（见 [PinResultReceiver]）。
	 */
	fun pinToHomeScreen(): Boolean {
		val manager = AppWidgetManager.getInstance(appContext)
		val provider = ComponentName(appContext, CourseWidgetReceiver::class.java)
		val callback = PendingIntent.getBroadcast(
			appContext,
			0,
			Intent(appContext, PinResultReceiver::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
		)
		return manager.isRequestPinAppWidgetSupported &&
			manager.requestPinAppWidget(provider, null, callback)
	}

	/** 桌面上现有的本组件实例数，用于桌面不回调时复核一键添加是否落地。 */
	fun pinnedWidgetCount(): Int = AppWidgetManager.getInstance(appContext)
		.getAppWidgetIds(ComponentName(appContext, CourseWidgetReceiver::class.java))
		.size

	internal fun onPinned() {
		pinResultFlow.tryEmit(Unit)
	}

	/**
	 * 装配一次刷新链路。
	 *
	 * 后端地址是设置项，换地址后必须换适配器，所以每次刷新按当前设置新建一遍；
	 * HTTP 客户端自身是共享的，重建的只是那个无状态的适配器对象。
	 */
	private suspend fun newInteractor(): RefreshTodayCourses {
		val backend = newBackend()
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

	private suspend fun newBackend(): WeihudaBackend {
		val settings = settingsStore.load()
		return WeihudaBackend(http, settings.baseUrl.ifBlank { DEFAULT_BASE_URL })
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
