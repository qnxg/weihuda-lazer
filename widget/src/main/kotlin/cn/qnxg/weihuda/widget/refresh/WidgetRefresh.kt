// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context

/** 课表卡片的刷新任务。任务名保持不变，升级后不会残留旧任务。 */
internal object WidgetRefresh {
	private const val UNIQUE_PERIODIC = "weihuda-course-refresh"
	private const val UNIQUE_ONCE = "weihuda-course-refresh-now"

	fun ensurePeriodic(context: Context) =
		WidgetScheduling.ensurePeriodic(context, RefreshWorker::class.java, UNIQUE_PERIODIC)

	fun enqueueNow(context: Context) =
		WidgetScheduling.enqueueNow(context, RefreshWorker::class.java, UNIQUE_ONCE)

	fun cancelAll(context: Context) =
		WidgetScheduling.cancel(context, UNIQUE_PERIODIC, UNIQUE_ONCE)
}
