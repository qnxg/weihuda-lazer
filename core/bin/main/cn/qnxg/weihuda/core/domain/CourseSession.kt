package cn.qnxg.weihuda.core.domain

import cn.qnxg.weihuda.core.model.CourseItem
import java.time.LocalTime

/** 课程状态，用于小组件里的状态文字与颜色。 */
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

/** 合并连续小节之后的一节课，是小组件真正展示的单元。 */
data class CourseSession(
	val name: String,
	val place: String?,
	val teacher: String?,
	val startPeriod: Int,
	val endPeriod: Int,
	val startTime: LocalTime,
	val endTime: LocalTime,
	val status: CourseStatus,
) {
	/** 与小程序保持一致：`1-2`、`5-6` 这样的节次文案。 */
	val periodLabel: String
		get() = if (startPeriod == endPeriod) "$startPeriod" else "$startPeriod-$endPeriod"

	/** 是否与另一节课的时间重叠。 */
	fun overlaps(other: CourseSession): Boolean =
		startPeriod <= other.endPeriod && other.startPeriod <= endPeriod
}

private const val SOON_WINDOW_MINUTES = 20L

/**
 * 后端历史版本曾用 0 表示周日，这里统一成「周一 = 1 … 周日 = 7」。
 */
fun normalizeDay(day: Int): Int = when (day) {
	0, 7 -> 7
	else -> day.coerceIn(1, 7)
}

/**
 * 取某一天、某一周要上的课，并把连续小节合并成一节。
 *
 * 合并规则与小程序一致：课程名、课程代码、自定义 ID、教师、地点、班级全部相同，
 * 且小节次连续时才合并；任何字段不同都必须拆开。
 */
fun List<CourseItem>.sessionsOn(
	day: Int,
	week: Int,
	now: LocalTime? = null,
): List<CourseSession> {
	val targetDay = normalizeDay(day)

	data class Key(
		val name: String,
		val courseId: String?,
		val customizeId: Int,
		val teacher: String?,
		val place: String?,
		val className: String?,
	)

	val grouped = filter { item ->
		normalizeDay(item.day) == targetDay && week in item.weeks
	}.groupBy { item ->
		Key(
			name = item.courseName,
			courseId = item.courseId,
			customizeId = item.customizeId,
			teacher = item.teacher,
			place = item.place,
			className = item.className,
		)
	}

	return grouped.values.flatMap { items ->
		consecutiveRuns(items.map { it.time }.distinct().sorted()).mapNotNull { run ->
			val start = run.first
			val end = run.last
			val startTime = CoursePeriods.start(start) ?: return@mapNotNull null
			val endTime = CoursePeriods.end(end) ?: return@mapNotNull null
			val first = items.first()
			CourseSession(
				name = first.courseName,
				place = first.place,
				teacher = first.teacher,
				startPeriod = start,
				endPeriod = end,
				startTime = startTime,
				endTime = endTime,
				status = statusAt(startTime, endTime, now),
			)
		}
	}.sortedWith(compareBy({ it.startPeriod }, { it.name }))
}

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

fun statusAt(
	startTime: LocalTime,
	endTime: LocalTime,
	now: LocalTime?,
): CourseStatus {
	if (now == null) return CourseStatus.UPCOMING
	return when {
		now < startTime.minusMinutes(SOON_WINDOW_MINUTES) -> CourseStatus.UPCOMING
		now < startTime -> CourseStatus.SOON
		now <= endTime -> CourseStatus.ONGOING
		else -> CourseStatus.FINISHED
	}
}
