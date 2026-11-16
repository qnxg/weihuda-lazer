package cn.qnxg.weihuda.widget.refresh

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.qnxg.weihuda.widget.CourseWidgetComponent

/**
 * 刷新作业：交给组件门面取一次数据并重建桌面。
 *
 * 失败已经在用例里消化成快照上的错误信息，所以这里始终返回 success ——
 * 交给 WorkManager 重试只会在令牌过期时变成一串无意义的请求。
 */
class RefreshWorker(
	appContext: Context,
	params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

	override suspend fun doWork(): Result {
		CourseWidgetComponent.of(applicationContext).refresh()
		return Result.success()
	}
}
