// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.CampusCard
import java.time.Instant

/**
 * 「校园卡余额」用例的输出数据，同时是小组件快照的内容。
 *
 * 失败时保留上一次成功的余额，不设时间窗口：余额是存量而不是流量，旧值仍然是一个真实存在过的
 * 余额，卡片上与它并排显示「刷新失败」即可（对比 [NetworkTraffic]：跨月的用量会被当成这个月的账单，
 * 必须丢掉）。
 */
data class CardBalance(
	val state: State,
	/** 没有数据（未登录或首次刷新失败）时为 null。 */
	val card: CampusCard?,
	/** 最近一次刷新失败的原因；非空时 [card] 可能是上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	enum class State {
		/** 没有登录：没有任何余额可展示。 */
		SIGNED_OUT,

		/** 有数据；[failure] 非空说明这是上一次成功的结果。 */
		LOADED,

		/** 没有数据，且刷新失败过。 */
		FAILED,
	}

	companion object {
		fun signedOut(refreshedAt: Instant): CardBalance = CardBalance(
			state = State.SIGNED_OUT,
			card = null,
			failure = null,
			refreshedAt = refreshedAt,
		)

		fun loaded(card: CampusCard, refreshedAt: Instant): CardBalance = CardBalance(
			state = State.LOADED,
			card = card,
			failure = null,
			refreshedAt = refreshedAt,
		)

		fun failed(message: String, previous: CampusCard?, refreshedAt: Instant): CardBalance = CardBalance(
			state = if (previous != null) State.LOADED else State.FAILED,
			card = previous,
			failure = message,
			refreshedAt = refreshedAt,
		)
	}
}
