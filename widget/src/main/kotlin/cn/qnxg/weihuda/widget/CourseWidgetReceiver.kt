package cn.qnxg.weihuda.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import cn.qnxg.weihuda.widget.refresh.WidgetRefresh

/**
 * 小组件的广播入口。
 *
 * 系统每次要求更新时先渲染旧快照（保证立刻有内容），再触发一次后台刷新。
 */
class CourseWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CourseWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefresh.ensurePeriodic(context)
        WidgetRefresh.enqueueNow(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefresh.ensurePeriodic(context)
        WidgetRefresh.enqueueNow(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefresh.cancelAll(context)
    }
}
