// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local.snapshot

import cn.qnxg.weihuda.core.entity.DormElectricity
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

/** 宿舍电量快照的落盘格式；数字是度，单位留给表现层。 */
@Serializable
internal data class ElectricitySnapshotDto(
	val version: Int = VERSION,
	val state: String,
	val failure: String? = null,
	val refreshedAt: Long,
	val remainingKwh: Double? = null,
) {
	companion object {
		const val VERSION: Int = 1
	}
}

internal object ElectricitySnapshotJson {
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
	}

	fun encode(snapshot: ElectricityBalance): String = json.encodeToString(
		ElectricitySnapshotDto.serializer(),
		ElectricitySnapshotDto(
			state = snapshot.state.name,
			failure = snapshot.failure,
			refreshedAt = snapshot.refreshedAt.toEpochMilli(),
			remainingKwh = snapshot.electricity?.remainingKwh,
		),
	)

	fun decode(text: String?): ElectricityBalance? {
		if (text.isNullOrBlank()) return null
		val dto = runCatching {
			json.decodeFromString(ElectricitySnapshotDto.serializer(), text)
		}.getOrNull() ?: return null
		if (dto.version != ElectricitySnapshotDto.VERSION) return null

		return ElectricityBalance(
			state = runCatching { ElectricityBalance.State.valueOf(dto.state) }
				.getOrDefault(ElectricityBalance.State.FAILED),
			electricity = dto.remainingKwh?.let { DormElectricity(remainingKwh = it) },
			failure = dto.failure,
			refreshedAt = Instant.ofEpochMilli(dto.refreshedAt),
		)
	}
}
