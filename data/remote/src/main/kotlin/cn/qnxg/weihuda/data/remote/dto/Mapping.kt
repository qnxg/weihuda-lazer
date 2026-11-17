// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.remote.dto

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.Student
import cn.qnxg.weihuda.core.port.GatewayException
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * DTO → 实体的映射。
 *
 * 放在适配器里，所以后端字段改名只影响这些函数；实体保持与后端无关的词汇。
 * 映射是纯函数，测试不需要 HTTP。
 */
internal fun SemesterDto.toEntity(): Semester {
	val start = try {
		LocalDate.parse(startDate)
	} catch (_: DateTimeParseException) {
		throw GatewayException(code = -1, message = "学期开始日期无法解析：$startDate")
	}
	// 假期与开学日期只有倒计时会用到：解析不了就当没有，不为装饰性的字段让整次刷新失败
	return Semester(
		year = year,
		term = term,
		startDate = start,
		vacation = vacation.toDateOrNull(),
		next = next.toDateOrNull(),
	)
}

private fun String?.toDateOrNull(): LocalDate? =
	this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

internal fun CourseDto.toEntity(): Course = Course(
	name = courseName,
	code = courseId,
	kind = type,
	className = className,
	place = place,
	campus = area,
	teacher = teacher,
	weeks = weeks,
	dayOfWeek = day,
	period = time,
	credits = credit,
	note = extra,
	customizeId = customizeId,
	students = people,
)

internal fun StudentDto.toEntity(): Student = Student(
	name = name,
	studentId = stuId,
	college = college,
	major = major,
	className = className,
)

internal fun NetflowDto.toEntity(): CampusNetwork = CampusNetwork(
	usedGb = thisMonth.basePackageUsed.coerceAtLeast(0.0),
	quotaGb = thisMonth.allBasePackageAmount.coerceAtLeast(0.0),
	usedRatio = normalizeRatio(thisMonth.basePackageUsedPer, thisMonth.basePackageUsed, thisMonth.allBasePackageAmount),
	status = when (unlock?.trim()) {
		"已锁定" -> CampusNetwork.AccountStatus.LOCKED
		"未锁定" -> CampusNetwork.AccountStatus.NORMAL
		else -> CampusNetwork.AccountStatus.UNKNOWN
	},
	overdueYuan = (payInfo ?: 0.0).coerceAtLeast(0.0),
)

/**
 * 后端给的是 0..1 的小数（40GB 用 13.1GB → 0.3275），但历史上出现过百分数。
 * 大于 1.5 时按百分数解释；两者都缺失时才用已用量除额度兜底。
 */
private fun normalizeRatio(raw: Double, used: Double, quota: Double): Double = when {
	raw > RATIO_PERCENT_THRESHOLD -> raw / 100.0
	raw > 0.0 -> raw
	quota > 0.0 -> used / quota
	else -> 0.0
}

/** 1.2 是合法的「超用 20%」，32.75 才是另一个单位。 */
private const val RATIO_PERCENT_THRESHOLD = 1.5
