package cn.qnxg.weihuda.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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

internal val COMPACT_SIZE = DpSize(110.dp, 110.dp)

/** 4×2，多数启动器把它排得更松，实际高度会落在 4×3。 */
internal val WIDE_SIZE = DpSize(250.dp, 110.dp)

internal val MEDIUM_SIZE = DpSize(250.dp, 170.dp)

/** 4×4 */
internal val LARGE_SIZE = DpSize(250.dp, 250.dp)

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
	val mode = WidgetLayout.density(size.width.value.toInt(), size.height.value.toInt())
	val rows = WidgetLayout.visibleRows(
		heightDp = size.height.value.toInt(),
		density = mode,
		hasFailure = view.failureLabel != null && view.hasRows,
	)
	// 必须用带 Component 的显式 Intent：Glance 会给 Intent 附加 data URI，
	// 隐式 Intent（只带 package）会因此匹配不到启动 Activity。
	val openApp = actionStartActivity(
		context.packageManager.getLaunchIntentForPackage(context.packageName)
			?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName),
	)

	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(Palette.surface)
			.cornerRadius(18.dp)
			.padding(horizontal = 13.dp, vertical = 8.dp)
			.clickable(openApp),
		verticalAlignment = Alignment.Top,
	) {
		Header(view = view, mode = mode)
		Divider()
		Body(view = view, mode = mode, rows = rows)
		if (mode != WidgetDensity.COMPACT) Footer(view)
	}
}

@Composable
private fun Header(view: CourseWidgetView, mode: WidgetDensity) {
	Row(
		modifier = GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = view.weekLabel,
			style = TextStyle(
				color = Palette.brand,
				fontSize = if (mode == WidgetDensity.COMPACT) 11.sp else 12.sp,
				fontWeight = FontWeight.Bold,
			),
			maxLines = 1,
		)
		Spacer(GlanceModifier.defaultWeight())
		Text(
			text = if (mode == WidgetDensity.COMPACT) view.dayLabelShort else view.dayLabel,
			style = TextStyle(
				color = Palette.inkMuted,
				fontSize = if (mode == WidgetDensity.COMPACT) 10.sp else 11.sp,
			),
			maxLines = 1,
		)
	}
}

@Composable
private fun Divider() {
	Box(
		modifier = GlanceModifier
			.fillMaxWidth()
			.padding(vertical = 3.dp)
			.height(1.dp)
			.background(Palette.line),
	) {}
}

@Composable
private fun ColumnScope.Body(view: CourseWidgetView, mode: WidgetDensity, rows: Int) {
	when {
		!view.hasRows -> Hint(
			title = view.emptyTitle.orEmpty(),
			detail = (view.emptyDetail ?: "").clip(if (mode == WidgetDensity.COMPACT) 14 else 34),
		)

		else -> Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
			val failure = view.failureLabel
			if (failure != null && mode == WidgetDensity.LARGE) {
				Text(
					text = failure.clip(24),
					style = TextStyle(color = Palette.error, fontSize = 9.sp),
					maxLines = 1,
				)
			}
			if (mode == WidgetDensity.COMPACT) {
				view.focus?.let { CompactCard(it, view.focusTotalLabel) }
			} else {
				// 「还有 N 节」自己也要占一行，所以有剩余课程时少画一行
				val fits = if (view.rows.size > rows) (rows - 1).coerceAtLeast(0) else rows
				view.rows.take(fits).forEach { row ->
					when (mode) {
						WidgetDensity.WIDE -> WideRow(row)
						else -> LargeRow(row)
					}
				}
				val hidden = view.rows.size - fits
				if (hidden > 0) {
					Text(
						text = "还有 $hidden 节",
						style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
						maxLines = 1,
					)
				}
			}
		}
	}
}

/**
 * 2×2：只放大当前/下一节课，其余课程以「共 N 节」提示。
 * 方形小尺寸里塞多行必然被裁切，不如把一节课讲清楚。
 */
