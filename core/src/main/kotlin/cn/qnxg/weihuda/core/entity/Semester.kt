// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * 学期信息，来自 `GET /info/smester`。
 *
 * [startDate] 是当前学期第一周的周日；周次计算见 [weekOf]。
 * [vacation] 是学期结束日（后端字段 `vacation`，假期从这一天之后开始），
 * [next] 是下学期开学日。后端只保证 `startDate` 一定存在，另外两个可能缺。
 */
data class Semester(
	val year: Int,
	val term: Int,
	val startDate: LocalDate,
	val vacation: LocalDate? = null,
	val next: LocalDate? = null,
) {
	/** 「2026 秋季学期」：term 1 是秋季、2 是春季，与小程序上的写法一致。 */
	val name: String get() = "$year ${if (term == 1) "秋季学期" else "春季学期"}"
}

/** 学期相对某一天的位置。 */
enum class SemesterPhase {
	/** 还没开学。 */
	BEFORE_START,

	/** 学期进行中，含学期最后一天。 */
	IN_TERM,

	/** 学期已结束，正在放假。 */
	AFTER_TERM,
}

fun Semester.phaseOn(date: LocalDate): SemesterPhase = when {
	date.isBefore(startDate) -> SemesterPhase.BEFORE_START
	vacation != null && date.isAfter(vacation) -> SemesterPhase.AFTER_TERM
	// 假期日期缺失时按「学期进行中」处理：宁可显示「结束日期未知」，也不要凭空宣布放假
	else -> SemesterPhase.IN_TERM
}

/**
 * 教学周数：开学日到学期结束日的整周数。
 *
 * 后端没有这个字段，只能由日期差现算；日期缺失或不是正数时返回 null，
 * 宁可不说，也不给一个看起来像真的周数。
 */
fun Semester.teachingWeeks(): Int? {
	val end = vacation ?: return null
	val days = ChronoUnit.DAYS.between(startDate, end)
	return if (days > 0) (days / 7.0).roundToInt() else null
}

/**
 * 倒计时的目标日期与所处阶段。
 *
 * 学期结束后指向下学期开学日：假期里这张卡片的用处就是「还有几天开学」。
 */
data class SemesterCountdownTarget(
	val phase: SemesterPhase,
	val date: LocalDate,
	val daysLeft: Long,
)

/**
 * 以 [today] 为基准算出倒计时指向哪一天；目标日期缺失时返回 null。
 *
 * 天数在渲染时现算，不写进快照：写进去的那一刻就开始过期。
 */
fun Semester.countdownOn(today: LocalDate): SemesterCountdownTarget? {
	val phase = phaseOn(today)
	val date = when (phase) {
		SemesterPhase.BEFORE_START -> startDate
		SemesterPhase.IN_TERM -> vacation
		SemesterPhase.AFTER_TERM -> next
	} ?: return null
	return SemesterCountdownTarget(
		phase = phase,
		date = date,
		daysLeft = ChronoUnit.DAYS.between(today, date).coerceAtLeast(0),
	)
}
