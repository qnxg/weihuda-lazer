// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import cn.qnxg.weihuda.widget.presentation.MetricCardDensity
import cn.qnxg.weihuda.widget.presentation.MetricCardLayout
import cn.qnxg.weihuda.widget.presentation.MetricCardView
import cn.qnxg.weihuda.widget.presentation.WidgetPalette
import cn.qnxg.weihuda.widget.presentation.drawableRes

private val surface = WidgetPalette.surface.provider()
private val brand = WidgetPalette.brand.provider()
private val ink = WidgetPalette.ink.provider()
private val inkMuted = WidgetPalette.inkMuted.provider()
private val error = WidgetPalette.error.provider()

/** 图标用的是单色 vector，颜色交给 day/night 的 tint，与文字令牌同一套解析规则。 */
private val iconTint: ColorFilter = ColorFilter.tint(brand)

/**
 * 单值卡片的视图，属于 Humble Object：只把 [MetricCardView] 摆到屏幕上。
 *
 * 2×1 只有一行的高度，所以图标在左、两行文字在右；校园卡余额与宿舍电量共用它——
 * 两张卡片的信息量相同，各写一份视图只会让两处间距慢慢跑偏。
 */
@Composable
internal fun MetricCardContent(context: Context, view: MetricCardView) {
	val size = LocalSize.current
	val density = MetricCardLayout.density(size.width.value.toInt())
	val compact = density == MetricCardDensity.COMPACT

	Row(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(surface)
			.cornerRadius(18.dp)
			.clickable(openAppAction(context))
			.padding(
				start = if (compact) 10.dp else 14.dp,
				end = if (compact) 10.dp else 14.dp,
				top = 6.dp,
				bottom = 6.dp,
			),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Image(
			provider = ImageProvider(view.icon.drawableRes()),
			contentDescription = null,
			modifier = GlanceModifier.size(if (compact) 18.dp else 22.dp),
			colorFilter = iconTint,
		)
		Column(
			modifier = GlanceModifier.defaultWeight().padding(start = if (compact) 8.dp else 10.dp),
		) {
			if (view.hasData) {
				// 有失败提示时占掉标签这一行：2×1 放不下第三行文字
				Text(
					text = view.failureLabel ?: view.label,
					style = TextStyle(
						color = if (view.failureLabel != null) error else inkMuted,
						fontSize = if (compact) 9.sp else 10.sp,
					),
					maxLines = 1,
				)
				Text(
					// 窄卡片里「1234.56度」这类更长的数字只能截断
					text = view.value.clip(9),
					style = TextStyle(
						color = ink,
						fontSize = if (compact) 14.sp else 18.sp,
						fontWeight = FontWeight.Bold,
					),
					maxLines = 1,
				)
			} else {
				Text(
					text = view.emptyTitle.orEmpty(),
					style = TextStyle(
						color = ink,
						fontSize = if (compact) 10.sp else 12.sp,
						fontWeight = FontWeight.Medium,
					),
					maxLines = 1,
				)
				view.emptyDetail?.let {
					Text(
						text = it.clip(if (compact) 14 else 20),
						style = TextStyle(color = inkMuted, fontSize = 9.sp),
						maxLines = 1,
					)
				}
			}
		}
	}
}
