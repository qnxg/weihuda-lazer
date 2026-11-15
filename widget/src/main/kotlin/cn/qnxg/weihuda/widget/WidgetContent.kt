package cn.qnxg.weihuda.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import cn.qnxg.weihuda.core.domain.CourseStatus
import cn.qnxg.weihuda.core.domain.SessionSnapshot
import cn.qnxg.weihuda.core.domain.WidgetSnapshot
import cn.qnxg.weihuda.core.domain.isPlausibleWeek
import cn.qnxg.weihuda.core.domain.monthDayLabel
import cn.qnxg.weihuda.core.domain.weekdayLabel

/** 三种信息密度，分别对应 2x2 / 4x2 / 4x4。 */
internal enum class WidgetMode {
	COMPACT,
	WIDE,
	LARGE,
}

@Composable
internal fun WidgetContent(context: Context, snapshot: WidgetSnapshot?) {
	val size = LocalSize.current
	val mode = when {
		size.width < 180.dp -> WidgetMode.COMPACT
		size.height >= 230.dp -> WidgetMode.LARGE
		else -> WidgetMode.WIDE
	}
	// 桌面单元格的实际高度和 dp 估算并不一致（各家启动器不同），按真实尺寸决定行数
	val rows = when (mode) {
		WidgetMode.COMPACT -> 1
		WidgetMode.WIDE -> when {
			size.height >= 160.dp -> 4
			size.height >= 130.dp -> 3
			else -> 2
		}
		WidgetMode.LARGE -> if (size.height >= 280.dp) 6 else 5
	}
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
		Header(snapshot = snapshot, mode = mode)
		Divider(mode)
		Body(snapshot = snapshot, mode = mode, rows = rows)
		if (mode != WidgetMode.COMPACT) Footer(snapshot)
	}
}

@Composable
private fun Header(snapshot: WidgetSnapshot?, mode: WidgetMode) {
	Row(
		modifier = GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = weekLabel(snapshot),
			style = TextStyle(
				color = Palette.brand,
				fontSize = if (mode == WidgetMode.COMPACT) 11.sp else 12.sp,
				fontWeight = FontWeight.Bold,
			),
			maxLines = 1,
		)
		Spacer(GlanceModifier.defaultWeight())
		Text(
			text = dayLabel(snapshot, mode),
			style = TextStyle(
				color = Palette.inkMuted,
				fontSize = if (mode == WidgetMode.COMPACT) 10.sp else 11.sp,
			),
			maxLines = 1,
		)
	}
}

@Composable
private fun Divider(mode: WidgetMode) {
	Box(
		modifier = GlanceModifier
			.fillMaxWidth()
			.padding(vertical = 3.dp)
			.height(1.dp)
			.background(Palette.line),
	) {}
}

@Composable
private fun ColumnScope.Body(snapshot: WidgetSnapshot?, mode: WidgetMode, rows: Int) {
	when {
		snapshot == null -> Hint(
			title = "正在获取课表",
			detail = if (mode == WidgetMode.COMPACT) "点按设置" else "点按应用完成设置或刷新",
		)

		snapshot.sessions.isEmpty() -> {
			val error = snapshot.error
			Hint(
				title = if (error != null) "刷新失败" else "今日无课",
				detail = error?.clip(if (mode == WidgetMode.COMPACT) 14 else 34)
					?: if (mode == WidgetMode.COMPACT) "好好休息" else "今天没有排课，点按查看完整课表",
			)
		}

		else -> Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
			val error = snapshot.error
			if (error != null && mode == WidgetMode.LARGE) {
				Text(
					text = "上次刷新失败：${error.clip(24)}",
					style = TextStyle(color = Palette.error, fontSize = 9.sp),
					maxLines = 1,
				)
			}
			if (mode == WidgetMode.COMPACT) {
				CompactCard(snapshot.sessions)
			} else {
				snapshot.sessions.take(rows).forEach { session ->
					when (mode) {
						WidgetMode.WIDE -> WideRow(session)
						else -> LargeRow(session)
					}
				}
			}
			val hidden = snapshot.sessions.size - rows
			if (hidden > 0 && mode != WidgetMode.COMPACT) {
				Text(
					text = "还有 $hidden 节",
					style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
					maxLines = 1,
				)
			}
		}
	}
}

/**
 * 2x2：只放大当前/下一节课，其余课程以「共 N 节」提示。
 * 方形小尺寸里塞多行必然被裁切，不如把一节课讲清楚。
 */
