package cn.qnxg.weihuda.core.domain

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 小节次与作息时间。
 *
 * 数据来源：<https://github.com/qnxg/hnu_query/blob/main/docs/hdjw/time.md>
 */
object CoursePeriods {
    /** 第 1…12 小节的开始时间。 */
    private val starts: List<LocalTime> = listOf(
        LocalTime.of(8, 0),
        LocalTime.of(8, 55),
        LocalTime.of(10, 0),
        LocalTime.of(10, 55),
        LocalTime.of(14, 30),
        LocalTime.of(15, 15),
        LocalTime.of(16, 10),
        LocalTime.of(16, 55),
        LocalTime.of(19, 0),
        LocalTime.of(19, 55),
        LocalTime.of(20, 50),
        LocalTime.of(21, 35),
    )

    /** 第 1…12 小节的结束时间。 */
    private val ends: List<LocalTime> = listOf(
        LocalTime.of(8, 45),
        LocalTime.of(9, 40),
        LocalTime.of(10, 45),
        LocalTime.of(11, 40),
        LocalTime.of(15, 15),
        LocalTime.of(16, 0),
        LocalTime.of(16, 55),
        LocalTime.of(17, 40),
        LocalTime.of(19, 45),
        LocalTime.of(20, 40),
        LocalTime.of(21, 35),
        LocalTime.of(22, 20),
    )

    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun start(period: Int): LocalTime? = starts.getOrNull(period - 1)

    fun end(period: Int): LocalTime? = ends.getOrNull(period - 1)

    fun label(period: Int): String {
        val start = start(period) ?: return "第${period}节"
        val end = end(period) ?: return "第${period}节"
        return "${start.format(clock)}-${end.format(clock)}"
    }
}
