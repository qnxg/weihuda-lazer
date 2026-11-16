// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.lazer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/** 二级页面的标题栏：品牌色底色，状态栏之下。 */
@Composable
fun SubHeader(title: String, subtitle: String) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(LazerColors.Brand)
			.statusBarsPadding()
			.padding(horizontal = 20.dp, vertical = 18.dp),
	) {
		Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
		Spacer(Modifier.height(4.dp))
		Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
	}
}
