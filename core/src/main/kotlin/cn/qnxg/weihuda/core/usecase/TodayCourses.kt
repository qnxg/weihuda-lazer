package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CourseSession
import java.time.Instant
import java.time.LocalDate

/**
 * 「今日课程」用例的输出数据。
 *
 * 它同时是小组件快照的内容：只有已经算好的、与界面无关的事实，没有任何格式化的字符串。
 * 状态不在这里（见 `CourseSession`），界面渲染时现算。
 */
data class TodayCourses(
	val date: LocalDate,
	val week: Int,
	val source: Source,
	val studentName: String?,
	val sessions: List<CourseSession>,
	/** 最近一次刷新失败的原因；非空时 [sessions] 仍是当天上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	enum class Source {
		/** 内置演示课表，不联网。 */
		DEMO,

		/** 来自微生活后端。 */
		REMOTE,
	}

	val isDemo: Boolean get() = source == Source.DEMO

	companion object {
		/**
		 * 刷新失败时的输出：同一天的上一次结果原样保留，只把失败原因写进去。
		 *
		 * 换了一天就丢弃旧课表——昨天的课留在今天的卡片上是错的信息，比空白更糟。
		 */
		fun ofFailure(
			date: LocalDate,
			week: Int,
			message: String,
			previous: TodayCourses?,
			refreshedAt: Instant,
		): TodayCourses {
			val keep = previous?.takeIf { it.date == date }
			return TodayCourses(
				date = date,
				week = week.takeIf { it > 0 } ?: keep?.week ?: 0,
				source = keep?.source ?: Source.REMOTE,
				studentName = keep?.studentName,
				sessions = keep?.sessions.orEmpty(),
				failure = message,
				refreshedAt = refreshedAt,
			)
		}
	}
}
