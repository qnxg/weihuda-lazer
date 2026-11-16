// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.CoursePeriods
import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.core.entity.focus
import cn.qnxg.weihuda.core.entity.normalizeDayOfWeek
import cn.qnxg.weihuda.core.entity.sessionsOn
import cn.qnxg.weihuda.core.entity.weekOf
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 实体规则：周次、小节合并、状态。这些是「不管哪个端都必须一致」的部分。 */
class TimetableTest {

	private fun course(
		name: String = "高等数学（一）",
		day: Int = 1,
		period: Int = 1,
		place: String = "复临舍101",
		weeks: List<Int> = (1..16).toList(),
		teacher: String = "张伟",
	) = Course(
		name = name,
		code = "GE01101",
		kind = "必修",
		className = "计科2301",
		place = place,
		campus = "南校区",
		teacher = teacher,
		weeks = weeks,
		dayOfWeek = day,
		period = period,
	)

	@Test
	fun `week number starts at one on semester start`() {
		val start = LocalDate.of(2026, 9, 13)
		assertEquals(1, weekOf(start, start))
		assertEquals(2, weekOf(start, start.plusDays(7)))
		assertEquals(10, weekOf(start, LocalDate.of(2026, 11, 16)))
		assertEquals(0, weekOf(start, start.minusDays(1)))
	}

	@Test
	fun `consecutive periods merge into one session`() {
		val sessions = listOf(course(period = 1), course(period = 2))
			.sessionsOn(dayOfWeek = 1, week = 10)

		assertEquals(1, sessions.size)
		val only = sessions.single()
		assertEquals(1, only.startPeriod)
		assertEquals(2, only.endPeriod)
		assertEquals(LocalTime.of(8, 0), only.startTime)
		assertEquals(LocalTime.of(9, 40), only.endTime)
		assertEquals("1-2", only.periodLabel)
	}

	@Test
	fun `non consecutive periods stay separate`() {
		val sessions = listOf(course(period = 1), course(period = 3))
			.sessionsOn(dayOfWeek = 1, week = 10)

		assertEquals(listOf(1, 3), sessions.map { it.startPeriod })
	}

	@Test
	fun `different place or teacher must not merge`() {
		val sessions = listOf(
			course(period = 1, place = "复临舍101"),
			course(period = 2, place = "复临舍203"),
			course(period = 3, teacher = "李娜"),
		).sessionsOn(dayOfWeek = 1, week = 10)

		assertEquals(3, sessions.size)
	}

	@Test
	fun `sessions are filtered by week and day`() {
		val items = listOf(
			course(day = 1, weeks = (1..8).toList()),
			course(day = 2, weeks = (1..16).toList()),
		)

		assertTrue(items.sessionsOn(dayOfWeek = 1, week = 10).isEmpty())
		assertEquals(1, items.sessionsOn(dayOfWeek = 2, week = 10).size)
	}

	@Test
	fun `sunday can be expressed as zero or seven`() {
		assertEquals(7, normalizeDayOfWeek(0))
		assertEquals(7, normalizeDayOfWeek(7))
		assertEquals(1, normalizeDayOfWeek(1))

		val sunday = course(day = 7)
		assertEquals(1, listOf(sunday).sessionsOn(dayOfWeek = 0, week = 10).size)
	}

	@Test
	fun `course status follows the 20 minute warning window`() {
		val session = CourseSession(
			name = "高等数学（一）",
			place = null,
			teacher = null,
			startPeriod = 1,
			endPeriod = 2,
			startTime = LocalTime.of(8, 0),
			endTime = LocalTime.of(9, 40),
		)

		assertEquals(CourseStatus.UPCOMING, session.statusAt(LocalTime.of(7, 39)))
		assertEquals(CourseStatus.SOON, session.statusAt(LocalTime.of(7, 41)))
		assertEquals(CourseStatus.ONGOING, session.statusAt(LocalTime.of(8, 0)))
		assertEquals(CourseStatus.ONGOING, session.statusAt(LocalTime.of(9, 40)))
		assertEquals(CourseStatus.FINISHED, session.statusAt(LocalTime.of(9, 41)))
	}

	@Test
	fun `focus prefers the ongoing session then the next one`() {
		val morning = session(startPeriod = 1, startTime = LocalTime.of(8, 0), endTime = LocalTime.of(9, 40))
		val afternoon = session(startPeriod = 5, startTime = LocalTime.of(14, 30), endTime = LocalTime.of(16, 0))

		assertEquals(morning, listOf(morning, afternoon).focus(LocalTime.of(9, 0)))
		assertEquals(afternoon, listOf(morning, afternoon).focus(LocalTime.of(12, 0)))
		assertEquals(afternoon, listOf(morning, afternoon).focus(LocalTime.of(23, 0)))
	}

	@Test
	fun `period times match the school timetable`() {
		assertEquals(LocalTime.of(8, 0), CoursePeriods.start(1))
		assertEquals(LocalTime.of(8, 45), CoursePeriods.end(1))
		assertEquals(LocalTime.of(14, 30), CoursePeriods.start(5))
		assertEquals(LocalTime.of(22, 20), CoursePeriods.end(12))
		assertEquals(null, CoursePeriods.start(13))
	}

	private fun session(startPeriod: Int, startTime: LocalTime, endTime: LocalTime) = CourseSession(
		name = "第 $startPeriod 节",
		place = null,
		teacher = null,
		startPeriod = startPeriod,
		endPeriod = startPeriod + 1,
		startTime = startTime,
		endTime = endTime,
	)
}
