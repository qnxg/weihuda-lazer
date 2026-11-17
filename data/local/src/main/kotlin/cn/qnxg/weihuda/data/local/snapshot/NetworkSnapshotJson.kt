// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local.snapshot

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * 流量快照的落盘格式。
 *
 * 与课表快照分开：两者的 data 形状、有效期（一天 / 一个月）和演进节奏都不同，
 * 共用一份 DTO 只会让改其中一个时担心另一个。
 */
@Serializable
internal data class NetworkSnapshotDto(
	val version: Int = VERSION,
	val state: String,
	val failure: String? = null,
	val refreshedAt: Long,
	val usedGb: Double? = null,
	val quotaGb: Double? = null,
	val usedRatio: Double? = null,
	val accountStatus: String? = null,
	val overdueYuan: Double? = null,
) {
	companion object {
		const val VERSION: Int = 1
	}
}

internal object NetworkSnapshotJson {
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
	}

	fun encode(snapshot: NetworkTraffic): String = json.encodeToString(
		NetworkSnapshotDto.serializer(),
		NetworkSnapshotDto(
			state = snapshot.state.name,
			failure = snapshot.failure,
			refreshedAt = snapshot.refreshedAt.toEpochMilli(),
			usedGb = snapshot.account?.usedGb,
			quotaGb = snapshot.account?.quotaGb,
			usedRatio = snapshot.account?.usedRatio,
			accountStatus = snapshot.account?.status?.name,
			overdueYuan = snapshot.account?.overdueYuan,
		),
	)

	fun decode(text: String?): NetworkTraffic? {
		if (text.isNullOrBlank()) return null
		val dto = runCatching {
			json.decodeFromString(NetworkSnapshotDto.serializer(), text)
		}.getOrNull() ?: return null
		if (dto.version != NetworkSnapshotDto.VERSION) return null

		return NetworkTraffic(
			state = runCatching { NetworkTraffic.State.valueOf(dto.state) }
				.getOrDefault(NetworkTraffic.State.FAILED),
			account = dto.toEntity(),
			failure = dto.failure,
			refreshedAt = Instant.ofEpochMilli(dto.refreshedAt),
		)
	}

	private fun NetworkSnapshotDto.toEntity(): CampusNetwork? {
		val used = usedGb ?: return null
		val quota = quotaGb ?: return null
		return CampusNetwork(
			usedGb = used,
			quotaGb = quota,
			usedRatio = usedRatio ?: 0.0,
			status = runCatching { CampusNetwork.AccountStatus.valueOf(accountStatus.orEmpty()) }
				.getOrDefault(CampusNetwork.AccountStatus.UNKNOWN),
			overdueYuan = overdueYuan ?: 0.0,
		)
	}
}
