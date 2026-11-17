// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local

import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.data.local.snapshot.CountdownSnapshotJson
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 倒计时快照的往返：缺字段、坏日期与未知版本都必须退回「没有快照」，而不是猜。 */
class CountdownSnapshotJsonTest {

	private val semester = Semester(
		year = 2026,
		term = 1,
		startDate = LocalDate.of(2026, 9, 13),
		vacation = LocalDate.of(2027, 1, 3),
		next = LocalDate.of(2027, 2, 21),
	)

	private val snapshot = SemesterCountdown.loaded(semester, Instant.ofEpochMilli(1_700_000_000_000))

	@Test
	fun `snapshot survives a round trip`() {
		assertEquals(snapshot, CountdownSnapshotJson.decode(CountdownSnapshotJson.encode(snapshot)))
	}

	@Test
	fun `missing vacation and next date stay missing`() {
		val bare = SemesterCountdown.loaded(
			semester.copy(vacation = null, next = null),
			Instant.ofEpochMilli(1_700_000_000_000),
		)

		assertEquals(bare, CountdownSnapshotJson.decode(CountdownSnapshotJson.encode(bare)))
	}

	@Test
	fun `failure and the dates it kept survive a restart`() {
		val failed = SemesterCountdown.failed("网络请求失败", semester, Instant.ofEpochMilli(1))

		val restored = CountdownSnapshotJson.decode(CountdownSnapshotJson.encode(failed))

		assertEquals("网络请求失败", restored?.failure)
		assertEquals(semester, restored?.semester)
	}

	@Test
	fun `empty or garbage text decodes to nothing`() {
		assertNull(CountdownSnapshotJson.decode(null))
		assertNull(CountdownSnapshotJson.decode(""))
		assertNull(CountdownSnapshotJson.decode("{"))
	}

	@Test
	fun `unknown format version is dropped instead of guessed`() {
		val text = """{"version":99,"refreshedAt":0,"year":2026,"term":1,"startDate":"2026-09-13"}"""

		assertNull(CountdownSnapshotJson.decode(text))
	}

	@Test
	fun `a snapshot without a readable start date has no semester`() {
		val text = """{"version":1,"refreshedAt":0,"year":2026,"term":1,"startDate":"第三周"}"""

		val restored = CountdownSnapshotJson.decode(text)

		assertNull(restored?.semester)
	}
}
