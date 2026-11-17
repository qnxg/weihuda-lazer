// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 流量卡片上一个值的语气，决定视图用哪一种颜色。
 *
 * 「未知」既不是正常也不是错误，所以不能用布尔值表达——把未知染成红色只会制造焦虑。
 */
enum class NetworkTone { OK, BAD, NEUTRAL }

/**
 * 校园网流量小组件的视图模型。
 *
 * 文案、百分比与颜色语气都在 presenter 里定好，视图只负责摆放；空态与失败态也在这里表达，
 * 视图没有一处 `if (状态)` 之外的分支。
 */
data class NetworkWidgetView(
	/** 本月已用，如 `12.80GB`；没有数据时是 `--`。 */
	val usedLabel: String,
	/** 2×2 用的短标签（`12.8GB`）：110dp 宽里 7 个字符会被裁掉。 */
	val usedLabelCompact: String,
	val percentLabel: String?,
	/** 进度条填充比例，0..1；超过 100% 时按满格画。 */
	val progress: Float?,
	val quotaLabel: String?,
	val accountLabel: String?,
	val accountTone: NetworkTone,
	val overdueLabel: String?,
	val overdueTone: NetworkTone,
	val emptyTitle: String?,
	val emptyDetail: String?,
	/** 有旧数据时的失败提示；空态下由 [emptyDetail] 承载原因。 */
	val failureLabel: String?,
) {
	val hasData: Boolean get() = emptyTitle == null
}
