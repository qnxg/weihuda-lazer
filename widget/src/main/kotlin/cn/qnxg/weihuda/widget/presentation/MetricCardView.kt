// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

/**
 * 单值卡片左上角的图标。
 *
 * presentation 不认 drawable：枚举在这里，资源 id 由 [drawableRes] 或视图自己换，
 * 于是应用内预览能和桌面用同一份图标，presenter 也可以在 JVM 上测。
 */
enum class MetricIcon { CAMPUS_CARD, DORM_ELECTRICITY }

/**
 * 2×1 单值卡片的视图模型：左边图标，右边一行标签 + 一行数字。
 *
 * 校园卡余额与宿舍电量的信息量相同（就是一个数字），所以共用这一种形状；
 * 差别只在 presenter 怎么把这个数字连同单位格式化成字符串。
 */
data class MetricCardView(
	val icon: MetricIcon,
	/** 如「校园卡金额」「宿舍剩余电量」。 */
	val label: String,
	/** 大号数字，已经带好单位；没有数据时是 `--`。 */
	val value: String,
	/** 有旧数据时的失败提示：视图用它顶替 [label] 那一行（空态下由 [emptyDetail] 承载原因）。 */
	val failureLabel: String?,
	val emptyTitle: String?,
	val emptyDetail: String?,
) {
	val hasData: Boolean get() = emptyTitle == null
}
