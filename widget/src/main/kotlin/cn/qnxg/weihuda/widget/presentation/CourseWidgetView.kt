// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.entity.CourseStatus

/**
 * 课程小组件的视图模型。
 *
 * 全是已经排好序、拼好文案的字符串：Glance 视图（以及应用内的预览）只负责把它们摆到屏幕上。
 * 视图与视图模型都放在这一层，所以「渲染规则」只有一处，预览不会和桌面说的不一样。
 */
data class CourseWidgetView(
	val weekLabel: String,
	val dayLabel: String,
	val dayLabelShort: String,
	val rows: List<SessionRow>,
	val focus: FocusCard?,
	val focusTotalLabel: String?,
	val emptyTitle: String?,
	val emptyDetail: String?,
	val failureLabel: String?,
	val sourceLabel: String,
) {
	val hasRows: Boolean get() = rows.isNotEmpty()

	/**
	 * 页脚那行字：刷新失败时用失败文案顶掉数据来源。
	 *
	 * 4×2 没有多余的行可以放错误，但页脚本来就在，所以失败在最小尺寸上也能被看见——
	 * 否则用户点了「刷新」只会看到时间戳没变。
	 */
	val footerLabel: String get() = failureLabel?.replace("上次刷新失败：", "刷新失败 · ") ?: sourceLabel

	val footerIsError: Boolean get() = failureLabel != null
}

/** 一节课在 4×2 / 4×4 里的一行。 */
data class SessionRow(
	val name: String,
	val place: String?,
	val periodLabel: String,
	val startTime: String,
	val endTime: String,
	val status: CourseStatus,
) {
	val statusLabel: String get() = status.label
}

/** 2×2 只放得下一节课：优先正在上的，其次下一节。 */
data class FocusCard(
	val timeRange: String,
	val name: String,
	val detail: String,
	val status: CourseStatus,
)
