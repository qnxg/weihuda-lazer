// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh

/**
 * 小组件的广播入口。
 *
 * 系统每次要求更新时先渲染旧快照（保证立刻有内容），再排一次后台刷新。
 * 加新小组件时的规矩同此：receiver 只负责渲染与排期，不负责取数。
 */
class CourseWidgetReceiver : GlanceAppWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget = CourseWidget()

	override fun onReceive(context: Context, intent: Intent) {
		// 应用内刷新后的重绘请求：只渲染，不再排刷新（调用方刚刚取过数）
		if (intent.action == ACTION_RERENDER) {
			val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: IntArray(0)
			if (ids.isNotEmpty()) {
				super.onUpdate(context, AppWidgetManager.getInstance(context), ids)
			}
			return
		}
		super.onReceive(context, intent)
	}

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray,
	) {
		super.onUpdate(context, appWidgetManager, appWidgetIds)
		WidgetRefresh.ensurePeriodic(context)
		WidgetRefresh.enqueueNow(context)
	}

	override fun onEnabled(context: Context) {
		super.onEnabled(context)
		WidgetRefresh.ensurePeriodic(context)
		WidgetRefresh.enqueueNow(context)
	}

	override fun onDisabled(context: Context) {
		super.onDisabled(context)
		WidgetRefresh.cancelAll(context)
	}

	companion object {
		private const val ACTION_RERENDER = "cn.qnxg.weihuda.widget.RERENDER"

		/**
		 * 请系统再推一次内容。
		 *
		 * 真机（MIUI 13 / Android 13）上，应用处于前台时 `GlanceAppWidget.updateAll` 写入的
		 * RemoteViews 有时不会落到已经绑定的实例上：用户在应用里点了「保存并刷新」，
		 * 回到桌面看到的还是上一帧，直到下一个 15 分钟周期。走一次广播让 receiver 重新
		 * 渲染快照，是唯一在真机上稳定的重绘路径。
		 */
		fun requestRerender(context: Context) {
			val manager = AppWidgetManager.getInstance(context)
			val ids = manager.getAppWidgetIds(
				android.content.ComponentName(context, CourseWidgetReceiver::class.java),
			)
			if (ids.isEmpty()) return

			context.sendBroadcast(
				Intent(context, CourseWidgetReceiver::class.java).apply {
					action = ACTION_RERENDER
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
				},
			)
		}
	}
}
