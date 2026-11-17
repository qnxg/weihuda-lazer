// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.widget.presentation.CourseWidgetPresenter
import cn.qnxg.weihuda.widget.presentation.CourseWidgetView
import cn.qnxg.weihuda.widget.presentation.FocusCard
import cn.qnxg.weihuda.widget.presentation.SessionRow
import cn.qnxg.weihuda.widget.presentation.WidgetDensity
import cn.qnxg.weihuda.widget.presentation.WidgetLayout
import cn.qnxg.weihuda.widget.presentation.WidgetPalette
import java.time.LocalTime

enum class PreviewMode { COMPACT, WIDE, LARGE }

/** 桌面令牌的 Compose 版本；颜色只有 [WidgetPalette] 一份。 */
@Immutable
private class PreviewColors(private val night: Boolean) {
	val surface = Color(WidgetPalette.surface.resolve(night))
	val line = Color(WidgetPalette.line.resolve(night))
	val brand = Color(WidgetPalette.brand.resolve(night))
	val ink = Color(WidgetPalette.ink.resolve(night))
	val inkMuted = Color(WidgetPalette.inkMuted.resolve(night))
	val error = Color(WidgetPalette.error.resolve(night))

	fun status(status: CourseStatus): Color =
		Color(WidgetPalette.status(status).resolve(night))
}

/**
 * 应用内的近似预览。
 *
 * 桌面真正的渲染发生在 RemoteViews 里，应用内无法复用那套布局；但**文案来自同一个
 * presenter**（[CourseWidgetPresenter]），颜色来自同一个 [WidgetPalette]，
 * 所以数据、顺序、状态与深浅色不会有两套说法。
 */
@Composable
fun CourseWidgetPreview(snapshot: TodayCourses?, mode: PreviewMode) {
	val view = CourseWidgetPresenter.present(snapshot, LocalTime.now(CampusClock.clock()))
	val colors = PreviewColors(isSystemInDarkTheme())
	val width = if (mode == PreviewMode.COMPACT) 110.dp else 250.dp
	val height = when (mode) {
		PreviewMode.COMPACT -> 110.dp
		PreviewMode.WIDE -> 110.dp
		PreviewMode.LARGE -> 250.dp
	}
	// 与桌面同一套尺寸规则，否则预览会承诺桌面上放不下的行数
	val density = when (mode) {
		PreviewMode.COMPACT -> WidgetDensity.COMPACT
		PreviewMode.WIDE -> WidgetDensity.WIDE
		// 应用里把 4×3 和 4×4 都当成 LARGE 预览；桌面会按真实高度落到 MEDIUM
		PreviewMode.LARGE -> WidgetDensity.LARGE
	}
	val rows = WidgetLayout.visibleRows(height.value.toInt(), density)

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = colors.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier.width(width).height(height),
	) {
		PreviewBody(view = view, mode = mode, rows = rows, colors = colors, heightDp = height.value.toInt(), density = density)
	}
}

@Composable
private fun PreviewBody(
	view: CourseWidgetView,
	mode: PreviewMode,
	rows: Int,
	colors: PreviewColors,
	heightDp: Int,
	density: WidgetDensity,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		Header(view = view, mode = mode, colors = colors)
		Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))

		if (!view.hasRows) {
			Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						text = view.emptyTitle.orEmpty(),
						color = colors.ink,
						style = Glyphs.hintTitle,
						maxLines = 1,
					)
					Text(
						text = view.emptyDetail.orEmpty(),
						color = colors.inkMuted,
						style = Glyphs.hintBody,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		} else if (mode == PreviewMode.COMPACT) {
			view.focus?.let { CompactFocus(it, view.focusTotalLabel, colors) }
		} else {
			// clipToBounds：多出来的行被裁掉，而不是压在页脚上
			Column(modifier = Modifier.weight(1f).clipToBounds()) {
				// 「还有 N 节」自己也要占一行：先试着放满，放不下才少画一行
				val overflow = view.rows.size > rows
				val fits = when {
					!overflow -> view.rows.size
					WidgetLayout.hasRoomForHidden(heightDp, density, rows) -> rows
					else -> (rows - 1).coerceAtLeast(0)
				}
				val shown = view.rows.take(fits)
				shown.forEachIndexed { index, row ->
					if (mode == PreviewMode.WIDE) WideRow(row, colors) else LargeRow(row, colors)
					if (index != shown.lastIndex) RowDivider(colors)
				}
				val hidden = view.rows.size - fits
				if (hidden > 0) {
					Text(
						text = "还有 $hidden 节",
						color = colors.inkMuted,
						style = Glyphs.hidden,
						modifier = Modifier.padding(start = 14.dp, top = 3.dp),
					)
				}
			}
		}

		if (mode != PreviewMode.COMPACT) Footer(view, colors)
	}
}

