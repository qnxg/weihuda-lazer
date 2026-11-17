// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.DormElectricity
import java.time.Instant

/**
 * 「宿舍剩余电量」用例的输出数据，同时是小组件快照的内容。
 *
 * 与 [CardBalance] 一样，失败时保留上一次成功的读数：电量的变化是连续的，几小时前的读数比空白
 * 更有用，而卡片上的「刷新失败」已经说明它不是刚取的。
 */
data class ElectricityBalance(
	val state: State,
	/** 没有数据（未登录、宿舍信息缺失或首次刷新失败）时为 null。 */
	val electricity: DormElectricity?,
	/** 最近一次刷新失败的原因；非空时 [electricity] 可能是上一次成功的结果。 */
	val failure: String?,
	val refreshedAt: Instant,
) {
	enum class State {
		/** 没有登录：没有任何电量可展示。 */
		SIGNED_OUT,

		/** 有数据；[failure] 非空说明这是上一次成功的结果。 */
		LOADED,

		/** 没有数据，且刷新失败过。 */
		FAILED,
	}

	companion object {
		fun signedOut(refreshedAt: Instant): ElectricityBalance = ElectricityBalance(
			state = State.SIGNED_OUT,
			electricity = null,
			failure = null,
			refreshedAt = refreshedAt,
		)

		fun loaded(electricity: DormElectricity, refreshedAt: Instant): ElectricityBalance = ElectricityBalance(
			state = State.LOADED,
			electricity = electricity,
			failure = null,
			refreshedAt = refreshedAt,
		)

		fun failed(message: String, previous: DormElectricity?, refreshedAt: Instant): ElectricityBalance =
			ElectricityBalance(
				state = if (previous != null) State.LOADED else State.FAILED,
				electricity = previous,
				failure = message,
				refreshedAt = refreshedAt,
			)
	}
}
