// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.core.entity.focus
import cn.qnxg.weihuda.core.entity.isPlausibleWeek
import cn.qnxg.weihuda.core.usecase.TodayCourses
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 把用例输出变成视图模型。
 *
 * 状态在渲染时用 [now] 现算：快照可能已经放了十五分钟，屏幕上的「上课中」不能停在写入时刻。
 */
object CourseWidgetPresenter {
	private val clockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	private val weekdays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

	fun present(snapshot: TodayCourses?, now: LocalTime): CourseWidgetView {
		snapshot ?: return CourseWidgetView(
			weekLabel = "微生活课表",
			dayLabel = "",
			dayLabelShort = "",
			rows = emptyList(),
			focus = null,
			focusTotalLabel = null,
			emptyTitle = "正在获取课表",
			emptyDetail = "点按应用完成设置或刷新",
			failureLabel = null,
			sourceLabel = "等待首次刷新",
		)

		val rows = snapshot.sessions.map { it.toRow(now) }
		val focusSession = snapshot.sessions.focus(now)
		val signedOut = snapshot.state == TodayCourses.State.SIGNED_OUT

		return CourseWidgetView(
			weekLabel = when {
				signedOut -> "微生活课表"
				isPlausibleWeek(snapshot.week) -> "第 ${snapshot.week} 周"
				else -> "假期"
			},
			dayLabel = "${weekday(snapshot.date)} ${monthDay(snapshot.date)}",
			dayLabelShort = weekday(snapshot.date),
			rows = rows,
			focus = focusSession?.toFocus(now),
			focusTotalLabel = if (snapshot.sessions.size > 1) "今日共 ${snapshot.sessions.size} 节" else null,
			emptyTitle = when {
				!signedOut && snapshot.sessions.isNotEmpty() -> null
				signedOut -> "未登录"
				snapshot.state == TodayCourses.State.FAILED -> "刷新失败"
				else -> "今日无课"
			},
			emptyDetail = when {
				!signedOut && snapshot.sessions.isNotEmpty() -> null
				signedOut -> "登录校园账号以使用此组件"
				snapshot.state == TodayCourses.State.FAILED -> snapshot.failure
				else -> "今天没有排课，点按查看完整课表"
			},
			failureLabel = snapshot.failure
				?.takeIf { snapshot.sessions.isNotEmpty() }
				?.let { "上次刷新失败：$it" },
			sourceLabel = buildString {
				append(if (signedOut) "未登录" else "实时数据")
				append(" · ")
				append(snapshot.refreshedAt.atZone(CampusClock.zone).format(clockFormat))
			},
		)
	}

	private fun CourseSession.toRow(now: LocalTime): SessionRow = SessionRow(
		name = name,
		place = place?.takeIf { it.isNotBlank() },
		periodLabel = periodLabel,
		startTime = startTime.format(clockFormat),
		endTime = endTime.format(clockFormat),
		status = statusAt(now),
	)

	private fun CourseSession.toFocus(now: LocalTime): FocusCard {
		val status = statusAt(now)
		return FocusCard(
			timeRange = "${startTime.format(clockFormat)} - ${endTime.format(clockFormat)}",
			name = name,
			detail = listOfNotNull(
				place?.takeIf { it.isNotBlank() },
				status.label.takeIf { status != CourseStatus.UPCOMING },
			).joinToString(" · "),
			status = status,
		)
	}

	private fun weekday(date: LocalDate): String = weekdays[(date.dayOfWeek.value - 1).coerceIn(0, 6)]

	/** 与小程序一致：`9/21`，不带年份。 */
	private fun monthDay(date: LocalDate): String = "${date.monthValue}/${date.dayOfMonth}"
}
