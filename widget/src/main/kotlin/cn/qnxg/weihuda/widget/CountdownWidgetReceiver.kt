// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import cn.qnxg.weihuda.widget.refresh.CountdownDayAlarm
import cn.qnxg.weihuda.widget.refresh.CountdownWidgetRefresh

/**
 * 倒计时卡片的广播入口。
 *
 * 与另外两张卡片同样的规矩：系统每次要求更新时先渲染旧快照（保证立刻有内容），
 * 再排一次后台刷新与午夜的日期边界闹钟；receiver 只负责渲染与排期，不负责取数。
 */
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

	override fun onReceive(context: Context, intent: Intent) {
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
		CountdownWidgetRefresh.ensurePeriodic(context)
		CountdownWidgetRefresh.enqueueNow(context)
		CountdownDayAlarm.scheduleNext(context)
	}

	override fun onEnabled(context: Context) {
		super.onEnabled(context)
		CountdownWidgetRefresh.ensurePeriodic(context)
		CountdownWidgetRefresh.enqueueNow(context)
		CountdownDayAlarm.scheduleNext(context)
	}

	override fun onDisabled(context: Context) {
		super.onDisabled(context)
		CountdownWidgetRefresh.cancelAll(context)
		CountdownDayAlarm.cancel(context)
	}

	companion object {
		private const val ACTION_RERENDER = "cn.qnxg.weihuda.widget.COUNTDOWN_RERENDER"

		/** 应用内刷新或午夜到点后的重绘请求：只渲染，不再排刷新（天数由快照里的日期现算）。 */
		fun requestRerender(context: Context) {
			val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
				ComponentName(context, CountdownWidgetReceiver::class.java),
			)
			if (ids.isEmpty()) return

			context.sendBroadcast(
				Intent(context, CountdownWidgetReceiver::class.java).apply {
					action = ACTION_RERENDER
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
				},
			)
		}
	}
}
