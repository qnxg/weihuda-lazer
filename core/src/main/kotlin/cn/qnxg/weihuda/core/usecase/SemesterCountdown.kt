// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.Semester
import java.time.Instant

/**
 * 「学期倒计时」用例的输出数据，同时是小组件快照的内容。
 *
 * 只存学期日期本身：阶段与剩余天数在渲染时按当前日期算，写进快照必然过期。
 */
data class SemesterCountdown(
	/** 没有数据（首次刷新就失败）时为 null。 */
	val semester: Semester?,
	/** 最近一次刷新失败的原因；非空时 [semester] 仍是上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	companion object {
		fun loaded(semester: Semester, refreshedAt: Instant): SemesterCountdown =
			SemesterCountdown(semester = semester, failure = null, refreshedAt = refreshedAt)

		/**
		 * 刷新失败：保留上一次成功取到的学期日期。
		 *
		 * 与课表不同，这里不按天丢弃：假期与开学都是绝对日期，旧数据最多是「不知道学校改了校历」，
		 * 而空着一张倒计时卡片没有任何用。阶段仍在渲染时按当前日期重算，所以留下的数据不会说谎。
		 */
		fun failed(message: String, previous: Semester?, refreshedAt: Instant): SemesterCountdown =
			SemesterCountdown(semester = previous, failure = message, refreshedAt = refreshedAt)
	}
}
