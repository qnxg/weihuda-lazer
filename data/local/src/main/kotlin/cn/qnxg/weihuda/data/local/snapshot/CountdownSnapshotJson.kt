// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local.snapshot

import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

/**
 * 倒计时快照的落盘格式。
 *
 * 存的是学期日期而不是「还有几天」：天数由渲染时的当前日期现算，存下来第二天就是错的。
 */
@Serializable
internal data class CountdownSnapshotDto(
	val version: Int = VERSION,
	val failure: String? = null,
	val refreshedAt: Long,
	val year: Int? = null,
	val term: Int? = null,
	val startDate: String? = null,
	val vacation: String? = null,
	val next: String? = null,
) {
	companion object {
		const val VERSION: Int = 1
	}
}

internal object CountdownSnapshotJson {
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
	}

	fun encode(snapshot: SemesterCountdown): String = json.encodeToString(
		CountdownSnapshotDto.serializer(),
		CountdownSnapshotDto(
			failure = snapshot.failure,
			refreshedAt = snapshot.refreshedAt.toEpochMilli(),
			year = snapshot.semester?.year,
			term = snapshot.semester?.term,
			startDate = snapshot.semester?.startDate?.toString(),
			vacation = snapshot.semester?.vacation?.toString(),
			next = snapshot.semester?.next?.toString(),
		),
	)

	fun decode(text: String?): SemesterCountdown? {
		if (text.isNullOrBlank()) return null
		val dto = runCatching {
			json.decodeFromString(CountdownSnapshotDto.serializer(), text)
		}.getOrNull() ?: return null
		if (dto.version != CountdownSnapshotDto.VERSION) return null

		return SemesterCountdown(
			semester = dto.toEntity(),
			failure = dto.failure,
			refreshedAt = Instant.ofEpochMilli(dto.refreshedAt),
		)
	}

	/** 学年、学期或开学日任一缺失就算没有学期数据：没有它们连「第几周」都谈不上。 */
	private fun CountdownSnapshotDto.toEntity(): Semester? {
		val year = year ?: return null
		val term = term ?: return null
		val start = startDate.toDateOrNull() ?: return null
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
}
