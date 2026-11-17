// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cn.qnxg.weihuda.widget.presentation.NetworkDensity
import cn.qnxg.weihuda.widget.presentation.NetworkTone
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetLayout
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetView
import cn.qnxg.weihuda.widget.presentation.WidgetPalette

private val surface = WidgetPalette.surface.provider()
private val line = WidgetPalette.line.provider()
private val ink = WidgetPalette.ink.provider()
private val inkMuted = WidgetPalette.inkMuted.provider()
private val error = WidgetPalette.error.provider()
private val success = WidgetPalette.success.provider()

/**
 * 流量卡片的视图，属于 Humble Object：只把 [NetworkWidgetView] 摆到屏幕上。
 *
 * 尺寸之间的差别只有「留下哪几层」：小尺寸丢掉页脚（免费流量 / 账号状态 / 欠费），
 * 因为三列文字在 110dp 里会被压成看不清的字号，而大号用量与进度条才是这张卡片的重点。
 */
@Composable
internal fun NetworkWidgetContent(context: Context, view: NetworkWidgetView) {
	val size = LocalSize.current
	val mode = NetworkWidgetLayout.density(size.width.value.toInt(), size.height.value.toInt())

	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(surface)
			.cornerRadius(18.dp)
			.clickable(openAppAction(context)),
	) {
		if (!view.hasData) EmptyBody(view, mode) else UsageBody(view, mode, size.width.value)
	}
}

@Composable
private fun ColumnScope.UsageBody(view: NetworkWidgetView, mode: NetworkDensity, sizeWidth: Float) {
	val gutter = if (mode == NetworkDensity.COMPACT) 12.dp else 14.dp

	Row(
		modifier = GlanceModifier
			.fillMaxWidth()
			.padding(start = gutter, end = gutter, top = 10.dp, bottom = 2.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = "校园网",
			style = TextStyle(
				color = ink,
				fontSize = if (mode == NetworkDensity.COMPACT) 12.sp else 13.sp,
				fontWeight = FontWeight.Bold,
			),
			maxLines = 1,
		)
		if (mode != NetworkDensity.COMPACT) {
			Spacer(GlanceModifier.width(6.dp))
			Text(
				text = "本月已用",
				style = TextStyle(color = inkMuted, fontSize = 10.sp),
				maxLines = 1,
			)
		}
		Spacer(GlanceModifier.defaultWeight())
		when {
			// 2×2 放不下「大号数字 + 百分比」并排，百分比挪到标题行
			mode == NetworkDensity.COMPACT -> view.percentLabel?.let {
				Text(
					text = it,
					style = TextStyle(color = inkMuted, fontSize = 10.sp),
					maxLines = 1,
				)
			}

			// 整张卡片都可点按，箭头只是与应用内设置页的视觉对齐
			mode == NetworkDensity.FULL -> Text(
				text = "›",
				style = TextStyle(color = inkMuted, fontSize = 18.sp),
				maxLines = 1,
			)
		}
	}

	Row(
		modifier = GlanceModifier
			.fillMaxWidth()
			.padding(start = gutter, end = gutter, top = 2.dp, bottom = 6.dp),
		verticalAlignment = Alignment.Bottom,
	) {
		Text(
			text = if (mode == NetworkDensity.COMPACT) view.usedLabelCompact else view.usedLabel,
			style = TextStyle(
				color = ink,
				fontSize = when (mode) {
					NetworkDensity.COMPACT -> 20.sp
					NetworkDensity.WIDE -> 24.sp
					NetworkDensity.FULL -> 32.sp
				},
				fontWeight = FontWeight.Bold,
			),
			maxLines = 1,
		)
		Spacer(GlanceModifier.defaultWeight())
		if (mode != NetworkDensity.COMPACT) {
			view.percentLabel?.let {
				Text(
					text = it,
					style = TextStyle(
						color = inkMuted,
						fontSize = if (mode == NetworkDensity.FULL) 13.sp else 12.sp,
					),
					maxLines = 1,
				)
			}
		}
	}

	// 进度条宽度按卡片的真实宽度算：Glance 1.2 没有按比例填充的修饰符
	val trackWidth = sizeWidth - gutter.value * 2
	Progress(fraction = view.progress, gutter = gutter, trackWidth = trackWidth)

	view.failureLabel?.let {
		Text(
			text = it,
			modifier = GlanceModifier.padding(start = gutter, end = gutter, top = 4.dp),
			style = TextStyle(color = error, fontSize = 9.sp),
			maxLines = 1,
		)
	}

	// 页脚贴底：4×4 多出来的空间留在进度条与分割线之间，而不是卡片底部
	Spacer(GlanceModifier.defaultWeight())

	if (mode == NetworkDensity.FULL) {
		Box(
			modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(line),
		) {}
		Footer(view)
	}
}

