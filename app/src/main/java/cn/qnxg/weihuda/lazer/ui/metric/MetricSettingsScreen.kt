// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.metric

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
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.components.SubHeader
import cn.qnxg.weihuda.lazer.ui.preview.MetricCardPreview
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors
import cn.qnxg.weihuda.widget.presentation.CardBalancePresenter
import cn.qnxg.weihuda.widget.presentation.ElectricityPresenter
import cn.qnxg.weihuda.widget.presentation.MetricCardView

/**
 * 「校园卡余额」组件的设置页：预览、添加到桌面与手动刷新。
 *
 * 数据来源与令牌是所有卡片共用的，放在应用设置里，见 `ui/settings`。
 */
@Composable
fun CampusCardSettingsScreen(
	snapshot: CardBalance?,
	notice: Notice?,
	busy: Boolean,
	pinBlocked: Boolean,
	canOpenAppSettings: Boolean,
	onPin: () -> Unit,
	onRefresh: () -> Unit,
	onOpenAppSettings: () -> Unit,
) = MetricSettingsScreen(
	title = "校园卡",
	subtitle = "在桌面查看校园卡里的余额",
	view = CardBalancePresenter.present(snapshot),
	pinHint = "或长按桌面空白处，选择「校园卡余额」小组件",
	notice = notice,
	busy = busy,
	pinBlocked = pinBlocked,
	canOpenAppSettings = canOpenAppSettings,
	onPin = onPin,
	onRefresh = onRefresh,
	onOpenAppSettings = onOpenAppSettings,
)

/** 「宿舍电量」组件的设置页；与校园卡只差文案与数据来源，所以共用一份布局。 */
@Composable
fun ElectricitySettingsScreen(
	snapshot: ElectricityBalance?,
	notice: Notice?,
	busy: Boolean,
	pinBlocked: Boolean,
	canOpenAppSettings: Boolean,
	onPin: () -> Unit,
	onRefresh: () -> Unit,
	onOpenAppSettings: () -> Unit,
) = MetricSettingsScreen(
	title = "宿舍电量",
	subtitle = "在桌面查看宿舍还剩多少电",
	view = ElectricityPresenter.present(snapshot),
	pinHint = "或长按桌面空白处，选择「宿舍电量」小组件",
	notice = notice,
	busy = busy,
	pinBlocked = pinBlocked,
	canOpenAppSettings = canOpenAppSettings,
	onPin = onPin,
	onRefresh = onRefresh,
	onOpenAppSettings = onOpenAppSettings,
)

@Composable
private fun MetricSettingsScreen(
	title: String,
	subtitle: String,
	view: MetricCardView,
	pinHint: String,
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
		SubHeader(title = title, subtitle = subtitle)
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
				HintText(pinHint)
			}

			SectionCard(title = "预览", subtitle = "2×2") {
				MetricCardPreview(view = view)
			}
		}
	}
}
