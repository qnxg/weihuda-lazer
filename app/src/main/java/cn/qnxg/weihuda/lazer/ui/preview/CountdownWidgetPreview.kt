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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.lazer.R
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetPresenter
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetView
import java.time.LocalDate

/**
 * 倒计时卡片的应用内预览。
 *
 * 桌面只有 4×1 一种形态，所以这里没有尺寸参数。文案来自同一个 [CountdownWidgetPresenter]，
 * 颜色来自同一份 `WidgetPalette`，天数按 [today] 现算——与桌面看到的一致。
 */
@Composable
fun CountdownWidgetPreview(
	snapshot: SemesterCountdown?,
	today: LocalDate = LocalDate.now(CampusClock.clock()),
) {
	val view = CountdownWidgetPresenter.present(snapshot, today)
	val colors = PreviewColors(isSystemInDarkTheme())

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = colors.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.width(320.dp)
			.height(100.dp),
	) {
		if (view.hasData) {
			Body(view = view, colors = colors)
		} else {
			EmptyBody(view = view, colors = colors)
		}
	}
}

@Composable
private fun Body(view: CountdownWidgetView, colors: PreviewColors) {
	Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
		Row(
			modifier = Modifier
				.weight(1f)
				.fillMaxHeight()
				.padding(start = 16.dp, end = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.background(colors.brandSoft, RoundedCornerShape(12.dp)),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					painter = painterResource(R.drawable.ic_book),
					contentDescription = null,
					tint = colors.brand,
					modifier = Modifier.size(22.dp),
				)
			}
			Spacer(Modifier.width(12.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(view.eyebrow, color = colors.inkMuted, style = CountdownGlyphs.eyebrow, maxLines = 1)
				Spacer(Modifier.height(3.dp))
				Text(
					text = view.title,
					color = colors.ink,
					style = CountdownGlyphs.title,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				view.detail?.let {
					Spacer(Modifier.height(6.dp))
					Row(verticalAlignment = Alignment.CenterVertically) {
						Icon(
							painter = painterResource(R.drawable.ic_calendar_range),
							contentDescription = null,
							tint = colors.inkMuted,
							modifier = Modifier.size(12.dp),
						)
						Spacer(Modifier.width(4.dp))
						Text(it, color = colors.inkMuted, style = CountdownGlyphs.detail, maxLines = 1)
					}
				}
				view.failureLabel?.let {
					Spacer(Modifier.height(2.dp))
					Text(it, color = colors.error, style = CountdownGlyphs.failure, maxLines = 1)
				}
			}
		}

		Box(
			modifier = Modifier
				.width(96.dp)
				.fillMaxHeight()
				.padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.background(colors.brandSoft, RoundedCornerShape(14.dp)),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				view.metricLabel?.let {
					Text(it, color = colors.inkMuted, style = CountdownGlyphs.metricLabel, maxLines = 1)
					Spacer(Modifier.height(2.dp))
				}
				Row(verticalAlignment = Alignment.Bottom) {
					Text(view.metricValue, color = colors.brand, style = CountdownGlyphs.metric)
					view.metricUnit?.let {
						Spacer(Modifier.width(2.dp))
						Text(it, color = colors.brand, style = CountdownGlyphs.metricUnit, maxLines = 1)
					}
				}
			}
		}
	}
}

@Composable
private fun EmptyBody(view: CountdownWidgetView, colors: PreviewColors) {
	Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(view.emptyTitle.orEmpty(), color = colors.ink, style = CountdownGlyphs.emptyTitle, maxLines = 1)
			view.emptyDetail?.let {
				Text(
					text = it,
					color = colors.inkMuted,
					style = CountdownGlyphs.emptyDetail,
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
 * 显式写 `lineHeight`：Compose 默认的 24sp 行高会让预览比 RemoteViews 里同字号的文字更高，
 * 多出来的部分在固定高度的卡片里会被裁掉。
 */
private object CountdownGlyphs {
	val eyebrow = TextStyle(fontSize = 11.sp, lineHeight = 15.sp)
	val title = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
	val detail = TextStyle(fontSize = 11.sp, lineHeight = 15.sp)
	val failure = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val metricLabel = TextStyle(fontSize = 11.sp, lineHeight = 15.sp)
	val metric = TextStyle(fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
	val metricUnit = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
	val emptyTitle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
	val emptyDetail = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
}
