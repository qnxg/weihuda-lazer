package cn.qnxg.weihuda.lazer.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.domain.CourseStatus
import cn.qnxg.weihuda.core.domain.SessionSnapshot
import cn.qnxg.weihuda.core.domain.WidgetSnapshot
import cn.qnxg.weihuda.core.domain.isPlausibleWeek
import cn.qnxg.weihuda.core.domain.monthDayLabel
import cn.qnxg.weihuda.core.domain.weekdayLabel

enum class PreviewMode { COMPACT, WIDE, LARGE }

/**
 * 应用内的近似预览。
 *
 * 小组件真正的渲染发生在 RemoteViews 里，无法在应用内复用，这里用同一份快照和
 * 同样的三种密度画一遍，方便检查数据与排版。
 */
@Composable
fun WidgetMock(
	snapshot: WidgetSnapshot?,
	widthDp: Int,
	heightDp: Int,
	mode: PreviewMode,
) {
	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		modifier = Modifier.width(widthDp.dp).height(heightDp.dp),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 13.dp, vertical = 8.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = weekLabel(snapshot),
					color = Brand,
					fontSize = if (mode == PreviewMode.COMPACT) 11.sp else 12.sp,
					fontWeight = FontWeight.Bold,
				)
				Spacer(Modifier.weight(1f))
				Text(
					text = dayLabel(snapshot, mode),
					color = InkMuted,
					fontSize = if (mode == PreviewMode.COMPACT) 10.sp else 11.sp,
				)
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 3.dp)
					.height(1.dp)
					.background(Line),
			)

			val sessions = snapshot?.sessions.orEmpty()
			if (sessions.isEmpty()) {
				Box(
					modifier = Modifier.fillMaxWidth().weight(1f),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = snapshot?.error ?: "今日无课",
						color = if (snapshot?.error != null) Danger else Ink,
						fontSize = 13.sp,
					)
				}
			} else {
				Column(modifier = Modifier.weight(1f)) {
					if (mode == PreviewMode.COMPACT) {
						CompactPreview(sessions)
					} else {
						sessions.take(if (mode == PreviewMode.LARGE) 5 else 2).forEach { session ->
							PreviewRow(session, mode)
						}
					}
				}
			}

			if (mode != PreviewMode.COMPACT) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = snapshot?.let {
							(if (it.isDemo) "演示数据" else "实时数据") +
								it.generatedAtLabel().let { time -> if (time.isEmpty()) "" else " · $time" }
						} ?: "等待首次刷新",
						color = InkMuted,
						fontSize = 9.sp,
					)
					Spacer(Modifier.weight(1f))
					Text("刷新", color = Brand, fontSize = 10.sp, fontWeight = FontWeight.Medium)
				}
			}
		}
	}
}

@Composable
private fun PreviewRow(session: SessionSnapshot, mode: PreviewMode) {
	val accent = statusColor(session.statusEnum)
	when (mode) {
		PreviewMode.COMPACT -> Unit

		PreviewMode.WIDE -> Row(
			modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = session.startTime,
				modifier = Modifier.width(34.dp),
				color = Ink,
				fontSize = 10.sp,
				fontWeight = FontWeight.Medium,
			)
			Box(Modifier.width(2.dp).height(14.dp).background(accent, RoundedCornerShape(1.dp)))
			Text(
				text = session.name,
				modifier = Modifier.weight(1f).padding(start = 6.dp),
				color = Ink,
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			session.place?.takeIf { it.isNotBlank() }?.let {
				Text(it, color = InkMuted, fontSize = 9.sp, maxLines = 1)
			}
		}

		PreviewMode.LARGE -> Row(
			modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(Modifier.width(40.dp)) {
				Text(session.startTime, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
				Text(session.endTime, color = InkMuted, fontSize = 9.sp)
			}
			Box(Modifier.width(3.dp).height(28.dp).background(accent, RoundedCornerShape(2.dp)))
			Column(Modifier.weight(1f).padding(start = 8.dp)) {
				Text(
					text = session.name,
					color = Ink,
					fontSize = 13.sp,
					fontWeight = FontWeight.Medium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = listOfNotNull(
						session.place?.takeIf { it.isNotBlank() },
						"第 ${session.periodLabel} 节".takeIf { session.startPeriod != session.endPeriod },
					).joinToString(" · "),
					color = InkMuted,
					fontSize = 10.sp,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
			Text(session.statusEnum.label, color = accent, fontSize = 9.sp)
		}
	}
}

@Composable
private fun CompactPreview(sessions: List<SessionSnapshot>) {
	val primary = sessions.firstOrNull { it.statusEnum == CourseStatus.ONGOING }
		?: sessions.firstOrNull { it.statusEnum != CourseStatus.FINISHED }
		?: sessions.first()
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			Modifier
				.width(3.dp)
				.height(38.dp)
				.background(statusColor(primary.statusEnum), RoundedCornerShape(2.dp)),
		)
		Column(Modifier.padding(start = 7.dp)) {
			Text(
				text = "${primary.startTime} - ${primary.endTime}",
				color = InkMuted,
				fontSize = 9.sp,
			)
			Text(
				text = primary.name,
				color = Ink,
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = listOfNotNull(
					primary.place?.takeIf { it.isNotBlank() },
					primary.statusEnum.label.takeIf { it != CourseStatus.UPCOMING.label },
				).joinToString(" · "),
				color = InkMuted,
				fontSize = 9.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
	if (sessions.size > 1) {
		Text(
			text = "今日共 ${sessions.size} 节",
			modifier = Modifier.padding(top = 4.dp),
			color = InkMuted,
			fontSize = 9.sp,
		)
	}
}

private fun weekLabel(snapshot: WidgetSnapshot?): String {
	snapshot ?: return "微生活课表"
	return if (isPlausibleWeek(snapshot.week)) "第 ${snapshot.week} 周" else "假期"
}

private fun dayLabel(snapshot: WidgetSnapshot?, mode: PreviewMode): String {
	snapshot ?: return ""
	val weekday = weekdayLabel(snapshot.weekday)
	if (mode == PreviewMode.COMPACT) return weekday
	val date = snapshot.localDate ?: return weekday
	return "$weekday ${monthDayLabel(date)}"
}

private fun statusColor(status: CourseStatus): Color = when (status) {
	CourseStatus.ONGOING -> Success
	CourseStatus.SOON -> Warning
	CourseStatus.UPCOMING -> Color(0xFF4A5560)
	CourseStatus.FINISHED -> Color(0xFF9AA5AF)
}
