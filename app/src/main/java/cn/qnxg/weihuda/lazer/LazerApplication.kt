// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer

import android.app.Application
import cn.qnxg.weihuda.widget.WidgetLibrary

/**
 * 进程的 Main 组件：只做装配与排期，不做业务。
 *
 * 应用启动时保证课表的刷新任务存在，避免「只装了 App 没加小组件」时漏建任务；
 * 其余卡片按桌面实例数决定是否刷新，不给没添加的卡片白耗请求。
 */
class LazerApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		val library = WidgetLibrary.of(this)
		// 课表在装好应用时就先取一次，免得第一次加卡片看到空态。
		library.courseWidget.apply {
			ensureScheduled()
			requestRefresh()
		}
		// 其余卡片只在桌面上真的存在时才刷新：不该为没添加的卡片白耗每一次网络请求。
		library.networkWidget.run {
			if (pinnedWidgetCount() > 0) {
				ensureScheduled()
				requestRefresh()
			}
		}
		library.campusCardWidget.run {
			if (pinnedWidgetCount() > 0) {
				ensureScheduled()
				requestRefresh()
			}
		}
		library.electricityWidget.run {
			if (pinnedWidgetCount() > 0) {
				ensureScheduled()
				requestRefresh()
			}
		}
	}
}
