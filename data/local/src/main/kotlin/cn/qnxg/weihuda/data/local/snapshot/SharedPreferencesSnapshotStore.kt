package cn.qnxg.weihuda.data.local.snapshot

import android.content.Context
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.usecase.TodayCourses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 用 SharedPreferences 存快照：一份 JSON 字符串，按 [WidgetId] 分键。
 *
 * 快照很小（一屏课表），SharedPreferences 的读取速度正好对上「小组件要立刻有内容」的要求；
 * 换成 DataStore 或数据库只影响这个类，端口和调用方都不用动。
 */
class SharedPreferencesSnapshotStore(context: Context) : SnapshotStore {

	private val prefs = context.applicationContext
		.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	override suspend fun readTodayCourses(id: WidgetId): TodayCourses? =
		withContext(Dispatchers.IO) {
			SnapshotJson.decode(prefs.getString(key(id), null))
		}

	override suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses) {
		withContext(Dispatchers.IO) {
			// 提交而不是 apply：Worker 结束后进程可能被回收，丢一次快照桌面就空一次
			prefs.edit().putString(key(id), SnapshotJson.encode(snapshot)).commit()
		}
	}

	private fun key(id: WidgetId): String = "snapshot_${id.value}"

	private companion object {
		const val PREFS = "weihuda_widget"
	}
}
