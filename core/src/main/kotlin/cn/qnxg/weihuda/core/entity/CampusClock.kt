// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.core.entity

import java.time.Clock
import java.time.ZoneId

/**
 * 校区时区与时钟。
 *
 * 课表按长沙时间解释；设备时区被改成别的时区时，周次与「上课中」仍然要对。
 * 用例只依赖 [Clock]，所以周次、状态和刷新时间都能在 JVM 上用固定时钟测。
 */
object CampusClock {
	val zone: ZoneId = ZoneId.of("Asia/Shanghai")

	fun clock(): Clock = Clock.system(zone)
}
