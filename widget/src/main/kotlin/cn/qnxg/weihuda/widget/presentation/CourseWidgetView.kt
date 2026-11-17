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
) {
	val hasRows: Boolean get() = rows.isNotEmpty()
}

/** 一节课在 4×2 / 4×3 / 4×4 里的一行。 */
data class SessionRow(
	val name: String,
	/** 已拼好的次要信息：`第 1–2 节 | 信科楼 201 | 陈老师`。 */
	val detail: String,
	val startTime: String,
	val endTime: String,
	val status: CourseStatus,
) {
	val statusLabel: String get() = status.label
}

/** 2×2 只放得下一节课：优先正在上的，其次下一节。 */
data class FocusCard(
	val startTime: String,
	val endTime: String,
	val name: String,
	val detail: String,
	val status: CourseStatus,
)
