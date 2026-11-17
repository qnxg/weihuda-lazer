// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

/**
 * 校园卡的当前余额。
 *
 * 只留余额：卡号与消费流水属于应用内的账单页，放进小组件快照只会让桌面读得更多，
 * 也会让快照格式跟着账单接口一起变。
 */
data class CampusCard(
	/** 余额（元）。后端给的是元为单位的浮点数，元与分之间的换算留在表现层。 */
	val balanceYuan: Double,
)
