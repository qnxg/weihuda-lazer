package cn.qnxg.weihuda.lazer.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.Screen
import cn.qnxg.weihuda.lazer.ui.components.HintText
import cn.qnxg.weihuda.lazer.ui.components.SectionCard
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.core.entity.Settings

/**
 * 小组件库：应用的第一屏，所有卡片的入口。
 *
 * 每个已实现的卡片占一行，点进去是它的设置页；还没做的卡片也留下来，
 * 让「以后会有什么」在界面上可见，而不是只在路线图里。
 */
@Composable
fun WidgetLibraryScreen(
	settings: Settings?,
	snapshot: TodayCourses?,
	notice: Notice?,
	onOpenCourseSettings: () -> Unit,
	onOpenAccount: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		Header()
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			AccountRow(settings = settings, onOpenAccount = onOpenAccount)

			Text("我的小组件", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LazerColors.InkMuted)

			SectionCard(
				title = "今日课程",
				subtitle = courseStatus(settings, snapshot),
				onClick = onOpenCourseSettings,
			) {
				HintText(
					"2×2 / 4×2 / 4×3 / 4×4 四种尺寸；每 15 分钟后台刷新，" +
						"失败时保留当天上一次成功的数据。",
				)
			}

			Text("即将支持", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LazerColors.InkMuted)

			SectionCard(title = "考试安排", subtitle = "规划中") {
				HintText("按考试时间与地点排序的桌面卡片。")
			}
			SectionCard(title = "校园卡与电费", subtitle = "规划中") {
				HintText("余额与用量摘要，只读展示，详情留在应用内。")
			}

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}

			HintText(
				"小组件只做只读摘要：详情、设置与登录全部在应用内完成，" +
					"所以小尺寸卡片永远有内容，也不会把令牌交给桌面渲染进程。",
			)
		}
	}
}

@Composable
private fun Header() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(Brush.verticalGradient(listOf(LazerColors.Brand, LazerColors.BrandDark)))
			.statusBarsPadding()
			.padding(horizontal = 20.dp, vertical = 22.dp),
	) {
		Column {
			Text("微生活小组件", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
			Spacer(Modifier.height(4.dp))
			Text(
				text = "湖南大学校园生活桌面卡片 · Android",
				color = Color.White.copy(alpha = 0.85f),
				fontSize = 13.sp,
			)
		}
	}
}

@Composable
private fun AccountRow(settings: Settings?, onOpenAccount: () -> Unit) {
	SectionCard(
		title = accountTitle(settings),
		subtitle = accountSubtitle(settings),
		onClick = onOpenAccount,
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			HintText(
				if (settings?.credentials?.isPresent == true) {
					"令牌由 Android Keystore 加密保存在本机，不会上传到小组件进程之外。"
				} else {
					"点按查看登录方式：后端目前只提供微信小程序登录，Android 端需要粘贴令牌。"
				},
			)
		}
	}
}

private fun accountTitle(settings: Settings?): String = when {
	settings == null -> "账号"
	settings.credentials.isPresent -> "已登录"
	else -> "未登录"
}

private fun accountSubtitle(settings: Settings?): String = when {
	settings == null -> "读取中…"
	settings.credentials.isPresent -> "令牌已保存，可读取真实数据"
	else -> "当前展示演示数据"
}

private fun courseStatus(settings: Settings?, snapshot: TodayCourses?): String = when {
	snapshot == null -> "等待首次刷新"
	snapshot.failure != null -> "上次刷新失败：${snapshot.failure}"
	snapshot.isDemo -> "演示数据 · 今天 ${snapshot.sessions.size} 节"
	settings?.credentials?.isPresent != true -> "今天 ${snapshot.sessions.size} 节"
	else -> "实时数据 · 今天 ${snapshot.sessions.size} 节"
}
