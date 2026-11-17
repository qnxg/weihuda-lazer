// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import cn.qnxg.weihuda.widget.refresh.CampusCardWidgetRefresh

/** 校园卡余额卡片的广播入口；系统按类名反射创建它，不能是 `internal`。 */
class CampusCardWidgetReceiver : MetricWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget = CampusCardWidget()

	override val rerenderAction: String = ACTION_RERENDER

	override fun ensureScheduled(context: Context) = CampusCardWidgetRefresh.ensurePeriodic(context)

	override fun enqueueRefresh(context: Context) = CampusCardWidgetRefresh.enqueueNow(context)

	override fun cancelScheduled(context: Context) = CampusCardWidgetRefresh.cancelAll(context)

	companion object {
		private const val ACTION_RERENDER = "cn.qnxg.weihuda.widget.CAMPUS_CARD_RERENDER"

		fun requestRerender(context: Context) =
			requestRerender(context, CampusCardWidgetReceiver::class.java, ACTION_RERENDER)
	}
}
