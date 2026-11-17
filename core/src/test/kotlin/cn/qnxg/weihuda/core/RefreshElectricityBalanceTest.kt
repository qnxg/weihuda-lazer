// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.DormElectricity
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.DormElectricityGateway
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshElectricityBalanceInteractor
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

/** 电量用例测试：不启动 Android，也不发 HTTP。 */
class RefreshElectricityBalanceTest {

	private val now: Instant = Instant.parse("2026-11-17T02:00:00Z")
	private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

	private val reading = DormElectricity(remainingKwh = 186.5)

	private val settings = FakeSettingsStore(Settings(baseUrl = "https://example.invalid"))
	private val snapshots = FakeSnapshotStore()
	private val gateway = FakeElectricityGateway(reading)

	private fun interactor() = RefreshElectricityBalanceInteractor(
		settings = settings,
		snapshots = snapshots,
		electricity = gateway,
		clock = clock,
	)

	private fun signIn(token: String = "jwt") {
		settings.current = settings.current.copy(credentials = Credentials(token))
	}

	@Test
	fun `no credentials means signed out and no network calls`() = runBlocking {
		val result = interactor().refresh()

		assertEquals(ElectricityBalance.State.SIGNED_OUT, result.state)
		assertNull(result.electricity)
		assertEquals(0, gateway.calls)
		assertEquals(result, snapshots.writtenElectricity)
	}

	@Test
	fun `remote refresh stores the reading and the refresh time`() = runBlocking {
		signIn()

		val result = interactor().refresh()

		assertEquals(ElectricityBalance.State.LOADED, result.state)
		assertEquals(reading, result.electricity)
		assertEquals(now, result.refreshedAt)
		assertNull(result.failure)
		assertEquals(1, gateway.calls)
	}

	@Test
	fun `failure keeps the last reading and records why`() = runBlocking {
		signIn()
		snapshots.storedElectricity =
			ElectricityBalance.loaded(reading, Instant.parse("2026-11-17T00:00:00Z"))
		gateway.failure = GatewayException.network("网络请求失败")

		val failed = interactor().refresh()

		assertEquals(ElectricityBalance.State.LOADED, failed.state)
		assertEquals(reading, failed.electricity)
		assertEquals("网络请求失败", failed.failure)
	}

	@Test
	fun `backend refusal such as an unsupported dormitory surfaces as a failure`() = runBlocking {
		signIn()
		gateway.failure = GatewayException(code = 500, message = "尚不支持你的宿舍")

		val failed = interactor().refresh()

		assertEquals(ElectricityBalance.State.FAILED, failed.state)
		assertNull(failed.electricity)
		assertEquals("尚不支持你的宿舍", failed.failure)
	}

	@Test
	fun `cancellation is rethrown instead of becoming a failure snapshot`() = runBlocking {
		signIn()
		gateway.failure = CancellationException("cancelled")

		assertFailsWith<CancellationException> { interactor().refresh() }
		assertTrue(snapshots.writtenElectricity == null)
	}

	private class FakeSettingsStore(var current: Settings) : SettingsStore {
		override suspend fun load(): Settings = current

		override suspend fun save(settings: Settings) {
			current = settings
		}
	}

	private class FakeSnapshotStore : SnapshotStore {
		var storedElectricity: ElectricityBalance? = null
		var writtenElectricity: ElectricityBalance? = null

		override suspend fun readTodayCourses(id: WidgetId): TodayCourses? = null

		override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) = Unit

		override suspend fun readNetworkTraffic(id: WidgetId): NetworkTraffic? = null

		override suspend fun writeNetworkTraffic(id: WidgetId, snapshot: NetworkTraffic) = Unit

		override suspend fun readCardBalance(id: WidgetId): CardBalance? = null

		override suspend fun writeCardBalance(id: WidgetId, snapshot: CardBalance) = Unit

		override suspend fun readElectricityBalance(id: WidgetId): ElectricityBalance? = storedElectricity

		override suspend fun writeElectricityBalance(id: WidgetId, snapshot: ElectricityBalance) {
			storedElectricity = snapshot
			writtenElectricity = snapshot
		}

		override suspend fun readSemesterCountdown(id: WidgetId): SemesterCountdown? = null

		override suspend fun writeSemesterCountdown(id: WidgetId, snapshot: SemesterCountdown) = Unit
	}

	private class FakeElectricityGateway(private val reading: DormElectricity) : DormElectricityGateway {
		var calls = 0
		var failure: Exception? = null

		override suspend fun remaining(credentials: Credentials): DormElectricity {
			calls++
			failure?.let { throw it }
			return reading
		}
	}
}
