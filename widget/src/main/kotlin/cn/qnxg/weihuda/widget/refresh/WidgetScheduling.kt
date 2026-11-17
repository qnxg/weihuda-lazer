// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 刷新排期的共用实现：每张卡片一个唯一的任务名，策略相同。
 *
 * Android 的 `updatePeriodMillis` 最短只有 30 分钟且不可靠，这里统一用 WorkManager：
 * - 周期任务 15 分钟一次（系统允许的最小值），带网络约束；
 * - 手动刷新（点小组件「刷新」或打开应用）走一次性任务，并对同名任务去重。
 *
 * 卡片之间只差 worker 与任务名，所以排期策略只有这一份，改间隔时不会漏掉某张卡。
 */
internal object WidgetScheduling {
	private val networkConstraint = Constraints.Builder()
		.setRequiredNetworkType(NetworkType.CONNECTED)
		.build()

	fun ensurePeriodic(context: Context, worker: Class<out ListenableWorker>, uniqueName: String) {
		val request = PeriodicWorkRequest.Builder(worker, 15, TimeUnit.MINUTES)
			.setConstraints(networkConstraint)
			.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
			.build()
		WorkManager.getInstance(context.applicationContext)
			.enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request)
	}

	fun enqueueNow(context: Context, worker: Class<out ListenableWorker>, uniqueName: String) {
		val request = OneTimeWorkRequest.Builder(worker)
			.setConstraints(networkConstraint)
			.build()
		WorkManager.getInstance(context.applicationContext)
			.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
	}

	/** 最后一个实例被移除后停止后台刷新，避免白耗电。 */
	fun cancel(context: Context, periodicName: String, onceName: String) {
		WorkManager.getInstance(context.applicationContext).apply {
			cancelUniqueWork(periodicName)
			cancelUniqueWork(onceName)
		}
	}
}
