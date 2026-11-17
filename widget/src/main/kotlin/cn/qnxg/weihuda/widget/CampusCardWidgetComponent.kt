// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.core.usecase.RefreshCardBalance
import cn.qnxg.weihuda.core.usecase.RefreshCardBalanceInteractor
import cn.qnxg.weihuda.widget.refresh.CampusCardWidgetRefresh

/**
 * 「校园卡余额」卡片的门面：只负责这张卡片自己的取数、快照、排期与添加。
 *
 * 它与其它卡片共用 [WidgetLibrary]，所以刷新节奏一致、令牌只有一份。
 */
class CampusCardWidgetComponent internal constructor(private val library: WidgetLibrary) {

	private val widgetId: WidgetId = CampusCardWidget.widgetId

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取余额」。 */
	suspend fun readSnapshot(): CardBalance? = library.snapshots.readCardBalance(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): CardBalance {
		val snapshot = newInteractor().refresh()
		CampusCardWidget().updateAll(library.appContext)
		// 与其它卡片同样的前台重绘兜底，见 MetricWidgetReceiver
		CampusCardWidgetReceiver.requestRerender(library.appContext)
		return snapshot
	}

	/** 应用启动或桌面上刚放下卡片时的一次性刷新（去重，不受周期任务时间窗限制）。 */
	fun requestRefresh() = CampusCardWidgetRefresh.enqueueNow(library.appContext)

	fun ensureScheduled() = CampusCardWidgetRefresh.ensurePeriodic(library.appContext)

	fun cancelScheduled() = CampusCardWidgetRefresh.cancelAll(library.appContext)

	fun pinToHomeScreen(): Boolean = library.pin(CampusCardWidgetReceiver::class.java, widgetId)

	fun pinnedWidgetCount(): Int = library.pinnedCount(CampusCardWidgetReceiver::class.java)

	private suspend fun newInteractor(): RefreshCardBalance {
		val backend = library.newBackend()
		return RefreshCardBalanceInteractor(
			settings = library.settingsStore,
			snapshots = library.snapshots,
			cards = backend,
			clock = CampusClock.clock(),
			widgetId = widgetId,
		)
	}

	companion object {
		fun of(context: Context): CampusCardWidgetComponent = WidgetLibrary.of(context).campusCardWidget
	}
}
