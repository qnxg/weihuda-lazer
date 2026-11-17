// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.SignInInteractor
import cn.qnxg.weihuda.core.usecase.SignInResult
import cn.qnxg.weihuda.data.local.settings.SharedPreferencesSettingsStore
import cn.qnxg.weihuda.data.local.snapshot.SharedPreferencesSnapshotStore
import cn.qnxg.weihuda.data.remote.DEFAULT_BASE_URL
import cn.qnxg.weihuda.data.remote.PASSWORD_RESET_URL
import cn.qnxg.weihuda.data.remote.WeihudaBackend
import cn.qnxg.weihuda.data.remote.weihudaHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 组件库的组合根：应用级配置（登录、后端地址、存储、HTTP）只在这里装配一次，
 * 每张卡片各自持有一个门面（[courseWidget] / [networkWidget] / [countdownWidget]）。
 *
 * 应用界面只认这一个入口，所以它不需要知道数据层实现了什么，也不会出现「桌面用一套适配器、
 * 应用内用另一套」的偏差。加卡片时新增一个门面，而不是复制登录与存储。
 */
class WidgetLibrary private constructor(context: Context) {

	internal val appContext: Context = context.applicationContext

	internal val settingsStore = SharedPreferencesSettingsStore(appContext)

	internal val snapshots = SharedPreferencesSnapshotStore(appContext)

	// 引擎在这里选择：data:remote 是纯 JVM 模块，不认识 Android。
	private val http: HttpClient by lazy { weihudaHttpClient(Android.create()) }

	/** 界面上「后端地址」留空时用的默认值。 */
	val defaultBaseUrl: String get() = DEFAULT_BASE_URL

	/** 登录页「忘记密码」的去处：地址定在数据层，界面只认门面。 */
	val passwordResetUrl: String get() = PASSWORD_RESET_URL

	val courseWidget: CourseWidgetComponent by lazy { CourseWidgetComponent(this) }

	val networkWidget: NetworkWidgetComponent by lazy { NetworkWidgetComponent(this) }

	val countdownWidget: CountdownWidgetComponent by lazy { CountdownWidgetComponent(this) }

	suspend fun settings(): Settings = settingsStore.load()

	suspend fun saveSettings(settings: Settings) = settingsStore.save(settings)

	/**
	 * 登录并把令牌写进设置。
	 *
	 * 失败已经是用例里的 [SignInResult.Failure]，所以这里不抛异常。
	 */
	suspend fun signIn(stuId: String, password: String): SignInResult =
		SignInInteractor(newBackend(), settingsStore).signIn(stuId, password)

	/**
	 * 装配一次请求客户端。
	 *
	 * 后端地址是设置项，换地址后必须换适配器，所以每次刷新按当前设置新建一遍；
	 * HTTP 客户端自身是共享的，重建的只是那个无状态的适配器对象。
	 */
	internal suspend fun newBackend(): WeihudaBackend {
		val settings = settingsStore.load()
		return WeihudaBackend(http, settings.baseUrl.ifBlank { DEFAULT_BASE_URL })
	}

	/** 一键添加的回执；卡片据此把「请求中」改成「已添加」。 */
	private val pinResultFlow = MutableSharedFlow<WidgetId>(extraBufferCapacity = 1)
	val pinResults: SharedFlow<WidgetId> = pinResultFlow.asSharedFlow()

	/**
	 * 请求把某张卡片固定到桌面；返回 false 表示当前启动器不接受应用内添加。
	 *
	 * 返回 true 只说明请求交给了桌面，不代表确认页真的出现：MIUI 桌面的
	 * `AddItemActivity` 会先拉起再立刻以「has no permission」自行结束，
	 * 既不弹窗也不回调（见 [PinResultReceiver]）。
	 */
	internal fun pin(receiver: Class<out AppWidgetProvider>, id: WidgetId): Boolean {
		val manager = AppWidgetManager.getInstance(appContext)
		val callback = PendingIntent.getBroadcast(
			appContext,
			id.value.hashCode(),
			Intent(appContext, PinResultReceiver::class.java).putExtra(EXTRA_PINNED_WIDGET, id.value),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
		)
		return manager.isRequestPinAppWidgetSupported &&
			manager.requestPinAppWidget(ComponentName(appContext, receiver), null, callback)
	}

	/** 桌面上现有的某张卡片的实例数，用于桌面不回调时复核一键添加是否落地。 */
	internal fun pinnedCount(receiver: Class<out AppWidgetProvider>): Int =
		AppWidgetManager.getInstance(appContext)
			.getAppWidgetIds(ComponentName(appContext, receiver))
			.size

	internal fun onPinned(id: WidgetId) {
		pinResultFlow.tryEmit(id)
	}

	companion object {
		internal const val EXTRA_PINNED_WIDGET = "cn.qnxg.weihuda.widget.PINNED_WIDGET"

		@Volatile
		private var instance: WidgetLibrary? = null

		fun of(context: Context): WidgetLibrary =
			instance ?: synchronized(this) {
				instance ?: WidgetLibrary(context).also { instance = it }
			}
	}
}
