// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 后端统一响应信封：`{ "code": 200, "data": ..., "msg": "请求成功" }`。
 *
 * 这一层只在适配器里存在，实体不认识 `code` 和 `msg`。
 */
@Serializable
internal data class ApiEnvelope<T>(
	val code: Int = 0,
	val data: T? = null,
	val msg: String? = null,
)

/** `GET /info/smester` */
@Serializable
internal data class SemesterDto(
	val startDate: String,
	val term: Int,
	val year: Int,
	val vacation: String? = null,
	val next: String? = null,
)

/** `GET /hdjw/class-table` 的单条记录。 */
@Serializable
internal data class CourseDto(
	val courseName: String,
	val courseId: String? = null,
	@SerialName("type") val type: String = "",
	val className: String? = null,
	val place: String? = null,
	val area: String? = null,
	val teacher: String? = null,
	val weeks: List<Int> = emptyList(),
	val day: Int = 1,
	val time: Int = 1,
	val credit: Float? = null,
	val extra: String? = null,
	val customizeId: Int = -1,
	val people: Int = 0,
)

/** `GET /info/user` */
@Serializable
internal data class StudentDto(
	val name: String? = null,
	@SerialName("stuId") val stuId: String? = null,
	val college: String? = null,
	val major: String? = null,
	@SerialName("class") val className: String? = null,
)

/** `GET /netflow`：本月流量概览、账号锁定状态与欠费。 */
@Serializable
internal data class NetflowDto(
	val thisMonth: NetflowMonthDto = NetflowMonthDto(),
	/** `已锁定` / `未锁定` / `未知状态`。 */
	val unlock: String? = null,
	val payInfo: Double? = null,
)

/** `GET /netflow` 里 `thisMonth` 的字段；流量单位是 GB，比例是 0..1 的小数。 */
@Serializable
internal data class NetflowMonthDto(
	val allBasePackageAmount: Double = 0.0,
	val basePackageUsed: Double = 0.0,
	val basePackageUsedPer: Double = 0.0,
)

/**
 * `GET /pt/card-info`：`{ "account": 368246, "balance": 84.0 }`。
 *
 * `account`（卡号）不在卡片上显示，所以不映射；要展示账单时再加，别让快照跟着一起长。
 */
@Serializable
internal data class CardInfoDto(
	val balance: Double = 0.0,
)
