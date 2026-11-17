// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.DormElectricityGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import java.time.Clock
import kotlin.coroutines.cancellation.CancellationException

/**
 * 「宿舍还剩多少电」的编排。
 *
 * 与余额不同的是这里多一类失败：宿舍信息缺失或宿舍暂不支持时后端会报错，而客户端无从自查，
 * 所以同样走 [ElectricityBalance.failed]，把后端的原话留给卡片显示。
 */
class RefreshElectricityBalanceInteractor(
	private val settings: SettingsStore,
	private val snapshots: SnapshotStore,
	private val electricity: DormElectricityGateway,
	private val clock: Clock = CampusClock.clock(),
	private val widgetId: WidgetId = WidgetId.DormElectricity,
) : RefreshElectricityBalance {

	override suspend fun refresh(): ElectricityBalance {
		val now = clock.instant()
		val settings = settings.load()
		val previous = snapshots.readElectricityBalance(widgetId)

		val result = if (!settings.usesRemoteData) {
			ElectricityBalance.signedOut(now)
		} else {
			try {
				ElectricityBalance.loaded(electricity.remaining(settings.credentials), now)
			} catch (cancelled: CancellationException) {
				throw cancelled
			} catch (error: Exception) {
				ElectricityBalance.failed(
					message = error.message?.takeIf { it.isNotBlank() } ?: "电量刷新失败",
					previous = previous?.electricity,
					refreshedAt = now,
				)
			}
		}

		snapshots.writeElectricityBalance(widgetId, result)
		return result
	}
}
