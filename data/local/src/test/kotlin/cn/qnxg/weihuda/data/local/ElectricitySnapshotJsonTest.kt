// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local

import cn.qnxg.weihuda.core.entity.DormElectricity
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.data.local.snapshot.ElectricitySnapshotJson
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 宿舍电量快照的往返；数字必须原样回去，否则 186.5 会在桌面上变成别的读数。 */
class ElectricitySnapshotJsonTest {

	private val reading = DormElectricity(remainingKwh = 186.5)

	private val snapshot = ElectricityBalance.loaded(reading, Instant.ofEpochMilli(1_700_000_000_000))

	@Test
	fun `snapshot survives a round trip`() {
		assertEquals(snapshot, ElectricitySnapshotJson.decode(ElectricitySnapshotJson.encode(snapshot)))
	}

	@Test
	fun `signed out snapshot keeps no reading`() {
		val signedOut = ElectricityBalance.signedOut(Instant.ofEpochMilli(1))

		val restored = ElectricitySnapshotJson.decode(ElectricitySnapshotJson.encode(signedOut))

		assertEquals(ElectricityBalance.State.SIGNED_OUT, restored?.state)
		assertNull(restored?.electricity)
	}

	@Test
	fun `empty or garbage text decodes to nothing`() {
		assertNull(ElectricitySnapshotJson.decode(null))
		assertNull(ElectricitySnapshotJson.decode(""))
		assertNull(ElectricitySnapshotJson.decode("{"))
	}

	@Test
	fun `unknown format version is dropped instead of guessed`() {
		val text = """{"version":99,"state":"LOADED","refreshedAt":0,"remainingKwh":186.5}"""

		assertNull(ElectricitySnapshotJson.decode(text))
	}

	@Test
	fun `failure message and the last reading are preserved across restarts`() {
		val failed = ElectricityBalance.failed("尚不支持你的宿舍", reading, Instant.ofEpochMilli(1))

		val restored = ElectricitySnapshotJson.decode(ElectricitySnapshotJson.encode(failed))

		assertEquals("尚不支持你的宿舍", restored?.failure)
		assertEquals(reading, restored?.electricity)
	}
}
