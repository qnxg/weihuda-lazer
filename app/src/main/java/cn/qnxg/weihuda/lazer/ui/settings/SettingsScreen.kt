package cn.qnxg.weihuda.lazer.ui.settings

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.lazer.BuildConfig
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.components.SubHeader
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 应用设置：账号状态、后端地址与版本，跨所有小组件共用。
 *
 * 登录表单在单独的登录页，这里只显示登录与否并给出入口——设置页要能随时「保存并刷新」，
 * 不该顺手把令牌改掉。「今日课程」这类单个卡片的选项留在各自的设置页。
 */
@Composable
fun SettingsScreen(
	settings: Settings?,
	notice: Notice?,
	busy: Boolean,
	defaultBaseUrl: String,
	onSave: (Settings) -> Unit,
	onOpenLogin: () -> Unit,
	onSignOut: () -> Unit,
) {
	val initial = settings ?: return
	val signedIn = initial.credentials.isPresent
	var baseUrl by remember(initial.baseUrl) { mutableStateOf(initial.baseUrl) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		SubHeader(title = "设置", subtitle = if (signedIn) "已登录" else "未登录")
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			SectionCard(title = "账号") {
				if (signedIn) {
					HintText("已登录，令牌用 Keystore 加密后存在本机。")
				} else {
					HintText("未登录：所有组件都拿不到数据。")
					Button(onClick = onOpenLogin, modifier = Modifier.fillMaxWidth()) {
						Text("去登录")
					}
				}
			}

			SectionCard(title = "后端") {
				OutlinedTextField(
					value = baseUrl,
					onValueChange = { baseUrl = it },
					label = { Text("后端地址") },
					placeholder = { Text(defaultBaseUrl) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
				)
				HintText("留空则使用 $defaultBaseUrl。")
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Button(
					onClick = { onSave(initial.copy(baseUrl = baseUrl.trim())) },
					enabled = !busy,
					modifier = Modifier.weight(1f),
				) {
					Text(if (busy) "刷新中…" else "保存并刷新")
				}
				OutlinedButton(
					onClick = onSignOut,
					enabled = signedIn && !busy,
					modifier = Modifier.weight(1f),
				) {
					Text("退出登录")
				}
			}

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}

			SectionCard(title = "关于") {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text("版本", fontSize = 14.sp, color = LazerColors.Ink, modifier = Modifier.weight(1f))
					HintText(BuildConfig.VERSION_NAME)
				}
			}
		}
	}
}
