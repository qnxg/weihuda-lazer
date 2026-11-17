// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import cn.qnxg.weihuda.widget.refresh.ElectricityWidgetRefresh

/** 宿舍电量卡片的广播入口；系统按类名反射创建它，不能是 `internal`。 */
class ElectricityWidgetReceiver : MetricWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget = ElectricityWidget()

	override val rerenderAction: String = ACTION_RERENDER

	override fun ensureScheduled(context: Context) = ElectricityWidgetRefresh.ensurePeriodic(context)

	override fun enqueueRefresh(context: Context) = ElectricityWidgetRefresh.enqueueNow(context)

	override fun cancelScheduled(context: Context) = ElectricityWidgetRefresh.cancelAll(context)

	companion object {
		private const val ACTION_RERENDER = "cn.qnxg.weihuda.widget.ELECTRICITY_RERENDER"

		fun requestRerender(context: Context) =
			requestRerender(context, ElectricityWidgetReceiver::class.java, ACTION_RERENDER)
	}
}
