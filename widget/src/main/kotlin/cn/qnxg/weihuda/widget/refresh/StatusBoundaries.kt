// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.refresh

import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.entity.SOON_WINDOW_MINUTES
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * 下一次状态可能变化的时刻：开课前 20 分钟、开始、结束后的下一秒，以及午夜。
 *
 * 结束用 `endTime + 1s` 而不是 `endTime`：`statusAt` 在结束那一刻仍算「上课中」，
 * 提前一秒重绘会得到同一个状态，闹钟就白醒了。
 */
internal fun nextStatusBoundary(
	sessions: List<CourseSession>,
	date: LocalDate,
	now: ZonedDateTime,
): ZonedDateTime {
	val zone = now.zone
	val endOfDay = date.plusDays(1).atStartOfDay(zone)
	val boundaries = buildList {
		sessions.forEach { session ->
			val start = date.atTime(session.startTime).atZone(zone)
			add(start.minusMinutes(SOON_WINDOW_MINUTES))
			add(start)
			add(date.atTime(session.endTime).atZone(zone).plusSeconds(1))
		}
		add(endOfDay)
	}
	return boundaries.filter { it.isAfter(now) }.minOrNull() ?: endOfDay
}
