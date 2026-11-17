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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetLayout
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetView
import cn.qnxg.weihuda.widget.presentation.WidgetPalette

private val surface = WidgetPalette.surface.provider()
private val brand = WidgetPalette.brand.provider()
private val brandSoft = WidgetPalette.brandSoft.provider()
private val ink = WidgetPalette.ink.provider()
private val inkMuted = WidgetPalette.inkMuted.provider()
private val error = WidgetPalette.error.provider()

/**
 * 倒计时卡片的视图，属于 Humble Object：只把 [CountdownWidgetView] 摆到屏幕上。
 *
 * 桌面里它是一张 4×1 的宽卡：左侧是「说明 + 学期名 + 日期」，右侧是品牌蓝的天数面板。
 * 启动器给的高度可能只有 60dp（按 `minHeight` 算），也可能接近一个单元格（实测 MIUI 约 77dp），
 * 所以只有一档密度切换，见 [CountdownWidgetLayout]。
 *
 * 面板做成内缩的一块而不是贴到卡片右边：RemoteViews 不会把子元素裁剪到父级的圆角里，
 * 满高的色块会从卡片右上角、右下角探出来盖住圆角。内缩 8dp + 14dp 圆角在 API 26–30
 * （`cornerRadius` 不生效）上也仍然是一块干净的方块。
 */
@Composable
internal fun CountdownWidgetContent(context: Context, view: CountdownWidgetView) {
	val compact = CountdownWidgetLayout.compact(LocalSize.current.height.value.toInt())

	Row(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(surface)
			.cornerRadius(18.dp)
			.clickable(openAppAction(context)),
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (view.hasData) {
			Summary(view = view, compact = compact)
			Metric(view = view, compact = compact)
		} else {
			EmptyBody(view)
		}
	}
}

@Composable
private fun RowScope.Summary(view: CountdownWidgetView, compact: Boolean) {
	Row(
		modifier = GlanceModifier
			.defaultWeight()
			.fillMaxHeight()
			.padding(start = if (compact) 14.dp else 16.dp, end = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (!compact) {
			Box(
				modifier = GlanceModifier
					.size(40.dp)
					.background(brandSoft)
					.cornerRadius(12.dp),
				contentAlignment = Alignment.Center,
			) {
				Image(
					provider = ImageProvider(R.drawable.ic_book),
					contentDescription = null,
					modifier = GlanceModifier.size(22.dp),
					colorFilter = ColorFilter.tint(brand),
				)
			}
			Spacer(GlanceModifier.width(12.dp))
		}
		Column(modifier = GlanceModifier.defaultWeight()) {
			Text(
				text = view.eyebrow,
				style = TextStyle(color = inkMuted, fontSize = if (compact) 10.sp else 11.sp),
				maxLines = 1,
			)
			Spacer(GlanceModifier.height(if (compact) 2.dp else 3.dp))
			Text(
				text = view.title,
				style = TextStyle(
					color = ink,
					fontSize = if (compact) 15.sp else 16.sp,
					fontWeight = FontWeight.Bold,
				),
				maxLines = 1,
			)
			view.detail?.let {
				Spacer(GlanceModifier.height(if (compact) 4.dp else 6.dp))
				Row(verticalAlignment = Alignment.CenterVertically) {
					Image(
						provider = ImageProvider(R.drawable.ic_calendar_range),
						contentDescription = null,
						modifier = GlanceModifier.size(12.dp),
						colorFilter = ColorFilter.tint(inkMuted),
					)
					Spacer(GlanceModifier.width(4.dp))
					Text(
						text = it.clip(22),
						style = TextStyle(color = inkMuted, fontSize = if (compact) 10.sp else 11.sp),
						maxLines = 1,
					)
				}
			}
			// 60dp 里放不下第四行：失败提示让位，数字才是这张卡片的正事
			if (!compact) {
				view.failureLabel?.let {
					Spacer(GlanceModifier.height(2.dp))
					Text(
						text = it.clip(24),
						style = TextStyle(color = error, fontSize = 9.sp),
						maxLines = 1,
					)
				}
			}
		}
	}
}

@Composable
private fun RowScope.Metric(view: CountdownWidgetView, compact: Boolean) {
	val inset = if (compact) 4.dp else 8.dp

	Box(
		modifier = GlanceModifier
			.width(if (compact) 84.dp else 96.dp)
			.fillMaxHeight()
			.padding(start = 4.dp, top = inset, end = inset, bottom = inset),
	) {
		Column(
			modifier = GlanceModifier
				.fillMaxSize()
				.background(brandSoft)
				.cornerRadius(14.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			view.metricLabel?.let {
				Text(
					text = it,
					style = TextStyle(color = inkMuted, fontSize = if (compact) 10.sp else 11.sp),
					maxLines = 1,
				)
				Spacer(GlanceModifier.height(if (compact) 1.dp else 2.dp))
			}
			Row(verticalAlignment = Alignment.Bottom) {
				Text(
					text = view.metricValue,
					style = TextStyle(
						color = brand,
						fontSize = if (compact) 22.sp else 30.sp,
						fontWeight = FontWeight.Bold,
					),
					maxLines = 1,
				)
				view.metricUnit?.let {
					Spacer(GlanceModifier.width(2.dp))
					Text(
						text = it,
						style = TextStyle(color = brand, fontSize = if (compact) 11.sp else 12.sp),
						maxLines = 1,
					)
				}
			}
		}
	}
}

@Composable
private fun RowScope.EmptyBody(view: CountdownWidgetView) {
	Box(
		modifier = GlanceModifier
			.defaultWeight()
			.fillMaxHeight()
			.padding(horizontal = 16.dp),
		contentAlignment = Alignment.Center,
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = view.emptyTitle.orEmpty(),
				style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			view.emptyDetail?.let {
				Spacer(GlanceModifier.height(4.dp))
				Text(
					text = it.clip(30),
					style = TextStyle(color = inkMuted, fontSize = 10.sp),
					maxLines = 2,
				)
			}
		}
	}
}
