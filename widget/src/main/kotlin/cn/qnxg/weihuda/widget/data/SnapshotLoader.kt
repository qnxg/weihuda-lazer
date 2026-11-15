package cn.qnxg.weihuda.widget.data

import android.content.Context
import cn.qnxg.weihuda.core.api.WeihudaApi
import cn.qnxg.weihuda.core.demo.DemoData
import cn.qnxg.weihuda.core.domain.SnapshotFactory
import cn.qnxg.weihuda.core.domain.WidgetSnapshot
import cn.qnxg.weihuda.core.domain.weekOf
import java.time.LocalDate
import java.time.LocalTime

/**
 * 把「设置 + 后端」变成一份可直接渲染的小组件快照。
 *
 * 演示模式完全离线，用于没有令牌时的安装即用体验；
 * 实时模式读取学期与课表，失败时保留当天上一次的快照。
 */
object SnapshotLoader {

	suspend fun load(context: Context, settings: AppSettings): WidgetSnapshot {
		val zone = WidgetSnapshot.ZONE
		val today = LocalDate.now(zone)
		val now = LocalTime.now(zone).withSecond(0).withNano(0)
		val previous = WidgetStore.read(context)

		if (settings.demoMode || !settings.hasToken) {
			return SnapshotFactory.of(
				courses = DemoData.courses(),
				source = WidgetSnapshot.SOURCE_DEMO,
				week = weekOf(DemoData.semesterStart(today), today),
				date = today,
				now = now,
				userName = DemoData.DEMO_USER_NAME,
			)
		}

		val api = WeihudaApi(settings.baseUrl)
		return try {
			val semester = api.semester()
			val week = weekOf(LocalDate.parse(semester.startDate), today)
			val courses = api.classTable(semester.year, semester.term, settings.token)
			val user = runCatching { api.userInfo(settings.token) }.getOrNull()
			SnapshotFactory.of(
				courses = courses,
				source = WidgetSnapshot.SOURCE_API,
				week = week,
				date = today,
				now = now,
				userName = user?.name,
			)
		} catch (error: Exception) {
			SnapshotFactory.failure(
				date = today,
				week = previous?.week ?: 0,
				source = WidgetSnapshot.SOURCE_API,
				message = error.message?.takeIf { it.isNotBlank() } ?: "课表刷新失败",
				previous = previous,
			)
		} finally {
			api.close()
		}
	}
}
