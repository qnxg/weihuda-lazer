// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.RefreshTodayCourses
import cn.qnxg.weihuda.core.usecase.RefreshTodayCoursesInteractor
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh
import cn.qnxg.weihuda.widget.refresh.WidgetStatusAlarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 「今日课程」卡片的门面：只负责这张卡片自己的取数、快照、排期与添加。
 *
 * 登录、后端地址这类跨卡片的配置在 [WidgetLibrary]，所以这里没有`signIn`，也不需要重复装配存储。
 */
class CourseWidgetComponent internal constructor(private val library: WidgetLibrary) {

	private val widgetId: WidgetId = CourseWidget.widgetId

	/** 桌面当前展示的快照；没有时返回 null，由 presenter 给出「正在获取课表」。 */
	suspend fun readSnapshot(): TodayCourses? = library.snapshots.readTodayCourses(widgetId)

	/**
	 * 立刻取一次数据：写快照，然后让桌面上所有本类小组件重新组合。
	 *
	 * 失败已经在用例里变成带 `failure` 的正常返回，所以这里不抛异常。
	 */
	suspend fun refresh(): TodayCourses {
		val snapshot = newInteractor().refresh()
		CourseWidget().updateAll(library.appContext)
		// updateAll 在应用前台时可能不生效，见 CourseWidgetReceiver.requestRerender
		CourseWidgetReceiver.requestRerender(library.appContext)
		WidgetStatusAlarm.scheduleNext(library.appContext)
		return snapshot
	}

	/** 节流后的一次性刷新（点小组件里的「刷新」）。 */
	fun requestRefresh() = WidgetRefresh.enqueueNow(library.appContext)

	/** 应用启动时保证周期任务与状态边界闹钟都存在。 */
	fun ensureScheduled() {
		WidgetRefresh.ensurePeriodic(library.appContext)
		CoroutineScope(Dispatchers.IO).launch { WidgetStatusAlarm.scheduleNext(library.appContext) }
	}

	/** 最后一个小组件被移除后调用，停止后台刷新与状态闹钟。 */
	fun cancelScheduled() {
		WidgetRefresh.cancelAll(library.appContext)
		WidgetStatusAlarm.cancel(library.appContext)
	}

	fun pinToHomeScreen(): Boolean = library.pin(CourseWidgetReceiver::class.java, widgetId)

	fun pinnedWidgetCount(): Int = library.pinnedCount(CourseWidgetReceiver::class.java)

	private suspend fun newInteractor(): RefreshTodayCourses {
		val backend = library.newBackend()
		return RefreshTodayCoursesInteractor(
			settings = library.settingsStore,
			snapshots = library.snapshots,
			semesters = backend,
			courses = backend,
			students = backend,
			clock = CampusClock.clock(),
			widgetId = widgetId,
		)
	}

	companion object {
		fun of(context: Context): CourseWidgetComponent = WidgetLibrary.of(context).courseWidget
	}
}
