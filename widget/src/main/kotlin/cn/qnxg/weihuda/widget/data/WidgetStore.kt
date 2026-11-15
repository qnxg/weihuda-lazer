package cn.qnxg.weihuda.widget.data

import android.content.Context
import cn.qnxg.weihuda.core.domain.SnapshotCodec
import cn.qnxg.weihuda.core.domain.WidgetSnapshot

/**
 * 小组件快照的唯一落盘位置。
 *
 * 小组件进程只读这里，刷新由 [cn.qnxg.weihuda.widget.refresh.RefreshWorker] 写入，
 * 避免桌面在等网络时出现白屏。
 */
object WidgetStore {
    private const val PREFS = "weihuda_widget"
    private const val KEY_SNAPSHOT = "snapshot"

    fun read(context: Context): WidgetSnapshot? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return SnapshotCodec.decode(prefs.getString(KEY_SNAPSHOT, null))
    }

    fun write(context: Context, snapshot: WidgetSnapshot) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SNAPSHOT, SnapshotCodec.encode(snapshot))
            .apply()
    }
}
