// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.unit.ColorProvider
import cn.qnxg.weihuda.widget.presentation.WidgetColor

/**
 * 两种尺寸以上的组件共用的桌面尺寸。
 *
 * 与 `CourseWidget` 的响应式尺寸集合相同：启动器按单元格给出 dp，卡片再按真实 dp 决定密度。
 */
internal val COMPACT_SIZE = DpSize(110.dp, 110.dp)

/** 4×2，多数启动器把它排得更松，实际高度会落在 4×3。 */
internal val WIDE_SIZE = DpSize(250.dp, 110.dp)

internal val MEDIUM_SIZE = DpSize(250.dp, 170.dp)

/** 4×4 */
internal val LARGE_SIZE = DpSize(250.dp, 250.dp)

/** 深浅成对的令牌交给 Glance，由系统在 API 31+ 上即时解析。 */
internal fun WidgetColor.provider(): ColorProvider =
	DayNightColorProvider(day = Color(light), night = Color(dark))

/** Glance 1.2 的 Text 没有 overflow 参数，按字符数手动截断。 */
internal fun String.clip(max: Int): String =
	if (length <= max) this else take((max - 1).coerceAtLeast(1)) + "…"

/**
 * 打开本应用。
 *
 * 必须用带 Component 的显式 Intent：Glance 会给 Intent 附加 data URI，
 * 隐式 Intent（只带 package）会因此匹配不到启动 Activity。
 */
internal fun openAppAction(context: Context): Action = actionStartActivity(
	context.packageManager.getLaunchIntentForPackage(context.packageName)
		?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName),
)
