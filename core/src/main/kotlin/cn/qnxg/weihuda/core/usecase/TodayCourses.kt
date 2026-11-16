// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
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
	val state: State,
	val studentName: String?,
	val studentCollege: String?,
	val sessions: List<CourseSession>,
	/** 最近一次刷新失败的原因；非空时 [sessions] 仍是当天上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	enum class State {
		/** 没有登录：没有课表可展示，界面只能引导去登录。 */
		SIGNED_OUT,

		/** 有数据；[failure] 非空说明这是上一次成功的结果。 */
		LOADED,

		/** 没有数据，且刷新失败过。 */
		FAILED,
	}

	companion object {
		/** 未登录时的输出：清空一切与账号有关的内容，避免退回界面时看到上一个人的名字。 */
		fun signedOut(date: LocalDate, refreshedAt: Instant): TodayCourses = TodayCourses(
			date = date,
			week = 0,
			state = State.SIGNED_OUT,
			studentName = null,
			studentCollege = null,
			sessions = emptyList(),
			failure = null,
			refreshedAt = refreshedAt,
		)

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
			val keep = previous?.takeIf { it.date == date && it.sessions.isNotEmpty() }
			return TodayCourses(
				date = date,
				week = week.takeIf { it > 0 } ?: keep?.week ?: 0,
				state = if (keep != null) State.LOADED else State.FAILED,
				studentName = keep?.studentName,
				studentCollege = keep?.studentCollege,
				sessions = keep?.sessions.orEmpty(),
				failure = message,
				refreshedAt = refreshedAt,
			)
		}
	}
}
