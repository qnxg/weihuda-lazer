// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusNetwork
import java.time.Instant

/**
 * 「校园网流量」用例的输出数据，同时是小组件快照的内容。
 *
 * 没有日期字段：流量按月结算，快照的有效期是一个自然月，而不是一天。
 * 配额、百分比与账号状态都已经算好，格式化留给 presenter。
 */
data class NetworkTraffic(
	val state: State,
	/** 没有数据（未登录或首次刷新失败）时为 null。 */
	val account: CampusNetwork?,
	/** 最近一次刷新失败的原因；非空时 [account] 可能是上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	enum class State {
		/** 没有登录：没有任何流量数据可展示。 */
		SIGNED_OUT,

		/** 有数据；[failure] 非空说明这是上一次成功的结果。 */
		LOADED,

		/** 没有数据，且刷新失败过。 */
		FAILED,
	}

	companion object {
		fun signedOut(refreshedAt: Instant): NetworkTraffic = NetworkTraffic(
			state = State.SIGNED_OUT,
			account = null,
			failure = null,
			refreshedAt = refreshedAt,
		)

		fun loaded(account: CampusNetwork, refreshedAt: Instant): NetworkTraffic = NetworkTraffic(
			state = State.LOADED,
			account = account,
			failure = null,
			refreshedAt = refreshedAt,
		)

		/**
		 * 刷新失败：保留上一次成功的结果，只把原因写进去。
		 *
		 * 是否保留由调用方决定（跨月就要丢弃），这里只负责状态。
		 */
		fun failed(message: String, previous: CampusNetwork?, refreshedAt: Instant): NetworkTraffic =
			NetworkTraffic(
				state = if (previous != null) State.LOADED else State.FAILED,
				account = previous,
				failure = message,
				refreshedAt = refreshedAt,
			)
	}
}
