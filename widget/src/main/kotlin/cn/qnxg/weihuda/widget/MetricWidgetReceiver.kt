// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 单值卡片共用的广播入口：先渲染旧快照保证桌上立刻有内容，再排一次后台刷新。
 *
 * receiver 只负责渲染与排期，不负责取数。两张单值卡片（余额 / 电量）在这一层没有任何差别，
 * 差别全在各自的排期对象上，所以共用一份实现；子类只声明自己的任务与重建广播。
 */
abstract class MetricWidgetReceiver : GlanceAppWidgetReceiver() {

	/** 应用内刷新后的重建广播，各卡片自带一个，避免两张卡片互相触发重绘。 */
	protected abstract val rerenderAction: String

	protected abstract fun ensureScheduled(context: Context)

	protected abstract fun enqueueRefresh(context: Context)

	protected abstract fun cancelScheduled(context: Context)

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == rerenderAction) {
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
		ensureScheduled(context)
		enqueueRefresh(context)
	}

	override fun onEnabled(context: Context) {
		super.onEnabled(context)
		ensureScheduled(context)
		enqueueRefresh(context)
	}

	override fun onDisabled(context: Context) {
		super.onDisabled(context)
		cancelScheduled(context)
	}

	companion object {
		/** 只渲染、不再排刷新：调用方刚刚取过数。 */
		internal fun requestRerender(
			context: Context,
			receiver: Class<out MetricWidgetReceiver>,
			action: String,
		) {
			val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, receiver))
			if (ids.isEmpty()) return

			context.sendBroadcast(
				Intent(context, receiver).apply {
					this.action = action
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
				},
			)
		}
	}
}
