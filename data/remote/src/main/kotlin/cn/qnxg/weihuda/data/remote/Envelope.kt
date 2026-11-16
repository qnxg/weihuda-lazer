// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.data.remote.dto.ApiEnvelope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/** 适配器内部的 JSON 配置：后端加字段不该让客户端崩。 */
internal val WeihudaJson: Json = Json {
	ignoreUnknownKeys = true
	coerceInputValues = true
	explicitNulls = false
}

/**
 * 拆开 `{ code, data, msg }` 信封，把后端的失败翻成 [GatewayException]。
 *
 * 拆包规则单独放在这里，是因为它是唯一「后端契约」的知识点，值得被直接测到。
 */
internal inline fun <reified T> unwrapEnvelope(text: String): T {
	val envelope = runCatching {
		WeihudaJson.decodeFromString(ApiEnvelope.serializer(JsonElement.serializer()), text)
	}.getOrElse {
		throw GatewayException(code = -1, message = "无法解析接口响应")
	}

	val data = envelope.data
	if (envelope.code != 200 || data == null) {
		throw GatewayException(
			code = envelope.code,
			message = envelope.msg?.takeIf { it.isNotBlank() } ?: "接口返回异常",
		)
	}

	return runCatching { WeihudaJson.decodeFromJsonElement(serializer<T>(), data) }
		.getOrElse { throw GatewayException(code = envelope.code, message = "无法解析接口响应") }
}
