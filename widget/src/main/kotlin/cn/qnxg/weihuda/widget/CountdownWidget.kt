// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetPresenter
import java.time.LocalDate

/**
 * 学期倒计时小组件。
 *
 * 只渲染：读 [CountdownWidgetComponent] 里最新的学期快照，按**今天**算出剩余天数，
 * 交给 presenter 变成文案，再摆到屏幕上。渲染路径上没有网络，也没有磁盘写入。
 */
class CountdownWidget : GlanceAppWidget() {

	// 只有 4×2 一种形态，用 Exact 拿真实 dp：Responsive 会按预设档位替换宽度，
	// 真机把卡片拉宽后右侧面板的位置就对不上了。
	override val sizeMode: SizeMode = SizeMode.Exact

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val snapshot = CountdownWidgetComponent.of(context).readSnapshot()
		val today = LocalDate.now(CampusClock.clock())
		val view = CountdownWidgetPresenter.present(snapshot, today)
		provideContent {
			CountdownWidgetContent(context = context, view = view)
		}
	}

	companion object {
		val widgetId: WidgetId = WidgetId.SemesterCountdown
	}
}
