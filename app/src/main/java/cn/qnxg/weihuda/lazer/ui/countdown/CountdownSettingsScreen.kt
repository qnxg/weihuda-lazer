// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.components.SubHeader
import cn.qnxg.weihuda.lazer.ui.preview.CountdownWidgetPreview
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 「学期倒计时」组件的设置页：预览、添加到桌面与手动刷新。
 *
 * 这张卡片不需要登录（学期信息是公开接口），所以这里不提供登录入口，
 * 后端地址与令牌仍然在应用设置里改。
 */
@Composable
fun CountdownSettingsScreen(
	snapshot: SemesterCountdown?,
	notice: Notice?,
	busy: Boolean,
	pinBlocked: Boolean,
	canOpenAppSettings: Boolean,
	onPin: () -> Unit,
	onRefresh: () -> Unit,
	onOpenAppSettings: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		SubHeader(title = "学期倒计时", subtitle = "在桌面查看距离学期结束（或开学）还有多少天")
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(onClick = onPin, modifier = Modifier.weight(1f)) {
					Text("添加到桌面")
				}
				OutlinedButton(onClick = onRefresh, enabled = !busy, modifier = Modifier.weight(1f)) {
					Text(if (busy) "刷新中…" else "立即刷新")
				}
			}

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}

			if (pinBlocked) {
				if (canOpenAppSettings) {
					OutlinedButton(onClick = onOpenAppSettings) {
						Text("打开应用设置")
					}
				}
				HintText("或长按桌面空白处，选择「学期倒计时」小组件")
			}

			SectionCard(title = "预览", subtitle = "4×1") {
				CountdownWidgetPreview(snapshot = snapshot)
			}

			HintText("学期日期来自校历，不需要登录；天数在每天午夜自动翻页，学期信息每天刷新一次。")
		}
	}
}
