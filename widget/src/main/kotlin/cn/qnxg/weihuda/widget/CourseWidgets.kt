package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.updateAll
import cn.qnxg.weihuda.core.domain.WidgetSnapshot
import cn.qnxg.weihuda.widget.data.AppSettings
import cn.qnxg.weihuda.widget.data.SnapshotLoader
import cn.qnxg.weihuda.widget.data.WidgetStore

/**
 * 给宿主应用用的门面：只暴露「刷新」和「添加到桌面」两件事，
 * 不把 Glance 的类型泄漏到 app 模块。
 */
object CourseWidgets {

	/** 按当前设置拉取一次数据，写入快照并重建桌面上的小组件。 */
	suspend fun refresh(context: Context, settings: AppSettings): WidgetSnapshot {
		val snapshot = SnapshotLoader.load(context, settings)
		WidgetStore.write(context, snapshot)
		CourseWidget().updateAll(context)
		return snapshot
	}

	/** 请求把小组件固定到桌面，返回 false 表示当前启动器不支持一键添加。 */
	fun pin(context: Context): Boolean {
		val manager = AppWidgetManager.getInstance(context)
		val provider = ComponentName(context, CourseWidgetReceiver::class.java)
		return manager.isRequestPinAppWidgetSupported &&
			manager.requestPinAppWidget(provider, null, null)
	}
}
