// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 2×1 单值卡片的字号档位。
 *
 * 启动器给 2 格的宽度在 110dp（系统公式 `70×2−30`）到 170dp 之间，差得很多：
 * `186.50度` 在 18sp 下要 ~78dp，110dp 的卡片放不下，所以窄卡片整档降一级字号。
 */
enum class MetricCardDensity { COMPACT, WIDE }

object MetricCardLayout {
	/** 低于这个宽度时大号数字会盖掉图标之外的空间，整档退回小字号。 */
	private const val WIDE_WIDTH_DP = 140

	fun density(widthDp: Int): MetricCardDensity =
		if (widthDp < WIDE_WIDTH_DP) MetricCardDensity.COMPACT else MetricCardDensity.WIDE
}
