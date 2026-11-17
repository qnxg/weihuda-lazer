// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.qnxg.weihuda.widget.CampusCardWidgetComponent

/**
 * 校园卡余额的刷新作业：交给门面取一次数据并重建桌面。
 *
 * 失败已经在用例里消化成快照上的错误信息，所以这里始终返回 success。
 */
class CampusCardRefreshWorker(
	appContext: Context,
	params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

	override suspend fun doWork(): Result {
		CampusCardWidgetComponent.of(applicationContext).refresh()
		return Result.success()
	}
}
