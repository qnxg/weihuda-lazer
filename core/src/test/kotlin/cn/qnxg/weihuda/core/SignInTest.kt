// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core

import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.port.AuthGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.usecase.SignInInteractor
import cn.qnxg.weihuda.core.usecase.SignInResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** 登录用例：校验、落盘与失败语义都在这里，与 HTTP 无关。 */
class SignInTest {

	private val settings = FakeSettingsStore(Settings(baseUrl = "https://example.invalid"))
	private val auth = FakeAuthGateway(Credentials("jwt-real"))

	private fun interactor() = SignInInteractor(auth = auth, settings = settings)

	@Test
	fun `successful sign in stores the token and keeps the base url`() = runBlocking {
		settings.current = Settings(baseUrl = "https://example.invalid")

		val result = interactor().signIn("202506050175", "secret")

		assertIs<SignInResult.Success>(result)
		assertEquals("jwt-real", settings.load().credentials.token)
		assertEquals("https://example.invalid", settings.load().baseUrl)
	}

	@Test
	fun `student id is trimmed before it reaches the gateway`() = runBlocking {
		interactor().signIn("  202506050175 ", "secret")

		assertEquals("202506050175" to "secret", auth.calls.single())
	}

	@Test
	fun `blank input is rejected before touching the gateway`() = runBlocking {
		assertIs<SignInResult.Failure>(interactor().signIn("  ", "secret"))
		assertIs<SignInResult.Failure>(interactor().signIn("202506050175", ""))
		assertTrue(auth.calls.isEmpty())
	}

	@Test
	fun `gateway failure surfaces its message and keeps the old credentials`() = runBlocking {
		auth.error = IllegalStateException("密码错误")

		val result = interactor().signIn("202506050175", "wrong")

		assertIs<SignInResult.Failure>(result)
		assertEquals("密码错误", result.message)
		assertTrue(settings.load().credentials.token.isEmpty())
	}
}

private class FakeAuthGateway(private val credentials: Credentials) : AuthGateway {
	val calls = mutableListOf<Pair<String, String>>()
	var error: Exception? = null

	override suspend fun signIn(stuId: String, password: String): Credentials {
		calls += stuId to password
		error?.let { throw it }
		return credentials
	}
}

private class FakeSettingsStore(initial: Settings) : SettingsStore {
	var current = initial

	override suspend fun load(): Settings = current

	override suspend fun save(settings: Settings) {
		current = settings
	}
}
