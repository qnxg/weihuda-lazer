// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 小组件刷新调度。
 *
 * Android 的 `updatePeriodMillis` 最短只有 30 分钟且不可靠，这里统一用 WorkManager：
 * - 周期任务 15 分钟一次（系统允许的最小值），带网络约束；
 * - 手动刷新（点小组件「刷新」或打开应用）走一次性任务，并对同名任务去重。
 */
internal object WidgetRefresh {
	private const val UNIQUE_PERIODIC = "weihuda-course-refresh"
	private const val UNIQUE_ONCE = "weihuda-course-refresh-now"

	private val networkConstraint = Constraints.Builder()
		.setRequiredNetworkType(NetworkType.CONNECTED)
		.build()

	fun ensurePeriodic(context: Context) {
		val request = PeriodicWorkRequestBuilder<RefreshWorker>(15, TimeUnit.MINUTES)
			.setConstraints(networkConstraint)
			.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
			.build()
		WorkManager.getInstance(context.applicationContext)
			.enqueueUniquePeriodicWork(UNIQUE_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, request)
	}

	fun enqueueNow(context: Context) {
		val request = OneTimeWorkRequestBuilder<RefreshWorker>()
			.setConstraints(networkConstraint)
			.build()
		WorkManager.getInstance(context.applicationContext)
			.enqueueUniqueWork(UNIQUE_ONCE, ExistingWorkPolicy.REPLACE, request)
	}

	/** 最后一个小组件被移除后停止后台刷新，避免白耗电。 */
	fun cancelAll(context: Context) {
		WorkManager.getInstance(context.applicationContext).apply {
			cancelUniqueWork(UNIQUE_PERIODIC)
			cancelUniqueWork(UNIQUE_ONCE)
		}
	}
}
