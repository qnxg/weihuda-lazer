// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
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
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.widget.presentation.CourseWidgetView
import cn.qnxg.weihuda.widget.presentation.FocusCard
import cn.qnxg.weihuda.widget.presentation.SessionRow
import cn.qnxg.weihuda.widget.presentation.WidgetDensity
import cn.qnxg.weihuda.widget.presentation.WidgetLayout
import cn.qnxg.weihuda.widget.presentation.WidgetPalette

private val surface = WidgetPalette.surface.provider()
private val line = WidgetPalette.line.provider()
private val brand = WidgetPalette.brand.provider()
private val ink = WidgetPalette.ink.provider()
private val inkMuted = WidgetPalette.inkMuted.provider()
private val error = WidgetPalette.error.provider()

/**
 * 小组件的视图，属于 Humble Object：只把 [CourseWidgetView] 摆到屏幕上，不做业务判断。
 *
 * 布局只用 RemoteViews 允许的能力（Text / Image / Box / Row / Column / Spacer 与 background、
 * cornerRadius、clickable），这也是为什么文案截断要在这里按字符做。
 */
@Composable
internal fun WidgetContent(context: Context, view: CourseWidgetView) {
	val size = LocalSize.current
	// 桌面单元格的实际高度和 dp 估算并不一致（各家启动器不同），按真实尺寸决定行数
	val heightDp = size.height.value.toInt()
	val mode = WidgetLayout.density(size.width.value.toInt(), heightDp)
	val rows = WidgetLayout.visibleRows(heightDp, mode)
	val openApp = openAppAction(context)

	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(surface)
			.cornerRadius(18.dp)
			.clickable(openApp),
		verticalAlignment = Alignment.Top,
	) {
		Header(view = view, mode = mode)
		Divider()
		Body(view = view, mode = mode, rows = rows, heightDp = heightDp)
		if (mode != WidgetDensity.COMPACT) Footer(view)
	}
}

@Composable
private fun Header(view: CourseWidgetView, mode: WidgetDensity) {
	Row(
		modifier = GlanceModifier
			.fillMaxWidth()
			.padding(
				start = if (mode == WidgetDensity.COMPACT) 12.dp else 14.dp,
				end = if (mode == WidgetDensity.COMPACT) 12.dp else 14.dp,
				top = 8.dp,
				bottom = 2.dp,
			),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = view.weekLabel,
			style = TextStyle(color = brand, fontSize = 11.sp, fontWeight = FontWeight.Bold),
			maxLines = 1,
		)
		Spacer(GlanceModifier.defaultWeight())
		Text(
			text = if (mode == WidgetDensity.COMPACT) view.dayLabelShort else view.dayLabel,
			style = TextStyle(color = inkMuted, fontSize = 10.sp),
			maxLines = 1,
		)
	}
}

@Composable
private fun Divider() {
	Box(
		modifier = GlanceModifier
			.fillMaxWidth()
			.height(1.dp)
			.background(line),
	) {}
}

/** 行分割线从状态轨右侧开始，和参考图一样让左侧竖条保持连续。 */
@Composable
private fun RowDivider() {
	// padding 要加在外层：Glance 的 background 会跳过同一元素上的 padding
	Row(modifier = GlanceModifier.fillMaxWidth().padding(start = 3.dp)) {
		Box(
			modifier = GlanceModifier.defaultWeight().height(1.dp).background(line),
		) {}
	}
}

@Composable
private fun ColumnScope.Body(view: CourseWidgetView, mode: WidgetDensity, rows: Int, heightDp: Int) {
	when {
		!view.hasRows -> Hint(
			title = view.emptyTitle.orEmpty(),
			detail = (view.emptyDetail ?: "").clip(if (mode == WidgetDensity.COMPACT) 14 else 34),
		)

		mode == WidgetDensity.COMPACT -> view.focus?.let { CompactFocus(it, view.focusTotalLabel) }

		else -> Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
			// 「还有 N 节」自己也要占一行：先试着放满，放不下才少画一行
			val overflow = view.rows.size > rows
			val fits = when {
				!overflow -> view.rows.size
				WidgetLayout.hasRoomForHidden(heightDp, mode, rows) -> rows
				else -> (rows - 1).coerceAtLeast(0)
			}
			val shown = view.rows.take(fits)
			val hidden = view.rows.size - fits
			val mediumHeight = WidgetLayout.mediumRowHeight(heightDp, fits, hidden > 0)
			shown.forEachIndexed { index, row ->
				when (mode) {
					WidgetDensity.WIDE -> WideRow(row)
					WidgetDensity.MEDIUM -> MediumRow(row, mediumHeight)
					else -> LargeRow(row)
				}
				if (index != shown.lastIndex) RowDivider()
			}
			if (hidden > 0) {
				Text(
					text = "还有 $hidden 节",
					modifier = GlanceModifier.padding(start = 14.dp, top = 3.dp),
					style = TextStyle(color = inkMuted, fontSize = 9.sp),
					maxLines = 1,
				)
			}
		}
	}
}

/**
 * 2×2：只放大当前/下一节课，其余课程以「共 N 节」提示。
 * 方形小尺寸里塞多行必然被裁切，不如把一节课讲清楚。
 */
