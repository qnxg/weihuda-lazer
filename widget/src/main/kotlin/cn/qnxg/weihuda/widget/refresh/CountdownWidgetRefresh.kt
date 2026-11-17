// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context

/**
 * 倒计时卡片的刷新任务。
 *
 * 周期按**天**而不是 15 分钟：学期日期一天之内不会变，15 分钟一次的轮询只会放大请求；
 * 天数的跳动由 [CountdownDayAlarm] 在午夜重绘解决，那条路径不发请求。
 */
internal object CountdownWidgetRefresh {
	private const val UNIQUE_PERIODIC = "weihuda-countdown-refresh"
	private const val UNIQUE_ONCE = "weihuda-countdown-refresh-now"

	private const val PERIODIC_MINUTES = 24L * 60L

	fun ensurePeriodic(context: Context) = WidgetScheduling.ensurePeriodic(
		context = context,
		worker = CountdownRefreshWorker::class.java,
		uniqueName = UNIQUE_PERIODIC,
		intervalMinutes = PERIODIC_MINUTES,
	)

	fun enqueueNow(context: Context) =
		WidgetScheduling.enqueueNow(context, CountdownRefreshWorker::class.java, UNIQUE_ONCE)

	fun cancelAll(context: Context) =
		WidgetScheduling.cancel(context, UNIQUE_PERIODIC, UNIQUE_ONCE)
}
