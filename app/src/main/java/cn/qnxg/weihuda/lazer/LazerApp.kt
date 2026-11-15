package cn.qnxg.weihuda.lazer

import android.app.Application
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh

class LazerApp : Application() {
	override fun onCreate() {
		super.onCreate()
		// 应用启动时保证周期刷新任务存在，避免只装 App 没加小组件时漏建任务
		WidgetRefresh.ensurePeriodic(this)
		WidgetRefresh.enqueueNow(this)
	}
}
