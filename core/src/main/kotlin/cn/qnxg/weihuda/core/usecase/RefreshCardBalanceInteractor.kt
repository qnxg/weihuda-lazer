// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.CampusCardGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import java.time.Clock
import kotlin.coroutines.cancellation.CancellationException

/**
 * 「卡里还剩多少钱」的编排：读设置 → 未登录就输出登录态，否则请求后端 → 写快照。
 *
 * 刷新的输入只有令牌，所以这个用例没有别的分支：余额既不分学期也不分月份。
 */
class RefreshCardBalanceInteractor(
	private val settings: SettingsStore,
	private val snapshots: SnapshotStore,
	private val cards: CampusCardGateway,
	private val clock: Clock = CampusClock.clock(),
	private val widgetId: WidgetId = WidgetId.CampusCard,
) : RefreshCardBalance {

	override suspend fun refresh(): CardBalance {
		val now = clock.instant()
		val settings = settings.load()
		val previous = snapshots.readCardBalance(widgetId)

		val result = if (!settings.usesRemoteData) {
			CardBalance.signedOut(now)
		} else {
			try {
				CardBalance.loaded(cards.card(settings.credentials), now)
			} catch (cancelled: CancellationException) {
				throw cancelled
			} catch (error: Exception) {
				CardBalance.failed(
					message = error.message?.takeIf { it.isNotBlank() } ?: "余额刷新失败",
					previous = previous?.card,
					refreshedAt = now,
				)
			}
		}

		snapshots.writeCardBalance(widgetId, result)
		return result
	}
}
