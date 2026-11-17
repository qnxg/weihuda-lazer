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
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetPresenter

/**
 * 校园网流量小组件。
 *
 * 只渲染：读 [NetworkWidgetComponent] 里最新的快照，交给 presenter 变成文案，再摆到屏幕上。
 * 渲染路径上没有网络，也没有磁盘写入。
 */
class NetworkWidget : GlanceAppWidget() {

	// Exact 而不是 Responsive：进度条要按卡片真实宽度算填充宽度，Responsive 的
	// LocalSize 只是预设档位（250dp），启动器把卡片拉宽后比例就会对不上。
	override val sizeMode: SizeMode = SizeMode.Exact

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val snapshot = NetworkWidgetComponent.of(context).readSnapshot()
		val view = NetworkWidgetPresenter.present(snapshot)
		provideContent {
			NetworkWidgetContent(context = context, view = view)
		}
	}

	companion object {
		val widgetId: WidgetId = WidgetId.CampusNetwork
	}
}
