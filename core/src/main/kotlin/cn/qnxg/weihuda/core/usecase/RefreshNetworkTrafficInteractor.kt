// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.NetworkGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import kotlin.coroutines.cancellation.CancellationException

/**
 * 「本月用了多少流量、账号是否正常」的编排：读设置 → 未登录就输出登录态，否则请求后端 → 写快照。
 *
 * 与课表不同，流量按月结算，所以失败时保留旧数据的条件不是「同一天」而是「同一个月」：
 * 上个月的数字留在卡片上会被当成这个月的用量。
 */
class RefreshNetworkTrafficInteractor(
	private val settings: SettingsStore,
	private val snapshots: SnapshotStore,
	private val networks: NetworkGateway,
	private val clock: Clock = CampusClock.clock(),
	private val widgetId: WidgetId = WidgetId.CampusNetwork,
) : RefreshNetworkTraffic {

	override suspend fun refresh(): NetworkTraffic {
		val now = clock.instant()
		val date = LocalDate.now(clock)
		val settings = settings.load()
		val previous = snapshots.readNetworkTraffic(widgetId)

		val result = if (!settings.usesRemoteData) {
			NetworkTraffic.signedOut(now)
		} else {
			try {
				NetworkTraffic.loaded(networks.usage(settings.credentials), now)
			} catch (cancelled: CancellationException) {
				throw cancelled
			} catch (error: Exception) {
				NetworkTraffic.failed(
					message = error.message?.takeIf { it.isNotBlank() } ?: "流量刷新失败",
					previous = previous?.account?.takeIf {
						YearMonth.from(previous.refreshedAt.atZone(clock.zone)) == YearMonth.from(date)
					},
					refreshedAt = now,
				)
			}
		}

		snapshots.writeNetworkTraffic(widgetId, result)
		return result
	}
}
