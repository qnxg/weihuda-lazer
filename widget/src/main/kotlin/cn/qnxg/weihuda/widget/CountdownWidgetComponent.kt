// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.RefreshSemesterCountdown
import cn.qnxg.weihuda.core.usecase.RefreshSemesterCountdownInteractor
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.widget.refresh.CountdownDayAlarm
import cn.qnxg.weihuda.widget.refresh.CountdownWidgetRefresh

/**
 * 「学期倒计时」卡片的门面：只负责这张卡片自己的取数、快照、排期与添加。
 *
 * 它与其它卡片共用 [WidgetLibrary]（后端地址、HTTP、存储），但**不需要登录**：
 * `/info/smester` 是公开接口，未登录时这张卡片照样有内容。
 */
class CountdownWidgetComponent internal constructor(private val library: WidgetLibrary) {

	private val widgetId: WidgetId = CountdownWidget.widgetId

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取学期信息」。 */
	suspend fun readSnapshot(): SemesterCountdown? = library.snapshots.readSemesterCountdown(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): SemesterCountdown {
		val snapshot = newInteractor().refresh()
		CountdownWidget().updateAll(library.appContext)
		// 与课表卡片同样的前台重绘兜底，见 CourseWidgetReceiver.requestRerender
		CountdownWidgetReceiver.requestRerender(library.appContext)
		// 学期日期可能刚好在这一刻换掉（开学/放假），下次午夜闹钟要按新的快照重新排
		CountdownDayAlarm.scheduleNext(library.appContext)
		return snapshot
	}

	/** 一次性刷新（应用内「立即刷新」）。 */
	fun requestRefresh() = CountdownWidgetRefresh.enqueueNow(library.appContext)

	fun ensureScheduled() = CountdownWidgetRefresh.ensurePeriodic(library.appContext)

	fun cancelScheduled() = CountdownWidgetRefresh.cancelAll(library.appContext)

	fun pinToHomeScreen(): Boolean = library.pin(CountdownWidgetReceiver::class.java, widgetId)

	fun pinnedWidgetCount(): Int = library.pinnedCount(CountdownWidgetReceiver::class.java)

	private suspend fun newInteractor(): RefreshSemesterCountdown = RefreshSemesterCountdownInteractor(
		snapshots = library.snapshots,
		semesters = library.newBackend(),
		clock = CampusClock.clock(),
		widgetId = widgetId,
	)

	companion object {
		fun of(context: Context): CountdownWidgetComponent = WidgetLibrary.of(context).countdownWidget
	}
}
