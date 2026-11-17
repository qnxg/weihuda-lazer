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
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
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
 * 校园卡余额与宿舍电量共用它——两张卡片的信息量相同，都是「图标 + 标签 + 一个数字」，
 * 各自写一份视图只会让两处间距慢慢跑偏。
 */
@Composable
internal fun MetricCardContent(context: Context, view: MetricCardView) {
	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(surface)
			.cornerRadius(18.dp)
			.clickable(openAppAction(context))
			.padding(14.dp),
	) {
		Image(
			provider = ImageProvider(view.icon.drawableRes()),
			contentDescription = null,
			modifier = GlanceModifier.size(22.dp),
			colorFilter = iconTint,
		)

		// 失败提示挨着图标放：数字那一块留给余额本身，2×2 没有第二行的位置
		view.failureLabel?.let {
			Text(
				text = it.clip(20),
				modifier = GlanceModifier.padding(top = 4.dp),
				style = TextStyle(color = error, fontSize = 9.sp),
				maxLines = 1,
			)
		}

		// 数字贴底：卡片被拉宽时多出来的空间留在图标与标签之间，和参考图一致
		Spacer(GlanceModifier.defaultWeight())

		if (view.hasData) {
			Text(
				text = view.label,
				style = TextStyle(color = inkMuted, fontSize = 12.sp),
				maxLines = 1,
			)
			Spacer(GlanceModifier.height(3.dp))
			Text(
				// 18sp 是「186.50度」在 110dp 里放得下的最大字号；再长就只能截断
				text = view.value.clip(9),
				style = TextStyle(color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold),
				maxLines = 1,
			)
		} else {
			Text(
				text = view.emptyTitle.orEmpty(),
				style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			view.emptyDetail?.let {
				Spacer(GlanceModifier.height(4.dp))
				Text(
					text = it.clip(28),
					style = TextStyle(color = inkMuted, fontSize = 10.sp),
					maxLines = 2,
				)
			}
		}
	}
}
