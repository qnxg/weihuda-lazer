// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.lazer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/** 白底卡片：应用里所有内容块都用它，圆角 14dp、无阴影。 */
@Composable
fun SectionCard(
	title: String? = null,
	subtitle: String? = null,
	onClick: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit,
) {
	Card(
		colors = CardDefaults.cardColors(containerColor = Color.White),
		shape = RoundedCornerShape(14.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.fillMaxWidth()
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
	) {
		Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			if (title != null) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Column(modifier = Modifier.weight(1f)) {
						Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LazerColors.Ink)
						if (subtitle != null) {
							Text(subtitle, fontSize = 12.sp, color = LazerColors.InkMuted)
						}
					}
					if (onClick != null) {
						Text("设置", fontSize = 13.sp, color = LazerColors.Brand, fontWeight = FontWeight.Medium)
					}
				}
				Spacer(Modifier.height(2.dp))
			}
			content()
		}
	}
}

/** 次要说明文字，用在卡片底部解释规则。 */
@Composable
fun HintText(text: String, color: Color = LazerColors.InkMuted) {
	Text(text = text, fontSize = 12.sp, color = color)
}
