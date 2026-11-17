// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.qnxg.weihuda.widget.CountdownWidgetReceiver

/** 日期边界闹钟的落点：只重绘快照并排下一天，不在这里取数。 */
class CountdownTickReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action != CountdownDayAlarm.ACTION) return

		val appContext = context.applicationContext
		CountdownWidgetReceiver.requestRerender(appContext)
		CountdownDayAlarm.scheduleNext(appContext)
	}
}
