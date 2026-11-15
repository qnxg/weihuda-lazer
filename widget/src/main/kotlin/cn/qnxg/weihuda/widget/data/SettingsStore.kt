package cn.qnxg.weihuda.widget.data

import android.content.Context
import cn.qnxg.weihuda.core.api.DEFAULT_BASE_URL

/** 小组件的数据源配置。 */
data class AppSettings(
	val baseUrl: String = DEFAULT_BASE_URL,
	val token: String = "",
	/** 未配置令牌时默认使用内置演示课表，保证装好就能看到内容。 */
	val demoMode: Boolean = true,
) {
	val hasToken: Boolean get() = token.isNotBlank()
}

class SettingsStore(context: Context) {
	private val prefs = context.applicationContext
		.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	fun load(): AppSettings = AppSettings(
		baseUrl = prefs.getString(KEY_BASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL,
		token = TokenCipher.decrypt(prefs.getString(KEY_TOKEN, "").orEmpty()),
		demoMode = prefs.getBoolean(KEY_DEMO, true),
	)

	fun save(settings: AppSettings) {
		prefs.edit()
			.putString(KEY_BASE_URL, settings.baseUrl.trim())
			.putString(KEY_TOKEN, TokenCipher.encrypt(settings.token.trim()))
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
