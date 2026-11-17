// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.data.local.snapshot.NetworkSnapshotJson
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 流量快照的往返：格式演进、损坏数据与未知状态都不能让桌面崩或显示错位数据。 */
class NetworkSnapshotJsonTest {

	private val account = CampusNetwork(
		usedGb = 12.8,
		quotaGb = 40.0,
		usedRatio = 0.32,
		status = CampusNetwork.AccountStatus.NORMAL,
		overdueYuan = 0.0,
	)

	private val snapshot = NetworkTraffic.loaded(account, Instant.ofEpochMilli(1_700_000_000_000))

	@Test
	fun `snapshot survives a round trip`() {
		assertEquals(snapshot, NetworkSnapshotJson.decode(NetworkSnapshotJson.encode(snapshot)))
	}

	@Test
	fun `signed out snapshot keeps no account`() {
		val signedOut = NetworkTraffic.signedOut(Instant.ofEpochMilli(1_700_000_000_000))

		val restored = NetworkSnapshotJson.decode(NetworkSnapshotJson.encode(signedOut))

		assertEquals(NetworkTraffic.State.SIGNED_OUT, restored?.state)
		assertNull(restored?.account)
	}

	@Test
	fun `empty or garbage text decodes to nothing`() {
		assertNull(NetworkSnapshotJson.decode(null))
		assertNull(NetworkSnapshotJson.decode(""))
		assertNull(NetworkSnapshotJson.decode("{"))
	}

	@Test
	fun `unknown format version is dropped instead of guessed`() {
		val text =
			"""{"version":99,"state":"LOADED","refreshedAt":0,"usedGb":12.8,"quotaGb":40.0}"""

		assertNull(NetworkSnapshotJson.decode(text))
	}

	@Test
	fun `failure message and old data are preserved across restarts`() {
		val failed = NetworkTraffic.failed("网络请求失败", account, Instant.ofEpochMilli(1))

		val restored = NetworkSnapshotJson.decode(NetworkSnapshotJson.encode(failed))

		assertEquals("网络请求失败", restored?.failure)
		assertEquals(account, restored?.account)
	}
}
