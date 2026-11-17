// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshNetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshNetworkTrafficInteractor
import cn.qnxg.weihuda.widget.refresh.NetworkWidgetRefresh

/**
 * 「校园网流量」卡片的门面：只负责这张卡片自己的取数、快照、排期与添加。
 *
 * 它与 [CourseWidgetComponent] 共用 [WidgetLibrary]，所以刷新节奏一致、令牌只有一份。
 */
class NetworkWidgetComponent internal constructor(private val library: WidgetLibrary) {

	private val widgetId: WidgetId = NetworkWidget.widgetId

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取流量」。 */
	suspend fun readSnapshot(): NetworkTraffic? = library.snapshots.readNetworkTraffic(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): NetworkTraffic {
		val snapshot = newInteractor().refresh()
		NetworkWidget().updateAll(library.appContext)
		// 与课表卡片同样的前台重绘兜底，见 CourseWidgetReceiver.requestRerender
		NetworkWidgetReceiver.requestRerender(library.appContext)
		return snapshot
	}

	/** 节流后的一次性刷新（点小组件里的「刷新」）。 */
	fun requestRefresh() = NetworkWidgetRefresh.enqueueNow(library.appContext)

	fun ensureScheduled() = NetworkWidgetRefresh.ensurePeriodic(library.appContext)

	fun cancelScheduled() = NetworkWidgetRefresh.cancelAll(library.appContext)

	fun pinToHomeScreen(): Boolean = library.pin(NetworkWidgetReceiver::class.java, widgetId)

	fun pinnedWidgetCount(): Int = library.pinnedCount(NetworkWidgetReceiver::class.java)

	private suspend fun newInteractor(): RefreshNetworkTraffic {
		val backend = library.newBackend()
		return RefreshNetworkTrafficInteractor(
			settings = library.settingsStore,
			snapshots = library.snapshots,
			networks = backend,
			clock = CampusClock.clock(),
			widgetId = widgetId,
		)
	}

	companion object {
		fun of(context: Context): NetworkWidgetComponent = WidgetLibrary.of(context).networkWidget
	}
}
