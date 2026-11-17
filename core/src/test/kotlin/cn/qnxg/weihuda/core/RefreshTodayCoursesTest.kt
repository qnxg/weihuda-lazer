// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.Student
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.CourseGateway
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.port.StudentGateway
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshTodayCoursesInteractor
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.core.usecase.TodayCourses
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 用例测试：不启动 Android，也不发 HTTP，全部外部能力都是假适配器。
 *
 * 固定时钟设在周一 2026-11-16 09:00（长沙时间 17:00 之前，用 UTC 时区偏移对齐即可）。
 */
class RefreshTodayCoursesTest {

	private val monday = LocalDate.of(2026, 11, 16)
	private val clock: Clock = Clock.fixed(Instant.parse("2026-11-16T01:00:00Z"), ZoneOffset.UTC)

	private val mondayCourse = Course(
		name = "操作系统",
		code = "CS30501",
		className = "计科2301",
		place = "软件大楼301",
		teacher = "孙敏",
		weeks = (1..16).toList(),
		dayOfWeek = 1,
		period = 1,
	)

	private val settings = FakeSettingsStore(Settings(baseUrl = "https://example.invalid"))
	private val snapshots = FakeSnapshotStore()
	private val semesters = FakeSemesterGateway(Semester(year = 2026, term = 1, startDate = monday.minusWeeks(9)))
	private val courses = FakeCourseGateway(listOf(mondayCourse))
	private val students = FakeStudentGateway(Student(name = "李同学", college = "电气与信息工程学院"))

	private fun interactor() = RefreshTodayCoursesInteractor(
		settings = settings,
		snapshots = snapshots,
		semesters = semesters,
		courses = courses,
		students = students,
		clock = clock,
	)

	private fun signIn(token: String = "jwt") {
		settings.current = settings.current.copy(credentials = Credentials(token))
	}

	@Test
	fun `no credentials means signed out and no network calls`() = runBlocking {
		val result = interactor().refresh()

		assertEquals(TodayCourses.State.SIGNED_OUT, result.state)
		assertTrue(result.sessions.isEmpty())
		assertNull(result.studentName)
		assertEquals(monday, result.date)
		assertEquals(0, semesters.calls)
		assertEquals(0, courses.calls)
		assertEquals(result, snapshots.written)
	}

	@Test
	fun `blank token is treated as signed out`() = runBlocking {
		signIn(token = "   ")

		val result = interactor().refresh()

		assertEquals(TodayCourses.State.SIGNED_OUT, result.state)
		assertEquals(0, courses.calls)
	}

	@Test
	fun `signing out drops the previous snapshot`() = runBlocking {
		signIn()
		val signedIn = interactor().refresh()
		assertEquals(TodayCourses.State.LOADED, signedIn.state)

		settings.current = settings.current.copy(credentials = Credentials(""))
		val signedOut = interactor().refresh()

		assertEquals(TodayCourses.State.SIGNED_OUT, signedOut.state)
		assertTrue(signedOut.sessions.isEmpty())
		assertNull(signedOut.studentName)
	}

	@Test
	fun `remote refresh merges sessions and records the semester week`() = runBlocking {
		signIn()
		courses.items = listOf(mondayCourse, mondayCourse.copy(period = 2))

		val result = interactor().refresh()

		assertEquals(TodayCourses.State.LOADED, result.state)
		assertEquals(10, result.week)
		assertEquals("李同学", result.studentName)
		assertEquals("电气与信息工程学院", result.studentCollege)
		assertEquals(1, result.sessions.size)
		assertEquals(2, result.sessions.single().endPeriod)
		assertNull(result.failure)
	}

	@Test
	fun `semester failure keeps the earlier snapshot of the same day`() = runBlocking {
		signIn()
		val first = interactor().refresh()
		semesters.failure = GatewayException.network("网络请求失败")

		val failed = interactor().refresh()

		assertEquals("网络请求失败", failed.failure)
		assertEquals(TodayCourses.State.LOADED, failed.state)
		assertEquals(first.sessions, failed.sessions)
		assertEquals("李同学", failed.studentName)
	}

	@Test
	fun `timetable failure drops the previous day sessions`() = runBlocking {
		snapshots.stored = TodayCourses(
			date = monday.minusDays(1),
			week = 9,
			state = TodayCourses.State.LOADED,
			studentName = "李同学",
			studentCollege = "电气与信息工程学院",
			sessions = emptyList(),
			failure = null,
			refreshedAt = Instant.EPOCH,
		)
		signIn()
		courses.failure = GatewayException(code = 401, message = "令牌已过期")

		val failed = interactor().refresh()

		assertEquals("令牌已过期", failed.failure)
		assertEquals(TodayCourses.State.FAILED, failed.state)
		assertTrue(failed.sessions.isEmpty())
		assertEquals(10, failed.week)
	}

	@Test
	fun `student failure does not fail the refresh`() = runBlocking {
		signIn()
		students.failure = GatewayException.network("用户信息超时")

		val result = interactor().refresh()

		assertNull(result.failure)
		assertNull(result.studentName)
		assertNull(result.studentCollege)
		assertEquals(1, result.sessions.size)
	}

	private class FakeSettingsStore(var current: Settings) : SettingsStore {
		override suspend fun load(): Settings = current

		override suspend fun save(settings: Settings) {
			current = settings
		}
	}

	private class FakeSnapshotStore : SnapshotStore {
		var stored: TodayCourses? = null
		var written: TodayCourses? = null

		override suspend fun readTodayCourses(id: WidgetId): TodayCourses? = stored

		override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) {
			stored = snapshot
			written = snapshot
		}

		override suspend fun readNetworkTraffic(id: WidgetId): NetworkTraffic? = null

		override suspend fun writeNetworkTraffic(id: WidgetId, snapshot: NetworkTraffic) = Unit

		override suspend fun readSemesterCountdown(id: WidgetId): SemesterCountdown? = null

		override suspend fun writeSemesterCountdown(id: WidgetId, snapshot: SemesterCountdown) = Unit
	}

	private class FakeSemesterGateway(private val semester: Semester) : SemesterGateway {
		var calls = 0
		var failure: Exception? = null

		override suspend fun current(): Semester {
			calls++
			failure?.let { throw it }
			return semester
		}
	}

	private class FakeCourseGateway(var items: List<Course>) : CourseGateway {
		var calls = 0
		var failure: Exception? = null

		override suspend fun timetable(semester: Semester, credentials: Credentials): List<Course> {
			calls++
			failure?.let { throw it }
			return items
		}
	}

	private class FakeStudentGateway(private val student: Student) : StudentGateway {
		var failure: Exception? = null

		override suspend fun current(credentials: Credentials): Student {
			failure?.let { throw it }
			return student
		}
	}
}
