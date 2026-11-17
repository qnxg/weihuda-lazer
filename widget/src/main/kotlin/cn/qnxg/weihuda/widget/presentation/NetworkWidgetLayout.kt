// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 流量卡片的尺寸规则。
 *
 * 与课程卡片不同，这里没有「行数」：信息量固定为三层（用量 / 进度 / 账单摘要），
 * 所以只需要决定哪些层留下。宽度不足以并排放下大号数字时按方形小卡处理。
 *
 * 数字来自真机测量（440dpi、默认字号）：标题行约 20dp，大号数字约 40dp，进度条 6dp，
 * 三列页脚约 46dp。
 */
enum class NetworkDensity {
	/** 2×2：只保留用量、百分比与进度条。 */
	COMPACT,

	/** 4×2：横向放得下标题和数字，但仍没有页脚的位置。 */
	WIDE,

	/** 4×3 / 4×4：参考设计的完整版，含免费流量、账号状态与欠费三列。 */
	FULL,
}

object NetworkWidgetLayout {
	/** 宽度小于这个值就按 2×2 处理：大号数字与百分比并排放不下。 */
	private const val COMPACT_WIDTH_DP = 180

	/** 完整版至少需要的高度：标题 + 数字 + 进度条 + 页脚。 */
	private const val FULL_HEIGHT_DP = 140

	fun density(widthDp: Int, heightDp: Int): NetworkDensity = when {
		widthDp < COMPACT_WIDTH_DP -> NetworkDensity.COMPACT
		heightDp >= FULL_HEIGHT_DP -> NetworkDensity.FULL
		else -> NetworkDensity.WIDE
	}
}
