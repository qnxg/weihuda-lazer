package cn.qnxg.weihuda.lazer.ui.account

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
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.course.SubHeader
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 账号页：登录状态与令牌。
 *
 * 现在只能粘贴令牌，因为后端只发微信小程序的 jscode 登录（见 docs/roadmap.md）。
 * 等后端加上账号密码或扫码下发 JWT，替换的只有这一屏——用例、快照和小组件都不用动，
 * 所以这里不预先造一个假的登录流程。
 */
@Composable
fun AccountScreen(
	settings: Settings?,
	notice: Notice?,
	busy: Boolean,
	onSaveToken: (String) -> Unit,
	onSignOut: () -> Unit,
	onRefresh: () -> Unit,
) {
	val current = settings ?: return
	var token by remember { mutableStateOf("") }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		SubHeader(
			title = "账号",
			subtitle = if (current.credentials.isPresent) "已登录" else "未登录",
		)
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			SectionCard(title = "登录方式") {
				HintText(
					"后端目前只提供微信小程序 jscode 登录，Android 拿不到 code。" +
						"请先在已登录的小程序侧取得 JWT，再粘贴到下面。",
				)
				HintText("令牌通过明文 HTTP 头发送（后端要求裸 JWT，没有 Bearer 前缀），传输始终在 HTTPS 上。")
			}

			SectionCard(title = "访问令牌") {
				OutlinedTextField(
					value = token,
					onValueChange = { token = it },
					label = { Text(if (current.credentials.isPresent) "替换令牌（留空则不修改）" else "粘贴 JWT") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
				)
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					Button(
						onClick = {
							onSaveToken(token)
							token = ""
						},
						enabled = !busy,
						modifier = Modifier.weight(1f),
					) {
						Text("保存并刷新")
					}
					OutlinedButton(
						onClick = onSignOut,
						enabled = current.credentials.isPresent && !busy,
						modifier = Modifier.weight(1f),
					) {
						Text("退出登录")
					}
				}
				HintText("保存后令牌会用 Android Keystore 的 AES-256-GCM 加密再落盘，应用已关闭 allowBackup。")
			}

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}

			SectionCard(title = "令牌过期了？") {
				HintText("JWT 目前不可续期也不可撤销，过期后需要在小程序侧重新登上并再次粘贴。")
				Row(verticalAlignment = Alignment.CenterVertically) {
					OutlinedButton(onClick = onRefresh, enabled = !busy) {
						Text("用当前令牌重试一次")
					}
				}
			}
		}
	}
}

/** 账号页保存令牌时保留其余设置，只换凭证。 */
internal fun Settings.withToken(token: String): Settings =
	copy(credentials = Credentials(token.trim().ifBlank { credentials.token }))
