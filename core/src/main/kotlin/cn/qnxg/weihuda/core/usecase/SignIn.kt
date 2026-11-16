// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.port.AuthGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import kotlin.coroutines.cancellation.CancellationException

/**
 * 登录：换一张令牌，并存进本机设置。
 */
interface SignIn {
	suspend fun signIn(stuId: String, password: String): SignInResult
}

sealed interface SignInResult {
	data class Success(val credentials: Credentials) : SignInResult

	/** [message] 直接交给界面显示，用例不改写后端文案。 */
	data class Failure(val message: String) : SignInResult
}

class SignInInteractor(
	private val auth: AuthGateway,
	private val settings: SettingsStore,
) : SignIn {

	override suspend fun signIn(stuId: String, password: String): SignInResult {
		val id = stuId.trim()
		if (id.isEmpty() || password.isEmpty()) {
			return SignInResult.Failure("请填写学号和密码")
		}

		val credentials = try {
			auth.signIn(id, password)
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (error: Exception) {
			return SignInResult.Failure(error.message?.takeIf { it.isNotBlank() } ?: "登录失败")
		}

		settings.save(settings.load().copy(credentials = credentials))
		return SignInResult.Success(credentials)
	}
}
