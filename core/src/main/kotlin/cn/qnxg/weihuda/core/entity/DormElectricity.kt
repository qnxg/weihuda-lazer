// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

/**
 * 宿舍的剩余电量。
 *
 * 后端把额度直接格式化成了「186.50度」这样的字符串，解析与单位剥离发生在适配器里：
 * 实体只保留数值，度 / kWh 的写法与小数位是表现层的事。
 */
data class DormElectricity(
	/** 剩余电量（度，即 kWh）。 */
	val remainingKwh: Double,
)
