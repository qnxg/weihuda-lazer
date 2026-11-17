// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshSemesterCountdownInteractor
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.core.usecase.TodayCourses
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * 倒计时用例：不需要令牌，失败时保留上一次的学期日期。
 *
 * 固定时钟设在 2026-11-17（秋季学期进行中），与小程序、截图里的「还有 47 天」对得上。
 */
class RefreshSemesterCountdownTest {

	private val clock: Clock = Clock.fixed(Instant.parse("2026-11-17T02:00:00Z"), ZoneId.of("Asia/Shanghai"))

	private val semester = Semester(
		year = 2026,
		term = 1,
		startDate = LocalDate.of(2026, 9, 13),
		vacation = LocalDate.of(2027, 1, 3),
		next = LocalDate.of(2027, 2, 21),
	)

	private val snapshots = FakeSnapshotStore()
	private val semesters = FakeSemesterGateway(semester)

	private fun interactor() = RefreshSemesterCountdownInteractor(
		snapshots = snapshots,
		semesters = semesters,
		clock = clock,
	)

	@Test
	fun `semester info is stored as the snapshot`() = runBlocking {
		val result = interactor().refresh()

		assertEquals(semester, result.semester)
		assertEquals(null, result.failure)
		assertEquals(Instant.parse("2026-11-17T02:00:00Z"), result.refreshedAt)
		assertEquals(result, snapshots.written)
		assertEquals(WidgetId.SemesterCountdown, snapshots.writtenId)
	}

	@Test
	fun `failure keeps the previous dates from another day`() = runBlocking {
		// 学期日期是绝对日期，跨天不会变错，所以前一天的结果仍然可用
		snapshots.stored = SemesterCountdown.loaded(semester, Instant.parse("2026-11-10T02:00:00Z"))
		semesters.failure = GatewayException.network("网络请求失败")

		val result = interactor().refresh()

		assertEquals(semester, result.semester)
		assertEquals("网络请求失败", result.failure)
	}

	@Test
	fun `first refresh failure leaves the card without data`() = runBlocking {
		semesters.failure = GatewayException(code = 500, message = "接口返回异常")

		val result = interactor().refresh()

		assertEquals(null, result.semester)
		assertEquals("接口返回异常", result.failure)
		assertTrue(result.failure != null)
	}

	@Test
	fun `a failure without a message still gets a readable reason`() = runBlocking {
		semesters.failure = RuntimeException()

		assertEquals("学期信息刷新失败", interactor().refresh().failure)
	}

	@Test
	fun `cancellation is rethrown and nothing is written`() = runBlocking {
		semesters.failure = CancellationException("cancelled")

		assertFailsWith<CancellationException> { interactor().refresh() }
		assertTrue(snapshots.written == null)
	}

	private class FakeSnapshotStore : SnapshotStore {
		var stored: SemesterCountdown? = null
		var written: SemesterCountdown? = null
		var writtenId: WidgetId? = null

		override suspend fun readTodayCourses(id: WidgetId): TodayCourses? = null

		override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) = Unit

		override suspend fun readNetworkTraffic(id: WidgetId): NetworkTraffic? = null

		override suspend fun writeNetworkTraffic(id: WidgetId, snapshot: NetworkTraffic) = Unit

		override suspend fun readSemesterCountdown(id: WidgetId): SemesterCountdown? = stored

		override suspend fun writeSemesterCountdown(id: WidgetId, snapshot: SemesterCountdown) {
			writtenId = id
			stored = snapshot
			written = snapshot
		}
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
}
