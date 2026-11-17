// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.qnxg.weihuda.core.entity.WidgetId

/**
 * 一键添加的回执。
 *
 * 这个广播只在桌面真的放下实例后发出，且没有失败回调；桌面静默拒绝时不会有任何广播。
 * 所以「收到即成功」——MIUI 桌面发这个广播时不带 `EXTRA_APPWIDGET_ID`，不能拿它判成败，
 * 卡片种类由发起请求时写入的 extra 决定。
 *
 * 系统按类名反射创建它，不能是 `internal`。
 */
class PinResultReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val id = intent.getStringExtra(WidgetLibrary.EXTRA_PINNED_WIDGET) ?: WidgetId.TodayCourses.value
		WidgetLibrary.of(context).onPinned(WidgetId(id))
	}
}
