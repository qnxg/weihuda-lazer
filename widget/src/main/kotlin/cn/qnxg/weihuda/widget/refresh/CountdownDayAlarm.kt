// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.widget.CountdownWidgetReceiver
import java.time.ZonedDateTime

/**
 * 倒计时的日期边界闹钟。
 *
 * 剩余天数是「渲染时按今天算」的，所以只要在午夜把卡片重绘一次，「还有 47 天」就会变成「还有 46 天」。
 * 这条路径不取数：快照里是绝对日期，离线也照样能算对。
 */
internal object CountdownDayAlarm {
	const val ACTION = "cn.qnxg.weihuda.widget.COUNTDOWN_DAY_TICK"

	/** 固定 requestCode，同一时间只保留一个日期闹钟。 */
	private const val REQUEST_CODE = 4201

	fun scheduleNext(context: Context) {
		val appContext = context.applicationContext
		if (!hasWidgets(appContext)) {
			cancel(appContext)
			return
		}

		// 校区时区的午夜，而不是设备时区：天数按长沙时间的「今天」算
		val now = ZonedDateTime.now(CampusClock.zone)
		val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(CampusClock.zone)
		WidgetAlarm.set(appContext, nextMidnight.toInstant().toEpochMilli(), pendingIntent(appContext))
	}

	fun cancel(context: Context) {
		val appContext = context.applicationContext
		WidgetAlarm.cancel(appContext, pendingIntent(appContext))
	}

	private fun hasWidgets(context: Context): Boolean =
		AppWidgetManager.getInstance(context)
			.getAppWidgetIds(ComponentName(context, CountdownWidgetReceiver::class.java))
			.isNotEmpty()

	private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
		context,
		REQUEST_CODE,
		Intent(context, CountdownTickReceiver::class.java).setAction(ACTION),
		PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
	)
}
