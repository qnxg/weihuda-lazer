// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.widget.presentation.NetworkTone
import cn.qnxg.weihuda.widget.presentation.WidgetPalette

/** 桌面令牌的 Compose 版本；颜色只有 [WidgetPalette] 一份，所有卡片的预览共用。 */
@Immutable
internal class PreviewColors(private val night: Boolean) {
	val surface = Color(WidgetPalette.surface.resolve(night))
	val line = Color(WidgetPalette.line.resolve(night))
	val brand = Color(WidgetPalette.brand.resolve(night))
	val ink = Color(WidgetPalette.ink.resolve(night))
	val inkMuted = Color(WidgetPalette.inkMuted.resolve(night))
	val error = Color(WidgetPalette.error.resolve(night))
	val success = Color(WidgetPalette.success.resolve(night))

	fun status(status: CourseStatus): Color =
		Color(WidgetPalette.status(status).resolve(night))

	fun tone(tone: NetworkTone): Color = when (tone) {
		NetworkTone.OK -> success
		NetworkTone.BAD -> error
		NetworkTone.NEUTRAL -> inkMuted
	}
}
