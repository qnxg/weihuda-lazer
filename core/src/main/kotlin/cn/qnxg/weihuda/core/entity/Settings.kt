// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.entity

/**
 * 微生活后端的一次登录会话。
 *
 * 后端读的是裸 JWT（`Authorization: <token>`，没有 `Bearer` 前缀），
 * 所以这里就是令牌本身，不再包一层 [WebToken] 之类的结构。
 */
@JvmInline
value class Credentials(val token: String) {
	val isPresent: Boolean get() = token.isNotBlank()
}

/**
 * 用户在本机配置的后端地址与登录会话。
 *
 * 它属于应用配置而不是实体规则，但跨层的每一方（用例、适配器、界面）都要读同一份，
 * 所以在 core 里定义结构，落盘格式留给 `data:local`。
 */
data class Settings(
	val baseUrl: String = "",
	val credentials: Credentials = Credentials(""),
) {
	/** 只有拿到令牌时才会真的发请求；没有令牌就没什么可展示的。 */
	val usesRemoteData: Boolean get() = credentials.isPresent
}
