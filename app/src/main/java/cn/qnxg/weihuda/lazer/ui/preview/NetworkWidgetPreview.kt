// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.widget.presentation.NetworkDensity
import cn.qnxg.weihuda.widget.presentation.NetworkTone
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetLayout
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetPresenter
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetView

/**
 * 流量卡片的应用内预览。
 *
 * 文案来自桌面上同一个 [NetworkWidgetPresenter]，颜色来自同一份 `WidgetPalette`，
 * 所以「预览说 32%、桌面说 33%」这类偏差不会出现。
 */
@Composable
fun NetworkWidgetPreview(snapshot: NetworkTraffic?, mode: PreviewMode) {
	val view = NetworkWidgetPresenter.present(snapshot)
	val colors = PreviewColors(isSystemInDarkTheme())
	val width = if (mode == PreviewMode.COMPACT) 110.dp else 250.dp
	val height = when (mode) {
		PreviewMode.COMPACT -> 110.dp
		PreviewMode.WIDE -> 110.dp
		PreviewMode.LARGE -> 250.dp
	}
	val density = NetworkWidgetLayout.density(width.value.toInt(), height.value.toInt())

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = colors.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier.width(width).height(height),
	) {
		if (!view.hasData) {
			EmptyBody(view = view, density = density, colors = colors)
		} else {
			UsageBody(view = view, density = density, colors = colors)
		}
	}
}

@Composable
private fun UsageBody(view: NetworkWidgetView, density: NetworkDensity, colors: PreviewColors) {
	val gutter = if (density == NetworkDensity.COMPACT) 12.dp else 14.dp
	Column(modifier = Modifier.fillMaxSize()) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(start = gutter, end = gutter, top = 10.dp, bottom = 2.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = "校园网",
				color = colors.ink,
				style = if (density == NetworkDensity.COMPACT) NetworkGlyphs.titleCompact else NetworkGlyphs.title,
				maxLines = 1,
			)
			if (density != NetworkDensity.COMPACT) {
				Spacer(Modifier.width(6.dp))
				Text("本月已用", color = colors.inkMuted, style = NetworkGlyphs.caption, maxLines = 1)
			}
			Spacer(Modifier.weight(1f))
			when {
				density == NetworkDensity.COMPACT -> view.percentLabel?.let {
					Text(it, color = colors.inkMuted, style = NetworkGlyphs.percentCompact, maxLines = 1)
				}

				density == NetworkDensity.FULL -> Text("›", color = colors.inkMuted, style = NetworkGlyphs.chevron, maxLines = 1)
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth().padding(start = gutter, end = gutter, top = 2.dp, bottom = 6.dp),
			verticalAlignment = Alignment.Bottom,
		) {
			Text(
				text = if (density == NetworkDensity.COMPACT) view.usedLabelCompact else view.usedLabel,
				color = colors.ink,
				style = when (density) {
					NetworkDensity.COMPACT -> NetworkGlyphs.usedCompact
					NetworkDensity.WIDE -> NetworkGlyphs.usedWide
					NetworkDensity.FULL -> NetworkGlyphs.usedFull
				},
				maxLines = 1,
			)
			Spacer(Modifier.weight(1f))
			if (density != NetworkDensity.COMPACT) {
				view.percentLabel?.let {
					Text(
						text = it,
						color = colors.inkMuted,
						style = if (density == NetworkDensity.FULL) NetworkGlyphs.percentFull else NetworkGlyphs.percent,
						maxLines = 1,
					)
				}
			}
		}

		Progress(fraction = view.progress, gutter = gutter, colors = colors)

		view.failureLabel?.let {
			Text(
				text = it,
				color = colors.error,
				style = NetworkGlyphs.failure,
				modifier = Modifier.padding(start = gutter, end = gutter, top = 4.dp),
				maxLines = 1,
			)
		}

		Spacer(Modifier.weight(1f))

		if (density == NetworkDensity.FULL) {
			Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
			Footer(view = view, colors = colors)
		}
	}
}

@Composable
private fun Progress(fraction: Float?, gutter: Dp, colors: PreviewColors) {
	val value = fraction ?: return
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = gutter, end = gutter)
			.height(6.dp)
			.background(colors.line, RoundedCornerShape(3.dp)),
	) {
		if (value > 0f) {
			Box(
				modifier = Modifier
					.fillMaxWidth(value.coerceAtMost(1f))
					.fillMaxHeight()
					.background(colors.success, RoundedCornerShape(3.dp)),
			)
		}
	}
}

@Composable
private fun Footer(view: NetworkWidgetView, colors: PreviewColors) {
	Row(
		modifier = Modifier.fillMaxWidth().height(50.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		FooterCell("免费流量", view.quotaLabel ?: "—", colors.ink, colors, Modifier.weight(1f))
		Box(Modifier.width(1.dp).fillMaxHeight().background(colors.line))
		FooterCell(
			"账号状态",
			view.accountLabel ?: "—",
			colors.tone(view.accountTone),
			colors,
			Modifier.weight(1f),
		)
		Box(Modifier.width(1.dp).fillMaxHeight().background(colors.line))
		FooterCell("欠费", view.overdueLabel ?: "—", colors.tone(view.overdueTone), colors, Modifier.weight(1f))
	}
}

@Composable
private fun FooterCell(
	label: String,
	value: String,
	valueColor: Color,
	colors: PreviewColors,
	modifier: Modifier,
) {
	Column(
		modifier = modifier.padding(start = 14.dp, end = 4.dp),
		verticalArrangement = Arrangement.spacedBy(3.dp),
	) {
		Text(label, color = colors.inkMuted, style = NetworkGlyphs.footerLabel, maxLines = 1)
		Text(value, color = valueColor, style = NetworkGlyphs.footerValue, maxLines = 1)
	}
}

@Composable
private fun EmptyBody(view: NetworkWidgetView, density: NetworkDensity, colors: PreviewColors) {
	Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = view.emptyTitle.orEmpty(),
				color = colors.ink,
				style = if (density == NetworkDensity.COMPACT) NetworkGlyphs.emptyTitleCompact else NetworkGlyphs.emptyTitle,
				maxLines = 1,
			)
			view.emptyDetail?.let {
				Text(
					text = it,
					color = colors.inkMuted,
					style = NetworkGlyphs.emptyDetail,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

/**
 * 预览用到的字形。
 *
 * 显式写 `lineHeight`：Compose 默认 text style 的 24sp 行高会让预览比 RemoteViews 里同字号
 * 的文字更高，多出来的部分在固定高度的卡片里就会被裁掉。
 */
private object NetworkGlyphs {
	val title = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
	val titleCompact = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
	val caption = TextStyle(fontSize = 10.sp, lineHeight = 13.sp)
	val chevron = TextStyle(fontSize = 18.sp, lineHeight = 20.sp)
	val usedCompact = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
	val percentCompact = TextStyle(fontSize = 10.sp, lineHeight = 13.sp)
	val usedWide = TextStyle(fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold)
	val usedFull = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
	val percent = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
	val percentFull = TextStyle(fontSize = 13.sp, lineHeight = 17.sp)
	val failure = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val footerLabel = TextStyle(fontSize = 10.sp, lineHeight = 13.sp)
	val footerValue = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
	val emptyTitle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
	val emptyTitleCompact = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
	val emptyDetail = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
}
