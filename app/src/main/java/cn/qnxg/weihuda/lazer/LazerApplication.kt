// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.lazer

import android.app.Application
import cn.qnxg.weihuda.widget.CourseWidgetComponent

/**
 * 进程的 Main 组件：只做装配与排期，不做业务。
 *
 * 应用启动时保证周期刷新任务存在，避免「只装了 App 没加小组件」时漏建任务；
 * 卡片是否在桌面上不影响这一点，因为用户随时可能添加。
 */
class LazerApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		CourseWidgetComponent.of(this).apply {
			ensureScheduled()
			requestRefresh()
		}
	}
}
