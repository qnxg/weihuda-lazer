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
 * 令牌用 Keystore 加密后再写入 SharedPreferences；其余字段是明文，因为它们本来就是
 * 用户自己选的开关和地址。默认演示模式打开，装好就能看到内容。
 */
class SharedPreferencesSettingsStore(context: Context) : SettingsStore {

	private val prefs = context.applicationContext
		.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	override suspend fun load(): Settings = withContext(Dispatchers.IO) {
		Settings(
			baseUrl = prefs.getString(KEY_BASE_URL, null).orEmpty(),
			credentials = Credentials(KeystoreTokenCipher.decrypt(prefs.getString(KEY_TOKEN, "").orEmpty())),
			demoMode = prefs.getBoolean(KEY_DEMO, true),
		)
	}

	override suspend fun save(settings: Settings): Unit = withContext(Dispatchers.IO) {
		prefs.edit()
			.putString(KEY_BASE_URL, settings.baseUrl.trim())
			.putString(KEY_TOKEN, KeystoreTokenCipher.encrypt(settings.credentials.token.trim()))
			.putBoolean(KEY_DEMO, settings.demoMode)
			.apply()
	}

	private companion object {
		const val PREFS = "weihuda_settings"
		const val KEY_BASE_URL = "base_url"
		const val KEY_TOKEN = "token"
		const val KEY_DEMO = "demo_mode"
	}
}
