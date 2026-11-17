// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.NetworkGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.core.usecase.RefreshNetworkTrafficInteractor
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

/**
 * 流量用例测试：不启动 Android，也不发 HTTP。
 *
 * 固定时钟设在 2026-11-17，方便验证「上个月的数字不能留到这个月」。
 */
class RefreshNetworkTrafficTest {

	private val now: Instant = Instant.parse("2026-11-17T02:00:00Z")
	private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
	private val november = Instant.parse("2026-11-02T02:00:00Z")
	private val october = Instant.parse("2026-10-28T02:00:00Z")

	private val account = CampusNetwork(
		usedGb = 12.8,
		quotaGb = 40.0,
		usedRatio = 0.32,
		status = CampusNetwork.AccountStatus.NORMAL,
		overdueYuan = 0.0,
	)

	private val settings = FakeSettingsStore(Settings(baseUrl = "https://example.invalid"))
	private val snapshots = FakeSnapshotStore()
	private val networks = FakeNetworkGateway(account)

	private fun interactor() = RefreshNetworkTrafficInteractor(
		settings = settings,
		snapshots = snapshots,
		networks = networks,
		clock = clock,
	)

	private fun signIn(token: String = "jwt") {
		settings.current = settings.current.copy(credentials = Credentials(token))
	}

	@Test
	fun `no credentials means signed out and no network calls`() = runBlocking {
		val result = interactor().refresh()

		assertEquals(NetworkTraffic.State.SIGNED_OUT, result.state)
		assertNull(result.account)
		assertEquals(0, networks.calls)
		assertEquals(result, snapshots.writtenNetwork)
	}

	@Test
	fun `blank token is treated as signed out`() = runBlocking {
		signIn(token = "   ")

		assertEquals(NetworkTraffic.State.SIGNED_OUT, interactor().refresh().state)
		assertEquals(0, networks.calls)
	}

	@Test
	fun `remote refresh stores the account and the refresh time`() = runBlocking {
		signIn()

		val result = interactor().refresh()

		assertEquals(NetworkTraffic.State.LOADED, result.state)
		assertEquals(account, result.account)
		assertEquals(now, result.refreshedAt)
		assertNull(result.failure)
		assertEquals(1, networks.calls)
		assertEquals(result, snapshots.writtenNetwork)
	}

	@Test
	fun `failure keeps the previous result of the same month`() = runBlocking {
		signIn()
		snapshots.storedNetwork = NetworkTraffic.loaded(account, november)
		networks.failure = GatewayException.network("网络请求失败")

		val failed = interactor().refresh()

		assertEquals(NetworkTraffic.State.LOADED, failed.state)
		assertEquals(account, failed.account)
		assertEquals("网络请求失败", failed.failure)
		assertEquals(now, failed.refreshedAt)
	}

	@Test
	fun `failure drops last month's numbers instead of showing them as this month`() = runBlocking {
		signIn()
		snapshots.storedNetwork = NetworkTraffic.loaded(account, october)
		networks.failure = GatewayException(code = 401, message = "令牌已过期")

		val failed = interactor().refresh()

		assertEquals(NetworkTraffic.State.FAILED, failed.state)
		assertNull(failed.account)
		assertEquals("令牌已过期", failed.failure)
	}

	@Test
	fun `cancellation is rethrown instead of becoming a failure snapshot`() = runBlocking {
		signIn()
		networks.failure = CancellationException("cancelled")

		assertFailsWith<CancellationException> { interactor().refresh() }
		assertTrue(snapshots.writtenNetwork == null)
	}

	private class FakeSettingsStore(var current: Settings) : SettingsStore {
		override suspend fun load(): Settings = current

		override suspend fun save(settings: Settings) {
			current = settings
		}
	}

	private class FakeSnapshotStore : SnapshotStore {
		var storedNetwork: NetworkTraffic? = null
		var writtenNetwork: NetworkTraffic? = null

		override suspend fun readTodayCourses(id: WidgetId): TodayCourses? = null

		override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) = Unit

		override suspend fun readNetworkTraffic(id: WidgetId): NetworkTraffic? = storedNetwork

		override suspend fun writeNetworkTraffic(id: WidgetId, snapshot: NetworkTraffic) {
			storedNetwork = snapshot
			writtenNetwork = snapshot
		}

		override suspend fun readSemesterCountdown(id: WidgetId): SemesterCountdown? = null

		override suspend fun writeSemesterCountdown(id: WidgetId, snapshot: SemesterCountdown) = Unit
	}

	private class FakeNetworkGateway(private val account: CampusNetwork) : NetworkGateway {
		var calls = 0
		var failure: Exception? = null

		override suspend fun usage(credentials: Credentials): CampusNetwork {
			calls++
			failure?.let { throw it }
			return account
		}
	}
}
