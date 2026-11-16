package cn.qnxg.weihuda.data.remote.dto

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
	return Semester(year = year, term = term, startDate = start, vacation = vacation, next = next)
}

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
