// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

/**
 * 小组件种类的标识。
 *
 * 每一种小组件各自持有一份快照（详见 `SnapshotStore`），所以「加一个新卡片」
 * 等于加一个 id，而不是往同一份快照里塞字段。
 */
@JvmInline
value class WidgetId(val value: String) {
	companion object {
		val TodayCourses = WidgetId("today-courses")
		val CampusNetwork = WidgetId("campus-network")
		val SemesterCountdown = WidgetId("semester-countdown")
		val CampusCard = WidgetId("campus-card")
		val DormElectricity = WidgetId("dorm-electricity")
	}
}
