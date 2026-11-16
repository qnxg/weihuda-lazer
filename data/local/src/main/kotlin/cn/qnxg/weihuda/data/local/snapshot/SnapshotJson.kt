package cn.qnxg.weihuda.data.local.snapshot

import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.usecase.TodayCourses
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * 快照的落盘格式。
 *
 * [version] 让格式可以在不改 port 的前提下演进：读到不认识的版本就当没有快照，
 * 桌面退回「正在获取课表」，而不是崩溃或显示错位的数据。
 * 版本 2 把「演示/实时」的来源字段换成了三态 [state]，旧快照直接作废。
 */
@Serializable
internal data class SnapshotDto(
	val version: Int = VERSION,
	val date: String,
	val week: Int,
	val state: String,
	val studentName: String? = null,
	val studentCollege: String? = null,
	val failure: String? = null,
	val refreshedAt: Long,
	val sessions: List<SessionDto> = emptyList(),
) {
	companion object {
		const val VERSION: Int = 2
	}
}

@Serializable
internal data class SessionDto(
	val name: String,
	val place: String? = null,
	val teacher: String? = null,
	val startPeriod: Int = 1,
	val endPeriod: Int = 1,
	val startTime: String = "",
	val endTime: String = "",
)

/**
 * 快照的编解码。
 *
 * 不依赖 Android，所以格式本身可以在 JVM 上单测；时间用「日期 + 时刻 + 秒」存，
 * 避免机器时区变化后把今天的课读成昨天的。
 */
internal object SnapshotJson {
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
	}

	fun encode(snapshot: TodayCourses): String = json.encodeToString(
		SnapshotDto.serializer(),
		SnapshotDto(
			date = snapshot.date.toString(),
			week = snapshot.week,
			state = snapshot.state.name,
			studentName = snapshot.studentName,
			studentCollege = snapshot.studentCollege,
			failure = snapshot.failure,
			refreshedAt = snapshot.refreshedAt.toEpochMilli(),
			sessions = snapshot.sessions.map { session ->
				SessionDto(
					name = session.name,
					place = session.place,
					teacher = session.teacher,
					startPeriod = session.startPeriod,
					endPeriod = session.endPeriod,
					startTime = session.startTime.toString(),
					endTime = session.endTime.toString(),
				)
			},
		),
	)

	fun decode(text: String?): TodayCourses? {
		if (text.isNullOrBlank()) return null
		val dto = runCatching { json.decodeFromString(SnapshotDto.serializer(), text) }.getOrNull() ?: return null
		if (dto.version != SnapshotDto.VERSION) return null

		val date = runCatching { LocalDate.parse(dto.date) }.getOrNull() ?: return null
		return TodayCourses(
			date = date,
			week = dto.week,
			state = runCatching { TodayCourses.State.valueOf(dto.state) }
				.getOrDefault(TodayCourses.State.SIGNED_OUT),
			studentName = dto.studentName,
			studentCollege = dto.studentCollege,
			sessions = dto.sessions.mapNotNull { it.toEntity() },
			failure = dto.failure,
			refreshedAt = Instant.ofEpochMilli(dto.refreshedAt),
		)
	}

	private fun SessionDto.toEntity(): CourseSession? {
		val start = runCatching { LocalTime.parse(startTime) }.getOrNull() ?: return null
		val end = runCatching { LocalTime.parse(endTime) }.getOrNull() ?: return null
		return CourseSession(
			name = name,
			place = place,
			teacher = teacher,
			startPeriod = startPeriod,
			endPeriod = endPeriod,
			startTime = start,
			endTime = end,
		)
	}
}