@Composable
private fun CompactCard(sessions: List<SessionSnapshot>) {
	val primary = sessions.firstOrNull { it.statusEnum == CourseStatus.ONGOING }
		?: sessions.firstOrNull { it.statusEnum != CourseStatus.FINISHED }
		?: sessions.first()
	Row(
		modifier = GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = GlanceModifier
				.width(3.dp)
				.height(38.dp)
				.background(ColorProvider(statusColor(primary.statusEnum)))
				.cornerRadius(2.dp),
		) {}
		Column(modifier = GlanceModifier.padding(start = 7.dp)) {
			Text(
				text = "${primary.startTime} - ${primary.endTime}",
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
			Text(
				text = primary.name.clip(7),
				style = TextStyle(color = Palette.ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = listOfNotNull(
					primary.place?.takeIf { it.isNotBlank() }?.clip(6),
					primary.statusEnum.label.takeIf { it != CourseStatus.UPCOMING.label },
				).joinToString(" · "),
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
	if (sessions.size > 1) {
		Text(
			text = "今日共 ${sessions.size} 节",
			modifier = GlanceModifier.padding(top = 4.dp),
			style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
			maxLines = 1,
		)
	}
}

/** 4x2：每节课压成一行，名称与地点同一行。 */
@Composable
private fun WideRow(session: SessionSnapshot) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = session.startTime,
			modifier = GlanceModifier.width(34.dp),
			style = TextStyle(color = Palette.ink, fontSize = 10.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		Box(
			modifier = GlanceModifier
				.width(2.dp)
				.height(14.dp)
				.background(ColorProvider(statusColor(session.statusEnum)))
				.cornerRadius(1.dp),
		) {}
		Text(
			text = session.name.clip(9),
			modifier = GlanceModifier.defaultWeight().padding(start = 6.dp),
			style = TextStyle(color = Palette.ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
			maxLines = 1,
		)
		session.place?.takeIf { it.isNotBlank() }?.let { place ->
			Text(
				text = place.clip(6),
				style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
				maxLines = 1,
			)
		}
	}
}

/** 4x4：两行一节课，带状态文字。 */
@Composable
private fun LargeRow(session: SessionSnapshot) {
	val accent = ColorProvider(statusColor(session.statusEnum))
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(modifier = GlanceModifier.width(40.dp)) {
			Text(
				text = session.startTime,
				style = TextStyle(color = Palette.ink, fontSize = 11.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = session.endTime,
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
				text = session.name.clip(14),
				style = TextStyle(color = Palette.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium),
				maxLines = 1,
			)
			Text(
				text = listOfNotNull(
					session.place?.takeIf { it.isNotBlank() },
					"第 ${session.periodLabel} 节".takeIf { session.startPeriod != session.endPeriod },
				).joinToString(" · ").clip(20),
				style = TextStyle(color = Palette.inkMuted, fontSize = 10.sp),
				maxLines = 1,
			)
		}
		Text(
			text = session.statusEnum.label,
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
private fun Footer(snapshot: WidgetSnapshot?) {
	Row(
		modifier = GlanceModifier.fillMaxWidth().padding(top = 5.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = sourceLabel(snapshot),
			style = TextStyle(color = Palette.inkMuted, fontSize = 9.sp),
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

private fun weekLabel(snapshot: WidgetSnapshot?): String {
	snapshot ?: return "微生活课表"
	return if (isPlausibleWeek(snapshot.week)) "第 ${snapshot.week} 周" else "假期"
}

private fun dayLabel(snapshot: WidgetSnapshot?, mode: WidgetMode): String {
	snapshot ?: return ""
	val weekday = weekdayLabel(snapshot.weekday)
	if (mode == WidgetMode.COMPACT) return weekday
	val date = snapshot.localDate ?: return weekday
	return "$weekday ${monthDayLabel(date)}"
}

private fun sourceLabel(snapshot: WidgetSnapshot?): String {
	snapshot ?: return "等待首次刷新"
	val source = if (snapshot.isDemo) "演示数据" else "实时数据"
	val time = snapshot.generatedAtLabel().let { if (it.isEmpty()) "" else " · $it" }
	return "$source$time"
}

/** Glance 1.2 的 Text 没有 overflow 参数，按字符数手动截断。 */
private fun String.clip(max: Int): String =
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