@Composable
private fun RowDivider(colors: PreviewColors) {
	Row(Modifier.fillMaxWidth().height(1.dp)) {
		Spacer(Modifier.width(3.dp))
		Box(Modifier.weight(1f).fillMaxHeight().background(colors.line))
	}
}

@Composable
private fun Header(view: CourseWidgetView, mode: PreviewMode, colors: PreviewColors) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				start = if (mode == PreviewMode.COMPACT) 12.dp else 14.dp,
				end = if (mode == PreviewMode.COMPACT) 12.dp else 14.dp,
				top = 8.dp,
				bottom = 2.dp,
			),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = view.weekLabel,
			color = colors.brand,
			style = Glyphs.week,
			maxLines = 1,
		)
		Spacer(Modifier.weight(1f))
		Text(
			text = if (mode == PreviewMode.COMPACT) view.dayLabelShort else view.dayLabel,
			color = colors.inkMuted,
			style = Glyphs.day,
			maxLines = 1,
		)
	}
}

@Composable
private fun CompactFocus(focus: FocusCard, totalLabel: String?, colors: PreviewColors) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth().height(56.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(Modifier.width(3.dp).fillMaxHeight().background(colors.status(focus.status)))
			Column(Modifier.weight(1f).padding(start = 10.dp, top = 6.dp, end = 12.dp)) {
				Text(
					text = "${focus.startTime} - ${focus.endTime}",
					color = colors.inkMuted,
					style = Glyphs.focusTime,
					maxLines = 1,
				)
				Text(
					text = focus.name,
					color = colors.ink,
					style = Glyphs.focusName,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = focus.detail,
					color = colors.inkMuted,
					style = Glyphs.focusDetail,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
		if (totalLabel != null) {
			Text(
				text = totalLabel,
				color = colors.inkMuted,
				style = Glyphs.total,
				modifier = Modifier.padding(start = 13.dp, top = 3.dp),
			)
		}
	}
}

@Composable
private fun WideRow(row: SessionRow, colors: PreviewColors) {
	Row(
		modifier = Modifier.fillMaxWidth().height(24.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(Modifier.width(3.dp).fillMaxHeight().background(colors.status(row.status)))
		Text(
			text = row.startTime,
			color = colors.ink,
			style = Glyphs.wideTime,
			modifier = Modifier.width(47.dp).padding(start = 14.dp),
			maxLines = 1,
		)
		Text(
			text = row.name,
			color = colors.ink,
			style = Glyphs.wideName,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f).padding(start = 9.dp, end = 6.dp),
		)
		Text(
			text = row.statusLabel,
			color = colors.status(row.status),
			style = Glyphs.wideStatus,
			modifier = Modifier.padding(end = 14.dp),
			maxLines = 1,
		)
	}
}

@Composable
private fun LargeRow(row: SessionRow, colors: PreviewColors) {
	Row(
		modifier = Modifier.fillMaxWidth().height(48.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(Modifier.width(3.dp).fillMaxHeight().background(colors.status(row.status)))
		Column(Modifier.width(58.dp).padding(start = 14.dp)) {
			Text(row.startTime, color = colors.ink, style = Glyphs.largeTime, maxLines = 1)
			Text(row.endTime, color = colors.inkMuted, style = Glyphs.largeEnd, maxLines = 1)
		}
		Column(Modifier.weight(1f).padding(start = 8.dp, end = 10.dp)) {
			Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = row.name,
					color = colors.ink,
					style = Glyphs.largeName,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				Text(
					text = row.statusLabel,
					color = colors.status(row.status),
					style = Glyphs.largeStatus,
					maxLines = 1,
					modifier = Modifier.padding(start = 4.dp),
				)
			}
			Text(
				text = row.detail,
				color = colors.inkMuted,
				style = Glyphs.largeDetail,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
private fun Footer(view: CourseWidgetView, colors: PreviewColors) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 2.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = view.failureLabel.orEmpty(),
			color = colors.error,
			style = Glyphs.failure,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)
		Text(
			text = "刷新",
			color = colors.brand,
			style = Glyphs.refresh,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
		)
	}
}

/**
 * 预览用到的字形。
 *
 * Compose 的默认 `LocalTextStyle` 是 bodyLarge（lineHeight 24sp），直接改 fontSize 会留着
 * 24sp 的行高，于是预览每行都比 RemoteViews 里的同字号文字高几个 dp——多出来的行就被裁掉。
 * 这里显式写 lineHeight，让预览的行距与桌面一致。
 */
private object Glyphs {
	val week = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
	val day = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
	val focusTime = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val focusName = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
	val focusDetail = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val total = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val wideTime = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
	val wideName = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
	val wideStatus = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val largeTime = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
	val largeEnd = TextStyle(fontSize = 10.sp, lineHeight = 13.sp)
	val largeName = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
	val largeDetail = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
	val largeStatus = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val hidden = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val failure = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val refresh = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
	val hintTitle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
	val hintBody = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
}
