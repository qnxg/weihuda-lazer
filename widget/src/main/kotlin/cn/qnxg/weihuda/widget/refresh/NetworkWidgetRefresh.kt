// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context

/** 校园网流量卡片的刷新任务。 */
internal object NetworkWidgetRefresh {
	private const val UNIQUE_PERIODIC = "weihuda-network-refresh"
	private const val UNIQUE_ONCE = "weihuda-network-refresh-now"

	fun ensurePeriodic(context: Context) =
		WidgetScheduling.ensurePeriodic(context, NetworkRefreshWorker::class.java, UNIQUE_PERIODIC)

	fun enqueueNow(context: Context) =
		WidgetScheduling.enqueueNow(context, NetworkRefreshWorker::class.java, UNIQUE_ONCE)

	fun cancelAll(context: Context) =
		WidgetScheduling.cancel(context, UNIQUE_PERIODIC, UNIQUE_ONCE)
}
