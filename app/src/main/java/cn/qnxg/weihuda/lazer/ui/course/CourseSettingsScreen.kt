// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.course

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.components.SubHeader
import cn.qnxg.weihuda.lazer.ui.preview.CourseWidgetPreview
import cn.qnxg.weihuda.lazer.ui.preview.PreviewMode
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 「今日课程」组件的设置页：预览、添加到桌面与手动刷新。
 *
 * 数据来源与令牌是所有卡片共用的，放在应用设置里，见 `ui/settings`。
 */
@Composable
fun CourseSettingsScreen(
	snapshot: TodayCourses?,
	notice: Notice?,
	busy: Boolean,
	pinBlocked: Boolean,
	canOpenAppSettings: Boolean,
	onPin: () -> Unit,
	onRefresh: () -> Unit,
	onOpenAppSettings: () -> Unit,
	onOpenExactAlarmSettings: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		SubHeader(title = "今日课程", subtitle = "在桌面查看今天的课程、时间与地点")
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
				HintText("或长按桌面空白处，选择「今日课程」小组件")
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				SectionCard(title = "状态刷新") {
					HintText(
						"课程开始、结束和开课前 20 分钟会按点重绘。如果状态没有准点切换，" +
							"检查 Android 12+ 的「闹钟与提醒」权限；没开时会退回近似闹钟，可能晚几分钟。",
					)
					OutlinedButton(onClick = onOpenExactAlarmSettings, modifier = Modifier.fillMaxWidth()) {
						Text("打开闹钟权限")
					}
				}
			}

			SectionCard(title = "预览", subtitle = "2×2 / 4×2 / 4×4") {
				CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.WIDE)
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.COMPACT)
					CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.LARGE)
				}
			}
		}
	}
}
