// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

import java.time.LocalTime

/** 一节课的状态。[CourseStatus.label] 是给界面用的文案，随实体一起走，避免各端各写一套。 */
enum class CourseStatus {
	/** 尚未开始 */
	UPCOMING,

	/** 即将开始（开课前 20 分钟内） */
	SOON,

	/** 正在上课 */
	ONGOING,

	/** 已经下课 */
	FINISHED,
	;

	val label: String
		get() = when (this) {
			UPCOMING -> "未开始"
			SOON -> "即将开始"
			ONGOING -> "上课中"
			FINISHED -> "已结束"
		}
}

/**
 * 合并连续小节之后的一节课，是小组件真正展示的单元。
 *
 * 这里**不存状态**：状态取决于「看表的时刻」，而快照要一直活到下一次刷新。
 * 渲染时用 [statusAt] 现算，屏幕上的「上课中」才不会停留在 15 分钟前。
 */
data class CourseSession(
	val name: String,
	val place: String?,
	val teacher: String?,
	val startPeriod: Int,
	val endPeriod: Int,
	val startTime: LocalTime,
	val endTime: LocalTime,
) {
	/** 与小程序保持一致：`1-2`、`5-6` 这样的节次文案。 */
	val periodLabel: String
		get() = if (startPeriod == endPeriod) "$startPeriod" else "$startPeriod-$endPeriod"

	/** 是否与另一节课的时间重叠。 */
	fun overlaps(other: CourseSession): Boolean =
		startPeriod <= other.endPeriod && other.startPeriod <= endPeriod

	fun statusAt(now: LocalTime): CourseStatus = when {
		now < startTime.minusMinutes(SOON_WINDOW_MINUTES) -> CourseStatus.UPCOMING
		now < startTime -> CourseStatus.SOON
		now <= endTime -> CourseStatus.ONGOING
		else -> CourseStatus.FINISHED
	}
}

/** 开课前多久算「即将开始」。 */
const val SOON_WINDOW_MINUTES: Long = 20L

/**
 * 取某一天、某一周要上的课，并把连续小节合并成一节。
 *
 * 合并规则与小程序一致：课程名、课程代码、自定义 ID、教师、地点、班级全部相同，
 * 且小节次连续时才合并；任何字段不同都必须拆开。
 */
fun List<Course>.sessionsOn(dayOfWeek: Int, week: Int): List<CourseSession> {
	val targetDay = normalizeDayOfWeek(dayOfWeek)

	data class Key(
		val name: String,
		val code: String?,
		val customizeId: Int,
		val teacher: String?,
		val place: String?,
		val className: String?,
	)

	val grouped = filter { course ->
		normalizeDayOfWeek(course.dayOfWeek) == targetDay && week in course.weeks
	}.groupBy { course ->
		Key(
			name = course.name,
			code = course.code,
			customizeId = course.customizeId,
			teacher = course.teacher,
			place = course.place,
			className = course.className,
		)
	}

	return grouped.values.flatMap { courses ->
		consecutiveRuns(courses.map { it.period }.distinct().sorted()).mapNotNull { run ->
			val startTime = CoursePeriods.start(run.first) ?: return@mapNotNull null
			val endTime = CoursePeriods.end(run.last) ?: return@mapNotNull null
			val first = courses.first()
			CourseSession(
				name = first.name,
				place = first.place,
				teacher = first.teacher,
				startPeriod = run.first,
				endPeriod = run.last,
				startTime = startTime,
				endTime = endTime,
			)
		}
	}.sortedWith(compareBy({ it.startPeriod }, { it.name }))
}

/** 当前正在上、或者接下来要上的那节课；全部结束后退回最后一节。 */
fun List<CourseSession>.focus(now: LocalTime): CourseSession? =
	firstOrNull { it.statusAt(now) == CourseStatus.ONGOING }
		?: firstOrNull { it.statusAt(now) != CourseStatus.FINISHED }
		?: lastOrNull()

private fun consecutiveRuns(periods: List<Int>): List<IntRange> {
	if (periods.isEmpty()) return emptyList()
	val runs = mutableListOf<IntRange>()
	var start = periods.first()
	var prev = start
	for (period in periods.drop(1)) {
		if (period == prev + 1) {
			prev = period
		} else {
			runs += start..prev
			start = period
			prev = period
		}
	}
	runs += start..prev
	return runs
}
