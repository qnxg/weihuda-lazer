// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * 精确闹钟的落地方式。
 *
 * 课表的状态边界与倒计时的日期边界都只需要「到点醒一次」，差别只在触发时刻，
 * 所以权限判断与低版本退回只有这一份：Android 12+ 需要 `SCHEDULE_EXACT_ALARM`，
 * 没授予时退回 `setAndAllowWhileIdle`，可能晚几分钟。
 */
internal object WidgetAlarm {
	@SuppressLint("ScheduleExactAlarm")
	fun set(context: Context, triggerAtMillis: Long, pendingIntent: PendingIntent) {
		val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
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
				// 权限刚好被撤销：退回不精确的闹钟，最多晚几分钟
			}
		}
		alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
	}

	fun cancel(context: Context, pendingIntent: PendingIntent) {
		context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent)
	}
}
