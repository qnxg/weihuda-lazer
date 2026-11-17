// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 小组件的尺寸规则：给定桌面单元格的实际 dp，决定信息密度和能放几行。
 *
 * Glance 视图与应用内预览都从这里取值。以前两边各写一份 `when (size)`，
 * 于是预览与桌面会在临界尺寸上给出不同的行数——这类偏差只有用户才发现得了。
 *
 * 数字来自真机测量（440dpi、默认字号）：头部约 24dp，分割线 1dp，页脚约 27dp，
 * 4×2 一行约 24dp，4×3 一行约 32dp，4×4 两行一节课约 48dp。
 */
enum class WidgetDensity {
	/** 2×2：只放大一节课。 */
	COMPACT,

	/** 4×2：单行课程，名称与状态。 */
	WIDE,

	/** 4×3：参考课表条目的两行布局，压到三行也放得下。 */
	MEDIUM,

	/** 4×4：参考课表条目的两行布局，时间竖排、节次地点教师同一行。 */
	LARGE,
}

object WidgetLayout {
	/** 宽度小于这个值就按 2×2 处理：方形小尺寸里一行字放不下。 */
	private const val COMPACT_WIDTH_DP = 180

	private const val LARGE_HEIGHT_DP = 230

	private const val MEDIUM_HEIGHT_DP = 140

	private const val CHROME_DP = 52

	private const val WIDE_ROW_DP = 24

	private const val MEDIUM_ROW_DP = 32

	private const val MEDIUM_ROW_MAX_DP = 48

	private const val LARGE_ROW_DP = 48

	/** 「还有 N 节」占的高度。 */
	private const val HIDDEN_LINE_DP = 14

	fun density(widthDp: Int, heightDp: Int): WidgetDensity = when {
		widthDp < COMPACT_WIDTH_DP -> WidgetDensity.COMPACT
		heightDp >= LARGE_HEIGHT_DP -> WidgetDensity.LARGE
		heightDp >= MEDIUM_HEIGHT_DP -> WidgetDensity.MEDIUM
		else -> WidgetDensity.WIDE
	}

	/**
	 * 当前高度能完整放下的课程行数。
	 *
	 * 返回 0 表示放不下整行——此时视图只显示聚焦卡片/空态，不让半行文字压在页脚上。
	 * 每行之间还有 1dp 分割线，行数按最坏情况少算一行，避免最后一行的下半截被裁掉。
	 */
	fun visibleRows(heightDp: Int, density: WidgetDensity): Int = when (density) {
		WidgetDensity.COMPACT -> 0
		WidgetDensity.WIDE -> (heightDp - CHROME_DP) / (WIDE_ROW_DP + 1)
		WidgetDensity.MEDIUM -> (heightDp - CHROME_DP) / (MEDIUM_ROW_DP + 1)
		WidgetDensity.LARGE -> (heightDp - CHROME_DP) / (LARGE_ROW_DP + 1)
	}.coerceAtLeast(0)

	/**
	 * 4×3 的行高：课程少时把行撑开，避免卡片下半片空白；课程多时回到 32dp 的紧凑行。
	 * 上限 48dp，和 4×4 的参考行高一致。
	 */
	/** 放满 [rows] 行后，是否还留得下「还有 N 节」那一行。 */
	fun hasRoomForHidden(heightDp: Int, density: WidgetDensity, rows: Int): Boolean {
		val rowDp = when (density) {
			WidgetDensity.COMPACT -> return false
			WidgetDensity.WIDE -> WIDE_ROW_DP
			WidgetDensity.MEDIUM -> MEDIUM_ROW_DP
			WidgetDensity.LARGE -> LARGE_ROW_DP
		}
		val used = CHROME_DP + rows * rowDp + (rows - 1)
		return rows > 0 && heightDp - used >= HIDDEN_LINE_DP
	}

	fun mediumRowHeight(heightDp: Int, rows: Int, hasHidden: Boolean): Int {
		if (rows <= 0) return MEDIUM_ROW_DP
		val available = heightDp - CHROME_DP - if (hasHidden) HIDDEN_LINE_DP else 0
		val fitted = (available - (rows - 1)) / rows
		return fitted.coerceIn(MEDIUM_ROW_DP, MEDIUM_ROW_MAX_DP)
	}
}
