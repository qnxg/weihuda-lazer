// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

/**
 * 校园网流量账户在本月的状态。
 *
 * 只保留卡片要用的数字：上下行明细、历史账单属于应用内的详情页，放进小组件快照只会让
 * 桌面读得更多。金额用元、流量用 GB，换单位与文案是表现层的事。
 */
data class CampusNetwork(
	/** 本月套餐内已用（GB）。 */
	val usedGb: Double,
	/** 套餐额度（免费流量，GB）。 */
	val quotaGb: Double,
	/** 已用比例，0..1；后端直接给出，不复算，避免与学校页面上的百分比不一致。 */
	val usedRatio: Double,
	val status: AccountStatus,
	/** 欠费金额（元），0 表示无欠费。 */
	val overdueYuan: Double,
) {
	enum class AccountStatus {
		NORMAL,
		LOCKED,
		UNKNOWN,
	}
}
