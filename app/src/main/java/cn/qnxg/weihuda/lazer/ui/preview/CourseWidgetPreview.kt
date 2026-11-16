package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors
import cn.qnxg.weihuda.widget.presentation.CourseWidgetPresenter
import cn.qnxg.weihuda.widget.presentation.CourseWidgetView
import cn.qnxg.weihuda.widget.presentation.SessionRow
import cn.qnxg.weihuda.widget.presentation.WidgetDensity
import cn.qnxg.weihuda.widget.presentation.WidgetLayout
import java.time.LocalTime

enum class PreviewMode { COMPACT, WIDE, LARGE }

/**
 * 应用内的近似预览。
 *
 * 桌面真正的渲染发生在 RemoteViews 里，应用内无法复用那套布局；但**文案来自同一个
 * presenter**（[CourseWidgetPresenter]），所以数据、顺序、状态和桌面上不会有两套说法。
 */
@Composable
fun CourseWidgetPreview(snapshot: TodayCourses?, mode: PreviewMode) {
	val view = CourseWidgetPresenter.present(snapshot, LocalTime.now(CampusClock.clock()))
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
		PreviewMode.LARGE -> WidgetDensity.LARGE
	}
	val rows = WidgetLayout.visibleRows(height.value.toInt(), density, view.failureLabel != null && view.hasRows)

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier.width(width).height(height),
	) {
		PreviewBody(view = view, mode = mode, rows = rows)
	}
}

@Composable
private fun PreviewBody(view: CourseWidgetView, mode: PreviewMode, rows: Int) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 13.dp, vertical = 8.dp),
	) {
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = view.weekLabel,
				color = LazerColors.Brand,
				style = if (mode == PreviewMode.COMPACT) Glyphs.weekCompact else Glyphs.week,
				maxLines = 1,
			)
			Spacer(Modifier.weight(1f))
			Text(
				text = if (mode == PreviewMode.COMPACT) view.dayLabelShort else view.dayLabel,
				color = LazerColors.InkMuted,
				style = if (mode == PreviewMode.COMPACT) Glyphs.dayCompact else Glyphs.day,
				maxLines = 1,
			)
		}
		Box(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).height(1.dp).background(LazerColors.Line))

		if (!view.hasRows) {
			Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						text = view.emptyTitle.orEmpty(),
						color = LazerColors.Ink,
						style = Glyphs.hintTitle,
						maxLines = 1,
					)
					Text(
						text = view.emptyDetail.orEmpty(),
						color = LazerColors.InkMuted,
						style = Glyphs.hintBody,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		} else {
			// clipToBounds：多出来的行被裁掉，而不是压在页脚上
			Column(modifier = Modifier.weight(1f).clipToBounds()) {
				if (mode == PreviewMode.COMPACT) {
					view.focus?.let { focus ->
						Row(verticalAlignment = Alignment.CenterVertically) {
							Box(
								Modifier
									.width(3.dp)
									.height(38.dp)
									.background(statusColor(focus.status), RoundedCornerShape(2.dp)),
							)
							Column(Modifier.padding(start = 7.dp)) {
								Text(focus.timeRange, color = LazerColors.InkMuted, style = Glyphs.rowPlace, maxLines = 1)
								Text(
									text = focus.name,
									color = LazerColors.Ink,
									style = Glyphs.rowName,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis,
								)
								Text(
									text = focus.detail,
									color = LazerColors.InkMuted,
									style = Glyphs.rowPlace,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}
						view.focusTotalLabel?.let {
							Text(
								it,
								color = LazerColors.InkMuted,
								style = Glyphs.rowPlace,
								modifier = Modifier.padding(top = 4.dp),
							)
						}
					}
				} else {
					val fits = if (view.rows.size > rows) (rows - 1).coerceAtLeast(0) else rows
					view.rows.take(fits).forEach { row ->
						if (mode == PreviewMode.LARGE) LargeRow(row) else WideRow(row)
					}
					val hidden = view.rows.size - fits
					if (hidden > 0) {
						Text("还有 $hidden 节", color = LazerColors.InkMuted, style = Glyphs.rowPlace, maxLines = 1)
					}
				}
			}
		}

		if (mode != PreviewMode.COMPACT) {
			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = view.footerLabel,
					color = if (view.footerIsError) LazerColors.Danger else LazerColors.InkMuted,
					style = Glyphs.footer,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				Text("刷新", color = LazerColors.Brand, style = Glyphs.refresh)
			}
		}
	}
}

@Composable
private fun WideRow(row: SessionRow) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(row.startTime, color = LazerColors.Ink, style = Glyphs.rowTime, modifier = Modifier.width(34.dp))
		Box(Modifier.width(2.dp).height(14.dp).background(statusColor(row.status), RoundedCornerShape(1.dp)))
		Text(
			text = row.name,
			color = LazerColors.Ink,
			style = Glyphs.rowName,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f).padding(start = 6.dp),
		)
		row.place?.let {
			Text(it, color = LazerColors.InkMuted, style = Glyphs.rowPlace, maxLines = 1)
		}
	}
}

@Composable
private fun LargeRow(row: SessionRow) {
	val accent = statusColor(row.status)
	Row(
		modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(Modifier.width(40.dp)) {
			Text(row.startTime, color = LazerColors.Ink, style = Glyphs.largeTime)
			Text(row.endTime, color = LazerColors.InkMuted, style = Glyphs.largeEnd)
		}
		Box(Modifier.width(3.dp).height(28.dp).background(accent, RoundedCornerShape(2.dp)))
		Column(Modifier.weight(1f).padding(start = 8.dp)) {
			Text(
				text = row.name,
				color = LazerColors.Ink,
				style = Glyphs.largeName,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = listOfNotNull(row.place, "第 ${row.periodLabel} 节".takeIf { row.periodLabel.contains("-") })
					.joinToString(" · "),
				color = LazerColors.InkMuted,
				style = Glyphs.largeDetail,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
		Text(row.statusLabel, color = accent, style = Glyphs.largeStatus)
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
	val week = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
	val weekCompact = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
	val day = TextStyle(fontSize = 11.sp, lineHeight = 16.sp)
	val dayCompact = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
	val rowTime = TextStyle(fontSize = 10.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
	val rowName = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
	val rowPlace = TextStyle(fontSize = 9.sp, lineHeight = 14.sp)
	val largeTime = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
	val largeEnd = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val largeName = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
	val largeDetail = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
	val largeStatus = TextStyle(fontSize = 9.sp, lineHeight = 14.sp)
	val footer = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val refresh = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
	val hintTitle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
	val hintBody = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
}

private fun statusColor(status: CourseStatus): Color = when (status) {
	CourseStatus.ONGOING -> LazerColors.Success
	CourseStatus.SOON -> LazerColors.Warning
	CourseStatus.UPCOMING -> Color(0xFF4A5560)
	CourseStatus.FINISHED -> LazerColors.Finished
}
