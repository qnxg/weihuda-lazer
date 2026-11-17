// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.widget.presentation.CardBalancePresenter

/**
 * 校园卡余额小组件。
 *
 * 只渲染：读 [CampusCardWidgetComponent] 里最新的快照，交给 presenter 变成文案，再摆到屏幕上。
 * 渲染路径上没有网络，也没有磁盘写入。
 */
class CampusCardWidget : GlanceAppWidget() {

	// 只有 2×2：信息就是一个数字，放大只会多出空白，所以不给启动器别的档位
	override val sizeMode: SizeMode = SizeMode.Single

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val snapshot = CampusCardWidgetComponent.of(context).readSnapshot()
		val view = CardBalancePresenter.present(snapshot)
		provideContent {
			MetricCardContent(context = context, view = view)
		}
	}

	companion object {
		val widgetId: WidgetId = WidgetId.CampusCard
	}
}
