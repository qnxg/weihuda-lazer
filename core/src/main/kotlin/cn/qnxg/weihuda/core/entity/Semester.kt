package cn.qnxg.weihuda.core.entity

import java.time.LocalDate

/**
 * 学期信息，来自 `GET /info/smester`。
 *
 * [startDate] 是当前学期第一周的周日；周次计算见 [weekOf]。
 */
data class Semester(
	val year: Int,
	val term: Int,
	val startDate: LocalDate,
	val vacation: String? = null,
	val next: String? = null,
)
