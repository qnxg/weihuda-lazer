// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context

/** 宿舍电量卡片的刷新任务。 */
internal object ElectricityWidgetRefresh {
	private const val UNIQUE_PERIODIC = "weihuda-electricity-refresh"
	private const val UNIQUE_ONCE = "weihuda-electricity-refresh-now"

	fun ensurePeriodic(context: Context) =
		WidgetScheduling.ensurePeriodic(context, ElectricityRefreshWorker::class.java, UNIQUE_PERIODIC)

	fun enqueueNow(context: Context) =
		WidgetScheduling.enqueueNow(context, ElectricityRefreshWorker::class.java, UNIQUE_ONCE)

	fun cancelAll(context: Context) =
		WidgetScheduling.cancel(context, UNIQUE_PERIODIC, UNIQUE_ONCE)
}
