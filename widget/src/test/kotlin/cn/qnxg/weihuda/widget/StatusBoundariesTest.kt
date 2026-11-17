// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.widget.refresh.nextStatusBoundary
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusBoundariesTest {

	private val date = LocalDate.of(2026, 11, 17)
	private val zone = CampusClock.zone
	private val morning = session(8, 0, 9, 40)
	private val afternoon = session(14, 30, 16, 0)

	@Test
	fun `next boundary is the twenty minute warning`() {
		assertEquals(at(7, 40), nextStatusBoundary(listOf(morning), date, at(7, 0)))
	}

	@Test
	fun `at the warning boundary the next stop is the class start`() {
		assertEquals(at(8, 0), nextStatusBoundary(listOf(morning), date, at(7, 40)))
	}

	@Test
	fun `class start is followed by the end plus one second`() {
		assertEquals(at(9, 40, 1), nextStatusBoundary(listOf(morning), date, at(8, 0)))
		assertEquals(at(9, 40, 1), nextStatusBoundary(listOf(morning), date, at(9, 40)))
	}

	@Test
	fun `after the morning the next stop is the afternoon warning`() {
		assertEquals(at(14, 10), nextStatusBoundary(listOf(morning, afternoon), date, at(9, 40, 1)))
	}

	@Test
	fun `after the last class the alarm waits for midnight`() {
		assertEquals(
			date.plusDays(1).atStartOfDay(zone),
			nextStatusBoundary(listOf(morning, afternoon), date, at(16, 0, 1)),
		)
	}

	@Test
	fun `no sessions still wakes at midnight`() {
		assertEquals(date.plusDays(1).atStartOfDay(zone), nextStatusBoundary(emptyList(), date, at(12, 0)))
	}

	private fun at(hour: Int, minute: Int, second: Int = 0): ZonedDateTime =
		ZonedDateTime.of(date, LocalTime.of(hour, minute, second), zone)

	private fun session(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) = CourseSession(
		name = "测试课",
		place = null,
		teacher = null,
		startPeriod = 1,
		endPeriod = 2,
		startTime = LocalTime.of(startHour, startMinute),
		endTime = LocalTime.of(endHour, endMinute),
	)
}
