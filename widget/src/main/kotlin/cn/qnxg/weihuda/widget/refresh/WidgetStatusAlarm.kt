// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.widget.CourseWidgetComponent
import cn.qnxg.weihuda.widget.CourseWidgetReceiver
import java.time.ZonedDateTime

/**
 * 课程状态边界闹钟。
 *
 * 状态不是定时器，而是渲染时按当前时间算出来的；所以只要在边界点把小组件重绘一次，
 * 「未开始 → 即将开始 → 上课中 → 已结束」就会在那一刻切过去，不需要轮询网络。
 * 开课前 20 分钟、开始、结束后的下一秒和午夜各排一次，取最近的一个。
 */
internal object WidgetStatusAlarm {
	const val ACTION = "cn.qnxg.weihuda.widget.STATUS_TICK"

	/** 固定 requestCode，同一时间只保留一个边界闹钟。 */
	private const val REQUEST_CODE = 4101

	suspend fun scheduleNext(context: Context) {
		val appContext = context.applicationContext
		if (!hasWidgets(appContext)) {
			cancel(appContext)
			return
		}

		val snapshot = CourseWidgetComponent.of(appContext).readSnapshot()
		val now = ZonedDateTime.now(CampusClock.zone)
		val today = now.toLocalDate()
		val next = if (snapshot == null || snapshot.date != today) {
			// 跨天先交给周期刷新取新快照；这里保证午夜会醒一次，重新排当天的边界
			today.plusDays(1).atStartOfDay(now.zone)
		} else {
			nextStatusBoundary(snapshot.sessions, today, now)
		}
		set(appContext, next.toInstant().toEpochMilli())
	}

	fun cancel(context: Context) {
		val appContext = context.applicationContext
		val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
		alarmManager.cancel(pendingIntent(appContext))
	}

	private fun hasWidgets(context: Context): Boolean =
		AppWidgetManager.getInstance(context)
			.getAppWidgetIds(ComponentName(context, CourseWidgetReceiver::class.java))
			.isNotEmpty()

	@SuppressLint("ScheduleExactAlarm")
	private fun set(context: Context, triggerAtMillis: Long) {
		val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
		val pendingIntent = pendingIntent(context)
		alarmManager.cancel(pendingIntent)

		val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
		if (exact) {
			try {
				alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					triggerAtMillis,
					pendingIntent,
				)
				return
			} catch (_: SecurityException) {
				// 权限刚好被撤销：退回不精确的闹钟，状态最多晚几分钟
			}
		}
		alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
	}

	private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
		context,
		REQUEST_CODE,
		Intent(context, StatusTickReceiver::class.java).setAction(ACTION),
		PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
	)
}
