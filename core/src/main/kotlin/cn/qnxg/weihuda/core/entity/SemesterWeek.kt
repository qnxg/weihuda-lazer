// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.core.entity

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 计算日期落在第几教学周（从 1 开始）。
 *
 * 与小程序保持一致：以学期开始日期 `startDate` 为基准做 7 天整除，
 * 湖大的一周从周日开始，因此 `startDate` 本身就是第一周的周日。
 */
fun weekOf(startDate: LocalDate, date: LocalDate): Int {
	val days = ChronoUnit.DAYS.between(startDate, date)
	return Math.floorDiv(days, 7L).toInt() + 1
}

/** 教学周是否可信（用于决定界面是否显示「第 N 周」还是「假期」）。 */
fun isPlausibleWeek(week: Int): Boolean = week in 1..30
