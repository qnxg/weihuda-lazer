// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local.snapshot

import cn.qnxg.weihuda.core.entity.CampusCard
import cn.qnxg.weihuda.core.usecase.CardBalance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * 校园卡余额快照的落盘格式。
 *
 * 与流量快照同一形状：两张卡片的数据都只有一个数字，但余额与流量的有效期、来源都不同，
 * 共用一份 DTO 只会让改其中一个时担心另一个。
 */
@Serializable
internal data class CardSnapshotDto(
	val version: Int = VERSION,
	val state: String,
	val failure: String? = null,
	val refreshedAt: Long,
	val balanceYuan: Double? = null,
) {
	companion object {
		const val VERSION: Int = 1
	}
}

internal object CardSnapshotJson {
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
	}

	fun encode(snapshot: CardBalance): String = json.encodeToString(
		CardSnapshotDto.serializer(),
		CardSnapshotDto(
			state = snapshot.state.name,
			failure = snapshot.failure,
			refreshedAt = snapshot.refreshedAt.toEpochMilli(),
			balanceYuan = snapshot.card?.balanceYuan,
		),
	)

	fun decode(text: String?): CardBalance? {
		if (text.isNullOrBlank()) return null
		val dto = runCatching {
			json.decodeFromString(CardSnapshotDto.serializer(), text)
		}.getOrNull() ?: return null
		if (dto.version != CardSnapshotDto.VERSION) return null

		return CardBalance(
			state = runCatching { CardBalance.State.valueOf(dto.state) }
				.getOrDefault(CardBalance.State.FAILED),
			card = dto.balanceYuan?.let { CampusCard(balanceYuan = it) },
			failure = dto.failure,
			refreshedAt = Instant.ofEpochMilli(dto.refreshedAt),
		)
	}
}
