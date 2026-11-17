// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * 点流量卡片里的「刷新」：只排一次后台任务，不在这里发请求。
 *
 * 与课表卡片的 [RefreshAction] 相同，系统按类名反射创建它，不能是 `internal`。
 */
class NetworkRefreshAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters,
	) {
		NetworkWidgetComponent.of(context).requestRefresh()
	}
}