@Composable
private fun CompactCard(focus: FocusCard, totalLabel: String?) {
	Row(
		modifier = GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.height(38.dp)
				.background(ColorProvider(statusColor(focus.status)))
				.cornerRadius(2.dp),
		) {}
		Column(modifier = GlanceModifier.padding(start = 7.dp)) {
			Text(
				text = focus.timeRange,
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
			Text(
				text = focus.name.clip(7),
				style = TextStyle(color = Palette.ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = focus.detail.clip(14),
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
	if (totalLabel != null) {
		Text(
			text = totalLabel,
			modifier = GlanceModifier.padding(top = 4.dp),
			style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
			maxLines = 1,
		)
	}
}

/** 4×2：每节课压成一行，名称与地点同一行。 */
@Composable
private fun WideRow(row: SessionRow) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = row.startTime,
			modifier = GlanceModifier.width(34.dp),
			style = TextStyle(color = Palette.ink, fontSize = 10.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		Box(
			modifier = GlanceModifier
				.width(2.dp)
				.height(14.dp)
				.background(ColorProvider(statusColor(row.status)))
				.cornerRadius(1.dp),
		) {}
		Text(
			text = row.name.clip(9),
			modifier = GlanceModifier.defaultWeight().padding(start = 6.dp),
			style = TextStyle(color = Palette.ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		row.place?.let { place ->
			Text(
				text = place.clip(6),
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
}

/** 4×4：两行一节课，带状态文字（状态不能只靠颜色表达）。 */
@Composable
private fun LargeRow(row: SessionRow) {
	val accent = ColorProvider(statusColor(row.status))
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(modifier = GlanceModifier.width(40.dp)) {
			Text(
				text = row.startTime,
				style = TextStyle(color = Palette.ink, fontSize = 11.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = row.endTime,
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.height(28.dp)
				.background(accent)
				.cornerRadius(2.dp),
		) {}
		Column(modifier = GlanceModifier.defaultWeight().padding(start = 8.dp)) {
			Text(
				text = row.name.clip(14),
				style = TextStyle(color = Palette.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = listOfNotNull(
					row.place,
					"第 ${row.periodLabel} 节".takeIf { row.periodLabel.contains("-") },
				).joinToString(" · ").clip(20),
				style = TextStyle(color = Palette.inkMuted, fontSize = 10.sp),
				maxLines = 1,
			)
		}
		Text(
			text = row.statusLabel,
			style = TextStyle(color = accent, fontSize = 9.sp),
			maxLines = 1,
		)
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
			style = TextStyle(color = Palette.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		Spacer(GlanceModifier.height(4.dp))
		Text(
			text = detail,
			style = TextStyle(color = Palette.inkMuted, fontSize = 10.sp),
			maxLines = 2,
		)
	}
}

@Composable
private fun Footer(view: CourseWidgetView) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(top = 5.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = view.footerLabel.clip(20),
			style = TextStyle(
				color = if (view.footerIsError) Palette.error else Palette.inkMuted,
				fontSize = 9.sp,
			),
			maxLines = 1,
		)
		Spacer(GlanceModifier.defaultWeight())
		Text(
			text = "刷新",
			modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()),
			style = TextStyle(color = Palette.brand, fontSize = 10.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
	}
}

/** Glance 1.2 的 Text 没有 overflow 参数，按字符数手动截断。 */
internal fun String.clip(max: Int): String =
	if (length <= max) this else take((max - 1).coerceAtLeast(1)) + "…"

private fun statusColor(status: CourseStatus): Color = when (status) {
	CourseStatus.ONGOING -> Palette.ongoing
	CourseStatus.SOON -> Palette.soon
	CourseStatus.UPCOMING -> Palette.upcoming
	CourseStatus.FINISHED -> Palette.finished
}

private object Palette {
	val surface = ColorProvider(Color(0xFFFFFFFF))
	val line = ColorProvider(Color(0xFFEDF1F5))
	val brand = ColorProvider(Color(0xFF318CCB))
	val ink = ColorProvider(Color(0xFF1B2733))
	val inkMuted = ColorProvider(Color(0xFF6A7883))
	val error = ColorProvider(Color(0xFFD64545))

	val ongoing = Color(0xFF2E7D32)
	val soon = Color(0xFFE08600)
	val upcoming = Color(0xFF4A5560)
	val finished = Color(0xFF9AA5AF)
}
