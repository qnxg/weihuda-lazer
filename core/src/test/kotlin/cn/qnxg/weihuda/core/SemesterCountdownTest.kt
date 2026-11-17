// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.SemesterPhase
import cn.qnxg.weihuda.core.entity.countdownOn
import cn.qnxg.weihuda.core.entity.phaseOn
import cn.qnxg.weihuda.core.entity.teachingWeeks
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 学期倒计时的规则：阶段、总周数与目标日期都在那里，界面只摆放结果。
 *
 * 日期取自 `weihuda_backend` 的学期开始日期表：2026 秋季学期 9/13 开学、次年 1/3 放假，
 * 正好 16 周。
 */
class SemesterCountdownTest {

	private val semester = Semester(
		year = 2026,
		term = 1,
		startDate = LocalDate.of(2026, 9, 13),
		vacation = LocalDate.of(2027, 1, 3),
		next = LocalDate.of(2027, 2, 21),
	)

	@Test
	fun `semester name follows the campus wording`() {
		assertEquals("2026 秋季学期", semester.name)
		assertEquals("2026 春季学期", semester.copy(term = 2).name)
	}

	@Test
	fun `phase is before start day one and in term from the start date`() {
		assertEquals(SemesterPhase.BEFORE_START, semester.phaseOn(LocalDate.of(2026, 9, 12)))
		assertEquals(SemesterPhase.IN_TERM, semester.phaseOn(LocalDate.of(2026, 9, 13)))
	}

	@Test
	fun `the last day still counts as in term and the day after starts the vacation`() {
		assertEquals(SemesterPhase.IN_TERM, semester.phaseOn(LocalDate.of(2027, 1, 3)))
		assertEquals(SemesterPhase.AFTER_TERM, semester.phaseOn(LocalDate.of(2027, 1, 4)))
	}

	@Test
	fun `missing vacation date keeps the term running instead of declaring a holiday`() {
		assertEquals(SemesterPhase.IN_TERM, semester.copy(vacation = null).phaseOn(LocalDate.of(2027, 3, 1)))
	}

	@Test
	fun `teaching weeks come from the distance between start and vacation`() {
		assertEquals(16, semester.teachingWeeks())
	}

	@Test
	fun `a calendar that does not end on a week boundary is rounded to the nearest week`() {
		assertEquals(16, semester.copy(vacation = LocalDate.of(2027, 1, 5)).teachingWeeks())
	}

	@Test
	fun `teaching weeks are unknown without a vacation date or with a broken one`() {
		assertNull(semester.copy(vacation = null).teachingWeeks())
		assertNull(semester.copy(vacation = LocalDate.of(2026, 9, 1)).teachingWeeks())
	}

	@Test
	fun `in term the countdown points at the end of the semester`() {
		val target = semester.countdownOn(LocalDate.of(2026, 11, 17))

		assertEquals(SemesterPhase.IN_TERM, target?.phase)
		assertEquals(LocalDate.of(2027, 1, 3), target?.date)
		assertEquals(47L, target?.daysLeft)
	}

	@Test
	fun `on the last day the countdown is zero rather than negative`() {
		assertEquals(0L, semester.countdownOn(LocalDate.of(2027, 1, 3))?.daysLeft)
	}

	@Test
	fun `during the vacation the countdown points at the next semester start`() {
		val target = semester.countdownOn(LocalDate.of(2027, 1, 20))

		assertEquals(SemesterPhase.AFTER_TERM, target?.phase)
		assertEquals(LocalDate.of(2027, 2, 21), target?.date)
		assertEquals(32L, target?.daysLeft)
	}

	@Test
	fun `before the term the countdown points at the start date`() {
		val target = semester.countdownOn(LocalDate.of(2026, 9, 10))

		assertEquals(SemesterPhase.BEFORE_START, target?.phase)
		assertEquals(LocalDate.of(2026, 9, 13), target?.date)
		assertEquals(3L, target?.daysLeft)
	}

	@Test
	fun `a missing target date yields no countdown instead of a fake zero`() {
		assertNull(semester.copy(vacation = null).countdownOn(LocalDate.of(2026, 11, 17)))
		assertNull(
			semester.copy(vacation = LocalDate.of(2027, 1, 3), next = null)
				.countdownOn(LocalDate.of(2027, 1, 20)),
		)
	}
}
