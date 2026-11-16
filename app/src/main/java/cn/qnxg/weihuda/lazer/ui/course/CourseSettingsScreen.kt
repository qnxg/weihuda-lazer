package cn.qnxg.weihuda.lazer.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.preview.CourseWidgetPreview
import cn.qnxg.weihuda.lazer.ui.preview.PreviewMode
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 「今日课程」组件的设置页：数据来源、刷新、添加到桌面、三尺寸预览。
 *
 * 令牌不在这里填写——它是账号级别的东西，放在账号页，免得每个卡片各存一份。
 */
@Composable
fun CourseSettingsScreen(
	settings: Settings?,
	snapshot: TodayCourses?,
	notice: Notice?,
	busy: Boolean,
	defaultBaseUrl: String,
	onSaveAndRefresh: (Settings) -> Unit,
	onPin: () -> Unit,
	onOpenAccount: () -> Unit,
) {
	val initial = settings ?: return
	var demoMode by remember(initial.demoMode) { mutableStateOf(initial.demoMode) }
	var baseUrl by remember(initial.baseUrl) { mutableStateOf(initial.baseUrl) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		SubHeader(
			title = "今日课程",
			subtitle = "在桌面查看今天的课程、时间与地点",
		)
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			SectionCard(title = "数据来源") {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Column(modifier = Modifier.weight(1f)) {
						Text("使用演示数据", fontSize = 14.sp, color = LazerColors.Ink)
						HintText("未配置令牌时展示内置课表，可离线预览")
					}
					Switch(checked = demoMode, onCheckedChange = { demoMode = it })
				}
				OutlinedTextField(
					value = baseUrl,
					onValueChange = { baseUrl = it },
					label = { Text("后端地址") },
					placeholder = { Text(defaultBaseUrl) },
					enabled = !demoMode,
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
				)
				HintText("留空即使用与小程序相同的网关；只在调试自建后端时才需要改。")
				HintText(
					if (initial.credentials.isPresent) {
						"已登录：桌面会展示真实课表"
					} else {
						"未登录：桌面展示演示课表，账号与令牌在账号页设置"
					},
				)
				OutlinedButton(onClick = onOpenAccount, modifier = Modifier.fillMaxWidth()) {
					Text("打开账号与令牌设置")
				}
			}

			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(
					onClick = { onSaveAndRefresh(initial.copy(demoMode = demoMode, baseUrl = baseUrl.trim())) },
					enabled = !busy,
					modifier = Modifier.weight(1f),
				) {
					Text(if (busy) "刷新中…" else "保存并刷新")
				}
				OutlinedButton(onClick = onPin, modifier = Modifier.weight(1f)) {
					Text("添加到桌面")
				}
			}

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}

			SectionCard(title = "小组件预览", subtitle = "4×2 / 2×2 / 4×4 三种尺寸") {
				CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.WIDE)
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.COMPACT)
					CourseWidgetPreview(snapshot = snapshot, mode = PreviewMode.LARGE)
				}
				HintText("预览与桌面共用同一个 presenter，所以这里看到的文案就是桌面上的文案。")
			}

			SectionCard(title = "刷新机制") {
				HintText(
					"· 每 15 分钟由 WorkManager 后台刷新一次（系统允许的最小周期）\n" +
						"· 点小组件里的「刷新」立即拉取；点其余区域打开本应用\n" +
						"· 刷新失败时保留当天上一次成功的课表，只在卡片上提示错误",
				)
			}
		}
	}
}

@Composable
internal fun SubHeader(title: String, subtitle: String) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(LazerColors.Brand)
			.statusBarsPadding()
			.padding(horizontal = 20.dp, vertical = 18.dp),
	) {
		Text(title, color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
		Spacer(Modifier.height(4.dp))
		Text(
			text = subtitle,
			color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
			fontSize = 12.sp,
		)
	}
}
