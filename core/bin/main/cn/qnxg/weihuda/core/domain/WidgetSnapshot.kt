package cn.qnxg.weihuda.core.domain

import cn.qnxg.weihuda.core.model.CourseItem
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** 小组件展示用的单节课快照，已经是「合并连续小节」之后的结果。 */
@Serializable
data class SessionSnapshot(
	val name: String,
	val place: String? = null,
	val teacher: String? = null,
	val startPeriod: Int = 1,
	val endPeriod: Int = 1,
	val startTime: String = "",
	val endTime: String = "",
	val status: String = CourseStatus.UPCOMING.name,
) {
	val statusEnum: CourseStatus
		get() = runCatching { CourseStatus.valueOf(status) }.getOrDefault(CourseStatus.UPCOMING)

	val periodLabel: String
		get() = if (startPeriod == endPeriod) "$startPeriod" else "$startPeriod-$endPeriod"
}

/**
 * 小组件的完整渲染快照。
 *
 * 小组件进程不直接发请求，只读这份结构；[error] 非空时表示最近一次刷新失败，
 * 此时 [sessions] 仍然保留上一次成功的数据，避免桌面突然空白。
 */
@Serializable
data class WidgetSnapshot(
	val generatedAt: Long = 0L,
	val source: String = SOURCE_DEMO,
	val date: String = "",
	val weekday: Int = 1,
	val week: Int = 0,
	val userName: String? = null,
	val error: String? = null,
	val sessions: List<SessionSnapshot> = emptyList(),
) {
	val isDemo: Boolean get() = source == SOURCE_DEMO
	val hasCourses: Boolean get() = sessions.isNotEmpty()
	val allFinished: Boolean
		get() = sessions.isNotEmpty() && sessions.all { it.statusEnum == CourseStatus.FINISHED }

	val localDate: LocalDate?
		get() = runCatching { LocalDate.parse(date) }.getOrNull()

	fun generatedAtLabel(): String {
		if (generatedAt <= 0L) return ""
		val time = Instant.ofEpochMilli(generatedAt).atZone(ZONE).toLocalTime()
		return time.format(TIME_FORMAT)
	}

	companion object {
		const val SOURCE_DEMO = "demo"
		const val SOURCE_API = "api"

		val ZONE: java.time.ZoneId = java.time.ZoneId.of("Asia/Shanghai")
		private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	}
}

object SnapshotFactory {
	fun of(
		courses: List<CourseItem>,
		source: String,
		week: Int,
		date: LocalDate,
		now: LocalTime,
		userName: String? = null,
		error: String? = null,
		generatedAt: Long = System.currentTimeMillis(),
	): WidgetSnapshot = WidgetSnapshot(
		generatedAt = generatedAt,
		source = source,
		date = date.toString(),
		weekday = date.dayOfWeek.value,
		week = week,
		userName = userName,
		error = error,
		sessions = courses.sessionsOn(date.dayOfWeek.value, week, now).map { session ->
			SessionSnapshot(
				name = session.name,
				place = session.place,
				teacher = session.teacher,
				startPeriod = session.startPeriod,
				endPeriod = session.endPeriod,
				startTime = session.startTime.toString(),
				endTime = session.endTime.toString(),
				status = session.status.name,
			)
		},
	)

	fun failure(
		date: LocalDate,
		week: Int,
		source: String,
		message: String,
		previous: WidgetSnapshot? = null,
	): WidgetSnapshot {
		val keepPrevious = previous != null && previous.date == date.toString()
		return WidgetSnapshot(
			generatedAt = System.currentTimeMillis(),
			source = source,
			date = date.toString(),
			weekday = date.dayOfWeek.value,
			week = week,
			userName = previous?.userName,
			error = message,
			sessions = if (keepPrevious) previous!!.sessions else emptyList(),
		)
	}
}
