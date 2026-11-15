package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.demo.DemoData
import cn.qnxg.weihuda.core.domain.CoursePeriods
import cn.qnxg.weihuda.core.domain.CourseStatus
import cn.qnxg.weihuda.core.domain.SnapshotCodec
import cn.qnxg.weihuda.core.domain.SnapshotFactory
import cn.qnxg.weihuda.core.domain.normalizeDay
import cn.qnxg.weihuda.core.domain.sessionsOn
import cn.qnxg.weihuda.core.domain.statusAt
import cn.qnxg.weihuda.core.domain.weekOf
import cn.qnxg.weihuda.core.model.CourseItem
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimetableTest {

	private fun course(
		name: String = "高等数学（一）",
		day: Int = 1,
		time: Int = 1,
		place: String = "复临舍101",
		weeks: List<Int> = (1..16).toList(),
		teacher: String = "张伟",
	) = CourseItem(
		courseName = name,
		courseId = "GE01101",
		type = "必修",
		className = "计科2301",
		place = place,
		area = "南校区",
		teacher = teacher,
		weeks = weeks,
		day = day,
		time = time,
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
		val sessions = listOf(
			course(time = 1),
			course(time = 2),
		).sessionsOn(day = 1, week = 10, now = LocalTime.of(7, 0))

		assertEquals(1, sessions.size)
		assertEquals(1, sessions.first().startPeriod)
		assertEquals(2, sessions.first().endPeriod)
		assertEquals(LocalTime.of(8, 0), sessions.first().startTime)
		assertEquals(LocalTime.of(9, 40), sessions.first().endTime)
	}

	@Test
	fun `non consecutive periods stay separate`() {
		val sessions = listOf(
			course(time = 1),
			course(time = 3),
		).sessionsOn(day = 1, week = 10, now = null)

		assertEquals(2, sessions.size)
		assertEquals(listOf(1, 3), sessions.map { it.startPeriod })
	}

	@Test
	fun `different place or teacher must not merge`() {
		val sessions = listOf(
			course(time = 1, place = "复临舍101"),
			course(time = 2, place = "复临舍203"),
			course(time = 3, teacher = "李娜"),
		).sessionsOn(day = 1, week = 10, now = null)

		assertEquals(3, sessions.size)
	}

	@Test
	fun `sessions are filtered by week and day`() {
		val items = listOf(
			course(day = 1, weeks = (1..8).toList()),
			course(day = 2, weeks = (1..16).toList()),
		)

		assertTrue(items.sessionsOn(day = 1, week = 10).isEmpty())
		assertEquals(1, items.sessionsOn(day = 2, week = 10).size)
	}

	@Test
	fun `sunday can be expressed as zero or seven`() {
		assertEquals(7, normalizeDay(0))
		assertEquals(7, normalizeDay(7))
		assertEquals(1, normalizeDay(1))

		val sunday = course(day = 7, time = 1)
		assertEquals(1, listOf(sunday).sessionsOn(day = 0, week = 10).size)
	}

	@Test
	fun `course status follows the 20 minute warning window`() {
		val start = LocalTime.of(8, 0)
		val end = LocalTime.of(9, 40)

		assertEquals(CourseStatus.UPCOMING, statusAt(start, end, LocalTime.of(7, 39)))
		assertEquals(CourseStatus.SOON, statusAt(start, end, LocalTime.of(7, 41)))
		assertEquals(CourseStatus.ONGOING, statusAt(start, end, LocalTime.of(8, 0)))
		assertEquals(CourseStatus.ONGOING, statusAt(start, end, LocalTime.of(9, 0)))
		assertEquals(CourseStatus.ONGOING, statusAt(start, end, LocalTime.of(9, 40)))
		assertEquals(CourseStatus.FINISHED, statusAt(start, end, LocalTime.of(9, 41)))
		assertEquals(CourseStatus.UPCOMING, statusAt(start, end, null))
	}

	@Test
	fun `period times match the school timetable`() {
		assertEquals("08:00-08:45", CoursePeriods.label(1))
		assertEquals("14:30-15:15", CoursePeriods.label(5))
		assertEquals("21:35-22:20", CoursePeriods.label(12))
	}

	@Test
	fun `demo snapshot is serializable and keeps today sessions`() {
		val today = LocalDate.of(2026, 11, 16) // 周一
		val snapshot = SnapshotFactory.of(
			courses = DemoData.courses(),
			source = "demo",
			week = weekOf(DemoData.semesterStart(today), today),
			date = today,
			now = LocalTime.of(9, 0),
		)

		assertEquals(2, snapshot.sessions.size)
		assertEquals("高等数学（一）", snapshot.sessions.first().name)
		assertEquals(CourseStatus.ONGOING, snapshot.sessions.first().statusEnum)
		assertEquals(1, snapshot.weekday)

		val restored = SnapshotCodec.decode(SnapshotCodec.encode(snapshot))
		assertEquals(snapshot, restored)
	}

	@Test
	fun `failure snapshot keeps previous sessions of the same day`() {
		val today = LocalDate.of(2026, 11, 16)
		val previous = SnapshotFactory.of(
			courses = DemoData.courses(),
			source = "demo",
			week = 10,
			date = today,
			now = LocalTime.of(7, 0),
		)
		val failed = SnapshotFactory.failure(
			date = today,
			week = 10,
			source = "api",
			message = "网络请求失败",
			previous = previous,
		)

		assertEquals(2, failed.sessions.size)
		assertEquals("网络请求失败", failed.error)

		val otherDay = SnapshotFactory.failure(
			date = today.plusDays(1),
			week = 10,
			source = "api",
			message = "网络请求失败",
			previous = previous,
		)
		assertTrue(otherDay.sessions.isEmpty())
	}
}
