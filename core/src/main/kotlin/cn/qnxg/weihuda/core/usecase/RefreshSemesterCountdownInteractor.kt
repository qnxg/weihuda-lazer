// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.SnapshotStore
import java.time.Clock
import kotlin.coroutines.cancellation.CancellationException

/**
 * 「离学期结束还有几天」的编排：请求学期信息 → 写快照。
 *
 * 与另外两张卡片不同，这里不读 [cn.qnxg.weihuda.core.port.SettingsStore]：`/info/smester`
 * 在 openapi 里是 `security: []`，不需要令牌，所以未登录也能显示倒计时。令牌只在
 * `WeihudaBackend` 里决定要不要带鉴权头，轮不到用例操心。
 */
class RefreshSemesterCountdownInteractor(
	private val snapshots: SnapshotStore,
	private val semesters: SemesterGateway,
	private val clock: Clock = CampusClock.clock(),
	private val widgetId: WidgetId = WidgetId.SemesterCountdown,
) : RefreshSemesterCountdown {

	override suspend fun refresh(): SemesterCountdown {
		val previous = snapshots.readSemesterCountdown(widgetId)

		val result = try {
			SemesterCountdown.loaded(semesters.current(), clock.instant())
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (error: Exception) {
			SemesterCountdown.failed(
				message = error.message?.takeIf { it.isNotBlank() } ?: "学期信息刷新失败",
				previous = previous?.semester,
				refreshedAt = clock.instant(),
			)
		}

		snapshots.writeSemesterCountdown(widgetId, result)
		return result
	}
}
