// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.CampusCard
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.CampusCardGateway
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshCardBalanceInteractor
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.core.usecase.TodayCourses
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 余额用例测试：不启动 Android，也不发 HTTP。 */
class RefreshCardBalanceTest {

	private val now: Instant = Instant.parse("2026-11-17T02:00:00Z")
	private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

	private val card = CampusCard(balanceYuan = 86.5)

	private val settings = FakeSettingsStore(Settings(baseUrl = "https://example.invalid"))
	private val snapshots = FakeSnapshotStore()
	private val cards = FakeCampusCardGateway(card)

	private fun interactor() = RefreshCardBalanceInteractor(
		settings = settings,
		snapshots = snapshots,
		cards = cards,
		clock = clock,
	)

	private fun signIn(token: String = "jwt") {
		settings.current = settings.current.copy(credentials = Credentials(token))
	}

	@Test
	fun `no credentials means signed out and no network calls`() = runBlocking {
		val result = interactor().refresh()

		assertEquals(CardBalance.State.SIGNED_OUT, result.state)
		assertNull(result.card)
		assertEquals(0, cards.calls)
		assertEquals(result, snapshots.writtenCard)
	}

	@Test
	fun `blank token is treated as signed out`() = runBlocking {
		signIn(token = "   ")

		assertEquals(CardBalance.State.SIGNED_OUT, interactor().refresh().state)
		assertEquals(0, cards.calls)
	}

	@Test
	fun `remote refresh stores the balance and the refresh time`() = runBlocking {
		signIn()

		val result = interactor().refresh()

		assertEquals(CardBalance.State.LOADED, result.state)
		assertEquals(card, result.card)
		assertEquals(now, result.refreshedAt)
		assertNull(result.failure)
		assertEquals(1, cards.calls)
		assertEquals(result, snapshots.writtenCard)
	}

	@Test
	fun `failure keeps the last known balance and records why`() = runBlocking {
		signIn()
		snapshots.storedCard = CardBalance.loaded(card, Instant.parse("2026-11-16T02:00:00Z"))
		cards.failure = GatewayException.network("网络请求失败")

		val failed = interactor().refresh()

		assertEquals(CardBalance.State.LOADED, failed.state)
		assertEquals(card, failed.card)
		assertEquals("网络请求失败", failed.failure)
		assertEquals(now, failed.refreshedAt)
	}

	@Test
	fun `first refresh failure has no balance to keep`() = runBlocking {
		signIn()
		cards.failure = GatewayException(code = 401, message = "令牌已过期")

		val failed = interactor().refresh()

		assertEquals(CardBalance.State.FAILED, failed.state)
		assertNull(failed.card)
		assertEquals("令牌已过期", failed.failure)
	}

	@Test
	fun `cancellation is rethrown instead of becoming a failure snapshot`() = runBlocking {
		signIn()
		cards.failure = CancellationException("cancelled")

		assertFailsWith<CancellationException> { interactor().refresh() }
		assertTrue(snapshots.writtenCard == null)
	}

	private class FakeSettingsStore(var current: Settings) : SettingsStore {
		override suspend fun load(): Settings = current

		override suspend fun save(settings: Settings) {
			current = settings
		}
	}

	private class FakeSnapshotStore : SnapshotStore {
		var storedCard: CardBalance? = null
		var writtenCard: CardBalance? = null

		override suspend fun readTodayCourses(id: WidgetId): TodayCourses? = null

		override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) = Unit

		override suspend fun readNetworkTraffic(id: WidgetId): NetworkTraffic? = null

		override suspend fun writeNetworkTraffic(id: WidgetId, snapshot: NetworkTraffic) = Unit

		override suspend fun readCardBalance(id: WidgetId): CardBalance? = storedCard

		override suspend fun writeCardBalance(id: WidgetId, snapshot: CardBalance) {
			storedCard = snapshot
			writtenCard = snapshot
		}

		override suspend fun readSemesterCountdown(id: WidgetId): SemesterCountdown? = null

		override suspend fun writeSemesterCountdown(id: WidgetId, snapshot: SemesterCountdown) = Unit

		override suspend fun readElectricityBalance(id: WidgetId): ElectricityBalance? = null

		override suspend fun writeElectricityBalance(id: WidgetId, snapshot: ElectricityBalance) = Unit
	}

	private class FakeCampusCardGateway(private val card: CampusCard) : CampusCardGateway {
		var calls = 0
		var failure: Exception? = null

		override suspend fun card(credentials: Credentials): CampusCard {
			calls++
			failure?.let { throw it }
			return card
		}
	}
}