/**
 * 进度条。
 *
 * 内边距必须加在外层 Row 上：Glance 把 `background` 画在 padding 之外，直接写在带 padding 的
 * Box 上会让轨道撑满整张卡片。填充宽度按轨道真实宽度乘比例——Glance 1.2 没有按比例填充的
 * 宽度修饰符，所以 [NetworkWidget] 用 `SizeMode.Exact` 保证 `LocalSize` 就是实际尺寸。
 */
@Composable
private fun Progress(fraction: Float?, gutter: Dp, trackWidth: Float) {
	val value = fraction ?: return
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(start = gutter, end = gutter),
	) {
		Box(
			modifier = GlanceModifier
				.defaultWeight()
				.height(6.dp)
				.cornerRadius(3.dp)
				.background(line),
		) {
			if (value > 0f) {
				Box(
					modifier = GlanceModifier
						.width((trackWidth * value.coerceIn(0f, 1f)).dp)
						.fillMaxHeight()
						.cornerRadius(3.dp)
						.background(success),
				) {}
			}
		}
	}
}

@Composable
private fun Footer(view: NetworkWidgetView) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().height(50.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		FooterCell("免费流量", view.quotaLabel ?: "—", ink, GlanceModifier.defaultWeight())
		CellDivider()
		FooterCell("账号状态", view.accountLabel ?: "—", tone(view.accountTone), GlanceModifier.defaultWeight())
		CellDivider()
		FooterCell("欠费", view.overdueLabel ?: "—", tone(view.overdueTone), GlanceModifier.defaultWeight())
	}
}

@Composable
private fun FooterCell(label: String, value: String, valueColor: ColorProvider, modifier: GlanceModifier) {
	Column(
		modifier = modifier.padding(start = 14.dp, end = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = label,
			style = TextStyle(color = inkMuted, fontSize = 10.sp),
			maxLines = 1,
		)
		Spacer(GlanceModifier.height(3.dp))
		Text(
			text = value,
			style = TextStyle(color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
	}
}

@Composable
private fun CellDivider() {
	Box(
		modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(line),
	) {}
}

@Composable
private fun ColumnScope.EmptyBody(view: NetworkWidgetView, mode: NetworkDensity) {
	Column(
		modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = view.emptyTitle.orEmpty(),
			style = TextStyle(
				color = ink,
				fontSize = if (mode == NetworkDensity.COMPACT) 12.sp else 14.sp,
				fontWeight = FontWeight.Medium,
			),
			maxLines = 1,
		)
		view.emptyDetail?.let {
			Spacer(GlanceModifier.height(4.dp))
			Text(
				text = it.clip(if (mode == NetworkDensity.COMPACT) 16 else 32),
				style = TextStyle(color = inkMuted, fontSize = if (mode == NetworkDensity.COMPACT) 9.sp else 10.sp),
				maxLines = 2,
			)
		}
	}
}

private fun tone(tone: NetworkTone): ColorProvider = when (tone) {
	NetworkTone.OK -> success
	NetworkTone.BAD -> error
	NetworkTone.NEUTRAL -> inkMuted
}
