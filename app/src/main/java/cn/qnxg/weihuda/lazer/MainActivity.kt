// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.qnxg.weihuda.lazer.ui.LazerApp
import cn.qnxg.weihuda.lazer.ui.theme.LazerTheme
import cn.qnxg.weihuda.widget.WidgetLibrary

/**
 * 唯一的 Activity：应用内所有页面都在同一个 Compose 树里。
 *
 * 它只搭主题和根组合函数，业务动作全部由 [LazerApp] 里的 AppState 转给组件门面。
 */
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		setContent {
			LazerTheme {
				LazerApp(
					library = WidgetLibrary.of(this),
					debugToken = debugToken(),
					fallbackToken = debugFallbackToken(),
					onFallbackTokenApplied = ::markFallbackTokenUsed,
				)
			}
		}
	}

	/** 显式注入：`adb shell am start … -e weihuda_jwt <token>`，每次启动都覆盖，适合临时切账号。 */
	private fun debugToken(): String? =
		if (BuildConfig.DEBUG) intent?.getStringExtra(EXTRA_DEBUG_JWT)?.trim() else null

	/**
	 * 构建期配置的默认令牌（见 app/build.gradle.kts）。
	 *
	 * 只种一次：种过之后用户退出登录就保持未登录，否则每次启动都会被兜底令牌顶回去，
	 * 「退出登录」这个动作就失效了。
	 */
	private fun debugFallbackToken(): String? {
		if (!BuildConfig.DEBUG) return null
		val token = BuildConfig.DEBUG_JWT.trim()
		if (token.isEmpty() || debugPrefs.getBoolean(KEY_FALLBACK_USED, false)) return null
		return token
	}

	private fun markFallbackTokenUsed() {
		debugPrefs.edit().putBoolean(KEY_FALLBACK_USED, true).apply()
	}

	private val debugPrefs by lazy {
		getSharedPreferences("weihuda_debug", Context.MODE_PRIVATE)
	}

	private companion object {
		const val EXTRA_DEBUG_JWT = "weihuda_jwt"
		const val KEY_FALLBACK_USED = "fallback_token_used"
	}
}
