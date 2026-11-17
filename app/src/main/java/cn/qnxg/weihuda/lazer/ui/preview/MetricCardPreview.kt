// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.widget.presentation.MetricCardView
import cn.qnxg.weihuda.widget.presentation.drawableRes

/**
 * 单值卡片（校园卡余额 / 宿舍电量）的应用内预览。
 *
 * 文案来自桌面上同一个 presenter，图标与颜色也取自 `widget` 的那一份，所以预览与桌面不会分叉。
 * 尺寸固定为 2×2 的 110dp：这两张卡片没有别的形态。
 */
@Composable
fun MetricCardPreview(view: MetricCardView) {
	val colors = PreviewColors(isSystemInDarkTheme())

	Card(
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = colors.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier.size(110.dp),
	) {
		Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
			Icon(
				painter = painterResource(view.icon.drawableRes()),
				contentDescription = null,
				tint = colors.brand,
				modifier = Modifier.size(22.dp),
			)

			view.failureLabel?.let {
				Text(
					text = it,
					color = colors.error,
					style = MetricGlyphs.failure,
					modifier = Modifier.padding(top = 4.dp),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}

			Spacer(Modifier.weight(1f))

			if (view.hasData) {
				Text(view.label, color = colors.inkMuted, style = MetricGlyphs.label, maxLines = 1)
				Spacer(Modifier.height(3.dp))
				Text(view.value, color = colors.ink, style = MetricGlyphs.value, maxLines = 1)
			} else {
				Text(
					text = view.emptyTitle.orEmpty(),
					color = colors.ink,
					style = MetricGlyphs.emptyTitle,
					maxLines = 1,
				)
				view.emptyDetail?.let {
					Spacer(Modifier.height(4.dp))
					Text(
						text = it,
						color = colors.inkMuted,
						style = MetricGlyphs.emptyDetail,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
					)
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
	val label = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
	val value = TextStyle(fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
	val failure = TextStyle(fontSize = 9.sp, lineHeight = 12.sp)
	val emptyTitle = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
	val emptyDetail = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
}
