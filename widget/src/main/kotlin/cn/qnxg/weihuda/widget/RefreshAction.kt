package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * 点小组件里的「刷新」：只排一次后台任务，不在这里发请求。
 *
 * 小组件的点击回调有执行时限，直接联网会在信号差的时候杀掉自己；
 * 排一个去重的一次性 Worker，界面立刻返回，结果由 Worker 写快照并重建。
 */
class RefreshAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters,
	) {
		CourseWidgetComponent.of(context).requestRefresh()
	}
}
