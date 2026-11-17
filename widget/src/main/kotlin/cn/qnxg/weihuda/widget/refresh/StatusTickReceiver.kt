// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.widget.CourseWidgetComponent
import cn.qnxg.weihuda.widget.CourseWidgetReceiver
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** 边界闹钟的落点：只重绘快照，不在这里取数。 */
class StatusTickReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action != WidgetStatusAlarm.ACTION) return

		val appContext = context.applicationContext
		val pending = goAsync()
		CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
			try {
				val snapshot = CourseWidgetComponent.of(appContext).readSnapshot()
				if (snapshot?.date == LocalDate.now(CampusClock.zone)) {
					CourseWidgetReceiver.requestRerender(appContext)
					WidgetStatusAlarm.scheduleNext(appContext)
				} else {
					// 跨天了：快照还是昨天的，先刷新再让刷新链路排新一天的闹钟
					WidgetRefresh.enqueueNow(appContext)
				}
			} finally {
				pending.finish()
			}
		}
	}
}
