package cn.qnxg.weihuda.widget.refresh

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.glance.appwidget.updateAll
import androidx.work.WorkerParameters
import cn.qnxg.weihuda.widget.CourseWidget
import cn.qnxg.weihuda.widget.data.SettingsStore
import cn.qnxg.weihuda.widget.data.SnapshotLoader
import cn.qnxg.weihuda.widget.data.WidgetStore

/**
 * 拉取课表 → 写快照 → 重建所有小组件。
 *
 * 失败也在 [SnapshotLoader] 里消化掉（保留当天旧数据 + 错误信息），
 * 因此这里始终返回 success，不触发 WorkManager 的指数退避重试风暴。
 */
class RefreshWorker(
	appContext: Context,
	params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

	override suspend fun doWork(): Result {
		val context = applicationContext
		val settings = SettingsStore(context).load()
		val snapshot = SnapshotLoader.load(context, settings)
		WidgetStore.write(context, snapshot)
		CourseWidget().updateAll(context)
		return Result.success()
	}
}
