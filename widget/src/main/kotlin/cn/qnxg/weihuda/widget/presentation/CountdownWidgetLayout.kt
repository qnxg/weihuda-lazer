// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 倒计时卡片的尺寸规则。
 *
 * 桌面目标是 4×1（宽约 4 个单元格、高 1 个），但启动器给出的高度差得很远：
 * 实测 MIUI 的一个单元格约 77dp，而按 `minHeight` 算出来的可能只有 60dp。
 * 所以只有一档判断——放不下「图标底片 + 行高」时把图标让出去、天数换成小一号字，
 * 宁可少一个装饰，也不要裁掉「还有 47 天」。
 */
object CountdownWidgetLayout {
	/** 完整样式要 72dp：文字列 60dp，加上一行「刷新失败」与上下留白。 */
	private const val ROOMY_HEIGHT_DP = 72

	fun compact(heightDp: Int): Boolean = heightDp < ROOMY_HEIGHT_DP
}
