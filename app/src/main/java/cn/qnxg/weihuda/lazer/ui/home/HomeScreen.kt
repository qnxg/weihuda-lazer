// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.lazer.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.lazer.R
import cn.qnxg.weihuda.lazer.ui.Notice
import cn.qnxg.weihuda.lazer.ui.login.SignedOutAvatar
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 首页：顶部是使用者，下面按主题排列已实现与规划中的卡片。
 *
 * 规划中的卡片留着并置灰，让「以后会有什么」在界面上可见；它们不可点，
 * 免得点进去发现是空页面。
 */
@Composable
fun HomeScreen(
	settings: Settings?,
	snapshot: TodayCourses?,
	notice: Notice?,
	onOpenLogin: () -> Unit,
	onOpenCourseSettings: () -> Unit,
	onOpenSettings: () -> Unit,
) {
	val signedIn = settings?.credentials?.isPresent == true
	val groups = listOf(
		TileGroup(
			title = "课程与考试",
			tiles = listOf(
				Tile(
					icon = R.drawable.ic_calendar,
					title = "今日课程",
					meta = courseStatus(signedIn, snapshot),
					onClick = onOpenCourseSettings,
				),
				Tile(icon = R.drawable.ic_clipboard, title = "考试安排"),
			),
		),
		TileGroup(
			title = "校园生活",
			tiles = listOf(
				Tile(icon = R.drawable.ic_card, title = "校园卡"),
				Tile(icon = R.drawable.ic_bolt, title = "宿舍电量"),
				Tile(icon = R.drawable.ic_wifi, title = "校园网"),
			),
		),
		TileGroup(
			title = "体测",
			tiles = listOf(
				Tile(icon = R.drawable.ic_dumbbell, title = "体测成绩"),
				Tile(icon = R.drawable.ic_calendar_check, title = "体测预约"),
			),
		),
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.verticalScroll(rememberScrollState()),
	) {
		HomeHeader(
			signedIn = signedIn,
			snapshot = snapshot,
			onOpenLogin = onOpenLogin,
			onOpenSettings = onOpenSettings,
		)

		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp),
		) {
			groups.forEach { group -> TileGroupView(group) }

			notice?.let {
				Text(
					text = it.text,
					fontSize = 12.sp,
					color = if (it.isError) LazerColors.Danger else LazerColors.Success,
				)
			}
		}
	}
}

@Composable
private fun HomeHeader(
	signedIn: Boolean,
	snapshot: TodayCourses?,
	onOpenLogin: () -> Unit,
	onOpenSettings: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Row(
			modifier = Modifier
				.weight(1f)
				.then(
					if (signedIn) {
						Modifier
					} else {
						// 未登录时整块头像与文字都是登录入口，比只让一个小按钮可点好找。
						Modifier
							.clip(RoundedCornerShape(12.dp))
							.clickable(onClick = onOpenLogin)
					},
				),
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (signedIn) {
				val name = snapshot?.studentName?.takeIf { it.isNotBlank() } ?: "微生活"
				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(CircleShape)
						.background(LazerColors.Brand),
					contentAlignment = Alignment.Center,
				) {
					Text(name.take(1), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
				}
				Spacer(Modifier.width(12.dp))
				Column {
					Text(
						text = name,
						fontSize = 18.sp,
						fontWeight = FontWeight.SemiBold,
						color = LazerColors.Ink,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					snapshot?.studentCollege?.takeIf { it.isNotBlank() }?.let {
						Text(
							text = it,
							fontSize = 13.sp,
							color = LazerColors.InkMuted,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
					}
				}
			} else {
				SignedOutAvatar()
				Spacer(Modifier.width(12.dp))
				Column {
					Text(
						text = "未登录",
						fontSize = 18.sp,
						fontWeight = FontWeight.SemiBold,
						color = LazerColors.Ink,
					)
					Text(
						text = "登录校园账号以使用所有组件",
						fontSize = 13.sp,
						color = LazerColors.InkMuted,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
		IconButton(onClick = onOpenSettings) {
			Icon(
				painter = painterResource(R.drawable.ic_settings),
				contentDescription = "设置",
				tint = LazerColors.Ink,
				modifier = Modifier.size(22.dp),
			)
		}
	}
}

@Composable
private fun TileGroupView(group: TileGroup) {
	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		Text(group.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LazerColors.Ink)
		group.tiles.chunked(2).forEach { row ->
			Row(
				modifier = Modifier.height(IntrinsicSize.Min),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				row.forEach { tile -> TileView(tile, Modifier.weight(1f).fillMaxHeight()) }
				if (row.size == 1) Spacer(Modifier.weight(1f))
			}
		}
	}
}

@Composable
private fun TileView(tile: Tile, modifier: Modifier = Modifier) {
	val enabled = tile.onClick != null
	Column(
		modifier = modifier
			.clip(RoundedCornerShape(14.dp))
			.background(Color.White)
			.then(if (tile.onClick != null) Modifier.clickable(onClick = tile.onClick) else Modifier)
			.padding(14.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Icon(
			painter = painterResource(tile.icon),
			contentDescription = null,
			tint = if (enabled) LazerColors.Brand else LazerColors.Finished,
			modifier = Modifier
				.size(22.dp)
				.padding(bottom = 4.dp),
		)
		Text(
			text = tile.title,
			fontSize = 14.sp,
			fontWeight = FontWeight.Medium,
			color = if (enabled) LazerColors.Ink else LazerColors.InkMuted,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			lineHeight = 18.sp,
		)
		Text(
			text = tile.meta,
			fontSize = 12.sp,
			color = LazerColors.InkMuted,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			lineHeight = 16.sp,
		)
	}
}

/** 一张卡片：已实现的有动作，规划中的没有动作，只能看。 */
private data class Tile(
	@DrawableRes val icon: Int,
	val title: String,
	val meta: String = "规划中",
	val onClick: (() -> Unit)? = null,
)

private data class TileGroup(val title: String, val tiles: List<Tile>)

private fun courseStatus(signedIn: Boolean, snapshot: TodayCourses?): String = when {
	// 没登录时快照可能压根没写过，说「正在获取」会让人以为在转圈等数据。
	!signedIn -> "未登录"
	snapshot == null -> "正在获取"
	snapshot.state == TodayCourses.State.SIGNED_OUT -> "未登录"
	snapshot.state == TodayCourses.State.FAILED -> "刷新失败"
	snapshot.failure != null -> "上次刷新失败"
	else -> "今天 ${snapshot.sessions.size} 节"
}
