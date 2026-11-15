package cn.qnxg.weihuda.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import cn.qnxg.weihuda.widget.data.WidgetStore

/**
 * 课程小组件。
 *
 * 只负责渲染：数据由 [cn.qnxg.weihuda.widget.refresh.RefreshWorker] 写入
 * [WidgetStore]，写入后调用 `updateAll` 触发这里重新组合。
 */
class CourseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(COMPACT_SIZE, WIDE_SIZE, MEDIUM_SIZE, LARGE_SIZE),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = runCatching { WidgetStore.read(context) }.getOrNull()
        provideContent {
            WidgetContent(context = context, snapshot = snapshot)
        }
    }

    companion object {
        /** 2x2 */
        val COMPACT_SIZE = DpSize(110.dp, 110.dp)

        /** 4x2 */
        val WIDE_SIZE = DpSize(250.dp, 110.dp)

        /** 4x3，多数启动器把 4x2 排得更松，实际高度会落在这里 */
        val MEDIUM_SIZE = DpSize(250.dp, 170.dp)

        /** 4x4 */
        val LARGE_SIZE = DpSize(250.dp, 250.dp)
    }
}
