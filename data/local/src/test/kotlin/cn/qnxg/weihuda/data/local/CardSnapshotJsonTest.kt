// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local

import cn.qnxg.weihuda.core.entity.CampusCard
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.data.local.snapshot.CardSnapshotJson
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 校园卡快照的往返：格式演进、损坏数据与未知状态都不能让桌面崩或显示错位数据。 */
class CardSnapshotJsonTest {

	private val card = CampusCard(balanceYuan = 86.5)

	private val snapshot = CardBalance.loaded(card, Instant.ofEpochMilli(1_700_000_000_000))

	@Test
	fun `snapshot survives a round trip`() {
		assertEquals(snapshot, CardSnapshotJson.decode(CardSnapshotJson.encode(snapshot)))
	}

	@Test
	fun `signed out snapshot keeps no balance`() {
		val signedOut = CardBalance.signedOut(Instant.ofEpochMilli(1))

		val restored = CardSnapshotJson.decode(CardSnapshotJson.encode(signedOut))

		assertEquals(CardBalance.State.SIGNED_OUT, restored?.state)
		assertNull(restored?.card)
	}

	@Test
	fun `empty or garbage text decodes to nothing`() {
		assertNull(CardSnapshotJson.decode(null))
		assertNull(CardSnapshotJson.decode(""))
		assertNull(CardSnapshotJson.decode("{"))
	}

	@Test
	fun `unknown format version is dropped instead of guessed`() {
		val text = """{"version":99,"state":"LOADED","refreshedAt":0,"balanceYuan":86.5}"""

		assertNull(CardSnapshotJson.decode(text))
	}

	@Test
	fun `failure message and the last balance are preserved across restarts`() {
		val failed = CardBalance.failed("网络请求失败", card, Instant.ofEpochMilli(1))

		val restored = CardSnapshotJson.decode(CardSnapshotJson.encode(failed))

		assertEquals("网络请求失败", restored?.failure)
		assertEquals(card, restored?.card)
	}
}
