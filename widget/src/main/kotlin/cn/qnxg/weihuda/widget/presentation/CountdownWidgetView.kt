// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 学期倒计时小组件的视图模型。
 *
 * 文案与天数都在 presenter 里定好，视图只负责摆放；空态与失败态也在这里表达，
 * 视图没有一处 `if (状态)` 之外的分支。
 */
data class CountdownWidgetView(
	/** 天数上面那行说明：「距离学期结束」/「距离开学」。 */
	val eyebrow: String,
	/** 学期名，如 `2026 秋季学期`。 */
	val title: String,
	/** 日历图标后面那行，如 `1 月 3 日结束 · 共 16 周`。 */
	val detail: String?,
	/** 天数上方的标签，空态时为 null。 */
	val metricLabel: String?,
	/** 天数本身，如 `47`；没有数据时是 `--`。 */
	val metricValue: String,
	/** 天数后面的单位。 */
	val metricUnit: String?,
	/** 有旧数据时的失败提示；空态下由 [emptyDetail] 承载原因。 */
	val failureLabel: String?,
	val emptyTitle: String?,
	val emptyDetail: String?,
) {
	val hasData: Boolean get() = emptyTitle == null
}
