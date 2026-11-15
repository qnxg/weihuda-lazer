package cn.qnxg.weihuda.core.domain

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

/** 教学周是否可信（用于决定小组件是否显示「第 N 周」）。 */
fun isPlausibleWeek(week: Int): Boolean = week in 1..30

private val WEEKDAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/** 1 = 周一 … 7 = 周日。 */
fun weekdayLabel(weekday: Int): String = WEEKDAY_LABELS[(weekday - 1).coerceIn(0, 6)]

/** `9/21` 这样的月日文案，与小程序一致不带年份。 */
fun monthDayLabel(date: LocalDate): String = "${date.monthValue}/${date.dayOfMonth}"
