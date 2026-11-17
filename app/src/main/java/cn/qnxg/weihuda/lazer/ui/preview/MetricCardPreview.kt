// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
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
import cn.qnxg.weihuda.widget.presentation.MetricCardDensity
import cn.qnxg.weihuda.widget.presentation.MetricCardLayout
import cn.qnxg.weihuda.widget.presentation.MetricCardView
import cn.qnxg.weihuda.widget.presentation.drawableRes

/** 桌面公式（`70×2−30` × `70×1−30`）给出的 2×1 最小尺寸，预览按它画。 */
private val PREVIEW_WIDTH = 110.dp
private val PREVIEW_HEIGHT = 40.dp

/**
 * 单值卡片（校园卡余额 / 宿舍电量）的应用内预览。
 *
 * 文案来自桌面上同一个 presenter，图标与颜色也取自 `widget` 的那一份，尺寸与字号档位同样
 * 走 [MetricCardLayout]，所以预览与桌面不会分叉。
 */
@Composable
fun MetricCardPreview(view: MetricCardView) {
	val colors = PreviewColors(isSystemInDarkTheme())
	val compact = MetricCardLayout.density(PREVIEW_WIDTH.value.toInt()) == MetricCardDensity.COMPACT

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = colors.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier.size(PREVIEW_WIDTH, PREVIEW_HEIGHT),
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = if (compact) 10.dp else 14.dp, end = if (compact) 10.dp else 14.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				painter = painterResource(view.icon.drawableRes()),
				contentDescription = null,
				tint = colors.brand,
				modifier = Modifier.size(if (compact) 18.dp else 22.dp),
			)
			Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
			Column {
				if (view.hasData) {
					Text(
						text = view.failureLabel ?: view.label,
						color = if (view.failureLabel != null) colors.error else colors.inkMuted,
						style = if (compact) MetricGlyphs.captionCompact else MetricGlyphs.caption,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					Text(
						text = view.value,
						color = colors.ink,
						style = if (compact) MetricGlyphs.valueCompact else MetricGlyphs.value,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				} else {
					Text(
						text = view.emptyTitle.orEmpty(),
						color = colors.ink,
						style = if (compact) MetricGlyphs.emptyTitleCompact else MetricGlyphs.emptyTitle,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					view.emptyDetail?.let {
						Text(
							text = it,
							color = colors.inkMuted,
							style = MetricGlyphs.emptyDetail,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
					}
				}
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
private object MetricGlyphs {
	val caption = TextStyle(fontSize = 10.sp, lineHeight = 13.sp)
	val captionCompact = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val value = TextStyle(fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
	val valueCompact = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
	val emptyTitle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
	val emptyTitleCompact = TextStyle(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium)
	val emptyDetail = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
}
