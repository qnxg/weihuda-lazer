// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.entity.CourseStatus

/**
 * 小组件的一个颜色令牌，深浅两套值。
 *
 * 放在 presentation 而不是 Glance 视图里：应用内预览要还原桌面，但没有 RemoteViews，
 * 只能共用同一份 ARGB。调用方按当前配置解析：桌面交给 Glance 的 day/night ColorProvider，
 * 预览用 [resolve]。
 */
data class WidgetColor(val light: Int, val dark: Int) {
	fun resolve(night: Boolean): Int = if (night) dark else light
}

/**
 * 小组件的颜色令牌。深色不是把浅色反相：卡片用深灰蓝而不是纯黑，状态色整体提亮，
 * 因为桌面壁纸比应用背景更花，纯黑卡片会显得像一块洞。
 */
object WidgetPalette {
	val surface = WidgetColor(0xFFFFFFFF.toInt(), 0xFF1A222C.toInt())
	val line = WidgetColor(0xFFEDF1F5.toInt(), 0xFF2B3542.toInt())
	val brand = WidgetColor(0xFF318CCB.toInt(), 0xFF5FAEE3.toInt())
	val ink = WidgetColor(0xFF1B2733.toInt(), 0xFFE6EDF3.toInt())
	val inkMuted = WidgetColor(0xFF6A7883.toInt(), 0xFF93A1AE.toInt())
	val error = WidgetColor(0xFFD64545.toInt(), 0xFFFF7B72.toInt())

	private val ongoing = WidgetColor(0xFF2E7D32.toInt(), 0xFF6ECB73.toInt())
	private val soon = WidgetColor(0xFFE08600.toInt(), 0xFFF5A623.toInt())
	private val upcoming = WidgetColor(0xFF4A5560.toInt(), 0xFF8A97A3.toInt())
	private val finished = WidgetColor(0xFF9AA5AF.toInt(), 0xFF65717D.toInt())

	fun status(status: CourseStatus): WidgetColor = when (status) {
		CourseStatus.ONGOING -> ongoing
		CourseStatus.SOON -> soon
		CourseStatus.UPCOMING -> upcoming
		CourseStatus.FINISHED -> finished
	}
}
