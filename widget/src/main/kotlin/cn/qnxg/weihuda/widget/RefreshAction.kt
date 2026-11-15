package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh

/** 点小组件里的「刷新」。 */
class RefreshAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters,
	) {
		WidgetRefresh.enqueueNow(context)
	}
}
