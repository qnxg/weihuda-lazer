// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.core.usecase.RefreshElectricityBalance
import cn.qnxg.weihuda.core.usecase.RefreshElectricityBalanceInteractor
import cn.qnxg.weihuda.widget.refresh.ElectricityWidgetRefresh

/**
 * 「宿舍剩余电量」卡片的门面：只负责这张卡片自己的取数、快照、排期与添加。
 *
 * 宿舍信息由后端从教务取，客户端没有可配置项，所以这里既没有设置也没有额外参数。
 */
class ElectricityWidgetComponent internal constructor(private val library: WidgetLibrary) {

	private val widgetId: WidgetId = ElectricityWidget.widgetId

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取电量」。 */
	suspend fun readSnapshot(): ElectricityBalance? = library.snapshots.readElectricityBalance(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): ElectricityBalance {
		val snapshot = newInteractor().refresh()
		ElectricityWidget().updateAll(library.appContext)
		// 与其它卡片同样的前台重绘兜底，见 MetricWidgetReceiver
		ElectricityWidgetReceiver.requestRerender(library.appContext)
		return snapshot
	}

	/** 应用启动或桌面上刚放下卡片时的一次性刷新（去重，不受周期任务时间窗限制）。 */
	fun requestRefresh() = ElectricityWidgetRefresh.enqueueNow(library.appContext)

	fun ensureScheduled() = ElectricityWidgetRefresh.ensurePeriodic(library.appContext)

	fun cancelScheduled() = ElectricityWidgetRefresh.cancelAll(library.appContext)

	fun pinToHomeScreen(): Boolean = library.pin(ElectricityWidgetReceiver::class.java, widgetId)

	fun pinnedWidgetCount(): Int = library.pinnedCount(ElectricityWidgetReceiver::class.java)

	private suspend fun newInteractor(): RefreshElectricityBalance {
		val backend = library.newBackend()
		return RefreshElectricityBalanceInteractor(
			settings = library.settingsStore,
			snapshots = library.snapshots,
			electricity = backend,
			clock = CampusClock.clock(),
			widgetId = widgetId,
		)
	}

	companion object {
		fun of(context: Context): ElectricityWidgetComponent = WidgetLibrary.of(context).electricityWidget
	}
}
