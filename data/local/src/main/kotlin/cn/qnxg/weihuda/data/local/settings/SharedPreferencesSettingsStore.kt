// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.local.settings

import android.content.Context
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.data.local.security.KeystoreTokenCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 设置与令牌的本机存放。
 *
 * 令牌用 Keystore 加密后再写入 SharedPreferences；后端地址是明文，它本来就是用户自己选的。
 */
class SharedPreferencesSettingsStore(context: Context) : SettingsStore {

	private val prefs = context.applicationContext
		.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	override suspend fun load(): Settings = withContext(Dispatchers.IO) {
		Settings(
			baseUrl = prefs.getString(KEY_BASE_URL, null).orEmpty(),
			credentials = Credentials(KeystoreTokenCipher.decrypt(prefs.getString(KEY_TOKEN, "").orEmpty())),
		)
	}

	override suspend fun save(settings: Settings): Unit = withContext(Dispatchers.IO) {
		prefs.edit()
			.putString(KEY_BASE_URL, settings.baseUrl.trim())
			.putString(KEY_TOKEN, KeystoreTokenCipher.encrypt(settings.credentials.token.trim()))
			.apply()
	}

	private companion object {
		const val PREFS = "weihuda_settings"
		const val KEY_BASE_URL = "base_url"
		const val KEY_TOKEN = "token"
	}
}
