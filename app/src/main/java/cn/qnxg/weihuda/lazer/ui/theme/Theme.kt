// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 一套完整的应用令牌；小组件侧对应 `widget.presentation.WidgetPalette`。 */
data class LazerPalette(
	val brand: Color,
	val brandDark: Color,
	val ink: Color,
	val inkMuted: Color,
	val pageBackground: Color,
	val surface: Color,
	val line: Color,
	val danger: Color,
	val success: Color,
	val warning: Color,
	val finished: Color,
)

// 应用的 Brand 还用作顶栏底色，深色保持 #318CCB 以保住白字；小组件的 Brand 只做文字，
// 深色用更亮的 #5FAEE3。角色不同，值不需要强行一致。
private val LightPalette = LazerPalette(
	brand = Color(0xFF318CCB),
	brandDark = Color(0xFF1F5F8B),
	ink = Color(0xFF1B2733),
	inkMuted = Color(0xFF6A7883),
	pageBackground = Color(0xFFF4F6F9),
	surface = Color(0xFFFFFFFF),
	line = Color(0xFFEDF1F5),
	danger = Color(0xFFD64545),
	success = Color(0xFF2E7D32),
	warning = Color(0xFFE08600),
	finished = Color(0xFF9AA5AF),
)

private val DarkPalette = LazerPalette(
	brand = Color(0xFF318CCB),
	brandDark = Color(0xFF1F5F8B),
	ink = Color(0xFFE6EDF3),
	inkMuted = Color(0xFF93A1AE),
	pageBackground = Color(0xFF10151B),
	surface = Color(0xFF1A222C),
	line = Color(0xFF2B3542),
	danger = Color(0xFFFF7B72),
	success = Color(0xFF6ECB73),
	warning = Color(0xFFF5A623),
	finished = Color(0xFF65717D),
)

private val LocalLazerPalette = staticCompositionLocalOf { LightPalette }

/**
 * 设计令牌，与 AGENTS.md 的「视觉」一节一一对应。
 *
 * 用 CompositionLocal 而不是常量：应用要跟随系统深浅色，调用方不需要知道当前是哪一套。
 * 小组件侧的那份在 `widget.presentation.WidgetPalette`，预览直接读它；新增令牌时两处都要加。
 */
object LazerColors {
	val Brand: Color @Composable get() = LocalLazerPalette.current.brand
	val BrandDark: Color @Composable get() = LocalLazerPalette.current.brandDark
	val Ink: Color @Composable get() = LocalLazerPalette.current.ink
	val InkMuted: Color @Composable get() = LocalLazerPalette.current.inkMuted
	val PageBackground: Color @Composable get() = LocalLazerPalette.current.pageBackground
	val Surface: Color @Composable get() = LocalLazerPalette.current.surface
	val Line: Color @Composable get() = LocalLazerPalette.current.line
	val Danger: Color @Composable get() = LocalLazerPalette.current.danger
	val Success: Color @Composable get() = LocalLazerPalette.current.success
	val Warning: Color @Composable get() = LocalLazerPalette.current.warning
	val Finished: Color @Composable get() = LocalLazerPalette.current.finished
}

private val LightScheme = lightColorScheme(
	primary = LightPalette.brand,
	onPrimary = Color.White,
	secondary = LightPalette.brandDark,
	background = LightPalette.pageBackground,
	onBackground = LightPalette.ink,
	surface = LightPalette.surface,
	onSurface = LightPalette.ink,
	outline = LightPalette.line,
	error = LightPalette.danger,
)

private val DarkScheme = darkColorScheme(
	primary = DarkPalette.brand,
	onPrimary = Color.White,
	secondary = DarkPalette.brandDark,
	background = DarkPalette.pageBackground,
	onBackground = DarkPalette.ink,
	surface = DarkPalette.surface,
	onSurface = DarkPalette.ink,
	outline = DarkPalette.line,
	error = DarkPalette.danger,
)

@Composable
fun LazerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
	val palette = if (darkTheme) DarkPalette else LightPalette
	CompositionLocalProvider(LocalLazerPalette provides palette) {
		MaterialTheme(colorScheme = if (darkTheme) DarkScheme else LightScheme, content = content)
	}
}
