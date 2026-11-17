// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.network

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
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.components.SubHeader
import cn.qnxg.weihuda.lazer.ui.preview.NetworkWidgetPreview
import cn.qnxg.weihuda.lazer.ui.preview.PreviewMode
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 「校园网流量」组件的设置页：预览、添加到桌面与手动刷新。
 *
 * 数据来源与令牌是所有卡片共用的，放在应用设置里，见 `ui/settings`。
 */
@Composable
fun NetworkSettingsScreen(
	snapshot: NetworkTraffic?,
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
		SubHeader(title = "校园网流量", subtitle = "在桌面查看本月已用流量、账号状态与欠费")
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
				HintText("或长按桌面空白处，选择「校园网流量」小组件")
			}

			SectionCard(title = "预览", subtitle = "2×2 / 4×2 / 4×4") {
				NetworkWidgetPreview(snapshot = snapshot, mode = PreviewMode.WIDE)
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					NetworkWidgetPreview(snapshot = snapshot, mode = PreviewMode.COMPACT)
					NetworkWidgetPreview(snapshot = snapshot, mode = PreviewMode.LARGE)
				}
			}
		}
	}
}