@Composable
private fun CompactFocus(focus: FocusCard, totalLabel: String?) {
	Column(modifier = GlanceModifier.fillMaxWidth()) {
		Row(
			modifier = GlanceModifier.fillMaxWidth().height(56.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(
				modifier = GlanceModifier
					.width(3.dp)
					.fillMaxHeight()
					.background(statusColor(focus.status)),
			) {}
			Column(modifier = GlanceModifier.defaultWeight().padding(start = 10.dp, top = 6.dp, end = 12.dp)) {
				Text(
					text = "${focus.startTime} - ${focus.endTime}",
					style = TextStyle(color = inkMuted, fontSize = 9.sp),
					maxLines = 1,
				)
				Text(
					text = focus.name.clip(9),
					style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium),
					maxLines = 1,
				)
				Text(
					text = focus.detail.clip(18),
					style = TextStyle(color = inkMuted, fontSize = 9.sp),
					maxLines = 1,
				)
			}
		}
		if (totalLabel != null) {
			Text(
				text = totalLabel,
				modifier = GlanceModifier.padding(start = 13.dp, top = 3.dp),
				style = TextStyle(color = inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
}

/** 4×2：一行一节课，名称与状态放得下，节次/地点留给 4×4 和应用。 */
@Composable
private fun WideRow(row: SessionRow) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().height(24.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.fillMaxHeight()
				.background(statusColor(row.status)),
		) {}
		Text(
			text = row.startTime,
			modifier = GlanceModifier.width(47.dp).padding(start = 14.dp),
			style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Bold),
			maxLines = 1,
		)
		Text(
			text = row.name.clip(10),
			modifier = GlanceModifier.defaultWeight().padding(start = 9.dp, end = 6.dp),
			style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		Text(
			text = row.statusLabel,
			modifier = GlanceModifier.padding(end = 14.dp),
			style = TextStyle(color = statusColor(row.status), fontSize = 9.sp),
			maxLines = 1,
		)
	}
}

/** 4×3：两行布局的紧凑版，三节课在 170dp 内放得下。 */
@Composable
private fun MediumRow(row: SessionRow, heightDp: Int) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().height(heightDp.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.fillMaxHeight()
				.background(statusColor(row.status)),
		) {}
		Column(modifier = GlanceModifier.width(54.dp).padding(start = 14.dp)) {
			Text(
				text = row.startTime,
				style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Bold),
				maxLines = 1,
			)
			Text(
				text = row.endTime,
				style = TextStyle(color = inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
		Column(modifier = GlanceModifier.defaultWeight().padding(start = 9.dp, end = 12.dp)) {
			Row(
				modifier = GlanceModifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = row.name.clip(12),
					modifier = GlanceModifier.defaultWeight(),
					style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
					maxLines = 1,
				)
				Text(
					text = row.statusLabel,
					modifier = GlanceModifier.padding(start = 5.dp),
					style = TextStyle(color = statusColor(row.status), fontSize = 9.sp),
					maxLines = 1,
				)
			}
			Text(
				text = row.detail.clip(22),
				style = TextStyle(color = inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
}

/** 4×4：参考课表条目的两行布局，时间竖排，节次 | 地点 | 教师同一行。 */
@Composable
private fun LargeRow(row: SessionRow) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().height(48.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.fillMaxHeight()
				.background(statusColor(row.status)),
		) {}
		Column(modifier = GlanceModifier.width(58.dp).padding(start = 14.dp)) {
			Text(
				text = row.startTime,
				style = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.Bold),
				maxLines = 1,
			)
			Text(
				text = row.endTime,
				style = TextStyle(color = inkMuted, fontSize = 10.sp),
				maxLines = 1,
			)
		}
		Column(modifier = GlanceModifier.defaultWeight().padding(start = 8.dp, end = 10.dp)) {
			Row(
				modifier = GlanceModifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = row.name.clip(12),
					modifier = GlanceModifier.defaultWeight(),
					style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium),
					maxLines = 1,
				)
				Text(
					text = row.statusLabel,
					modifier = GlanceModifier.padding(start = 4.dp),
					style = TextStyle(color = statusColor(row.status), fontSize = 9.sp),
					maxLines = 1,
				)
			}
			Text(
				text = row.detail.clip(24),
				style = TextStyle(color = inkMuted, fontSize = 10.sp),
				maxLines = 1,
			)
		}
	}
}

@Composable
private fun ColumnScope.Hint(title: String, detail: String) {
	Column(
		modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = title,
			style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		Spacer(GlanceModifier.height(4.dp))
		Text(
			text = detail,
			style = TextStyle(color = inkMuted, fontSize = 10.sp),
			maxLines = 2,
		)
	}
}

@Composable
private fun Footer(view: CourseWidgetView) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 2.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		view.failureLabel?.let {
			Text(
				text = it.clip(26),
				modifier = GlanceModifier.defaultWeight(),
				style = TextStyle(color = error, fontSize = 9.sp),
				maxLines = 1,
			)
		} ?: Spacer(GlanceModifier.defaultWeight())
		Text(
			text = "刷新",
			modifier = GlanceModifier
				.clickable(actionRunCallback<RefreshAction>())
				.padding(horizontal = 8.dp, vertical = 4.dp),
			style = TextStyle(color = brand, fontSize = 10.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
	}
}

private fun statusColor(status: CourseStatus): ColorProvider = WidgetPalette.status(status).provider()
