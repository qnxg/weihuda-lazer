package cn.qnxg.weihuda.data.local

import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.data.local.snapshot.SnapshotJson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 快照格式的往返测试：格式演进、损坏数据、坏时刻都不能让桌面崩。 */
class SnapshotJsonTest {

	private val snapshot = TodayCourses(
		date = LocalDate.of(2026, 11, 16),
		week = 10,
		source = TodayCourses.Source.REMOTE,		studentName = "李同学",
		sessions = listOf(
			CourseSession(
				name = "操作系统",
				place = "软件大楼301",
				teacher = "孙敏",
				startPeriod = 1,
				endPeriod = 2,
				startTime = LocalTime.of(8, 0),
				endTime = LocalTime.of(9, 40),
			),
		),
		failure = null,
		refreshedAt = Instant.ofEpochMilli(1_700_000_000_000),
	)

	@Test
	fun `snapshot survives a round trip`() {
		assertEquals(snapshot, SnapshotJson.decode(SnapshotJson.encode(snapshot)))
	}

	@Test
	fun `empty or garbage text decodes to nothing`() {
		assertNull(SnapshotJson.decode(null))
		assertNull(SnapshotJson.decode(""))
		assertNull(SnapshotJson.decode("{"))
	}

	@Test
	fun `unknown format version is dropped instead of guessed`() {
		val text = """{"version":99,"date":"2026-11-16","week":10,"source":"REMOTE","refreshedAt":0}"""

		assertNull(SnapshotJson.decode(text))
	}

	@Test
	fun `failure message is preserved across restarts`() {
		val failed = snapshot.copy(failure = "网络请求失败", sessions = emptyList())

		val restored = SnapshotJson.decode(SnapshotJson.encode(failed))

		assertEquals("网络请求失败", restored?.failure)
		assertEquals(emptyList(), restored?.sessions)
	}
}
