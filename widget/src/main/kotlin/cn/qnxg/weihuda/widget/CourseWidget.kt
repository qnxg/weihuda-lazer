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
import cn.qnxg.weihuda.widget.presentation.CourseWidgetPresenter
import java.time.LocalTime

/**
 * 课程小组件。
 *
 * 只渲染：读 [cn.qnxg.weihuda.widget.CourseWidgetComponent] 里最新的快照，
 * 交给 presenter 变成文案，再摆到屏幕上。渲染路径上没有网络，也没有磁盘写入。
 */
class CourseWidget : GlanceAppWidget() {

	override val sizeMode: SizeMode = SizeMode.Responsive(
		setOf(COMPACT_SIZE, WIDE_SIZE, MEDIUM_SIZE, LARGE_SIZE),
	)

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val snapshot = CourseWidgetComponent.of(context).readSnapshot()
		val view = CourseWidgetPresenter.present(snapshot, LocalTime.now(CampusClock.clock()))
		provideContent {
			WidgetContent(context = context, view = view)
		}
	}

	companion object {
		val widgetId: WidgetId = WidgetId.TodayCourses
	}
}
