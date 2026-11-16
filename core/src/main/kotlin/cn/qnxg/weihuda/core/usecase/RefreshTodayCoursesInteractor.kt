package cn.qnxg.weihuda.core.usecase

import cn.qnxg.weihuda.core.demo.DemoTimetable
import cn.qnxg.weihuda.core.entity.CampusClock
import cn.qnxg.weihuda.core.entity.Settings
import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.entity.sessionsOn
import cn.qnxg.weihuda.core.entity.weekOf
import cn.qnxg.weihuda.core.port.CourseGateway
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.SettingsStore
import cn.qnxg.weihuda.core.port.SnapshotStore
import cn.qnxg.weihuda.core.port.StudentGateway
import java.time.Clock
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/**
 * 「今天上什么课」的编排：读设置 → 选演示或后端 → 合并小节 → 写快照。
 *
 * 它不碰 Android、HTTP 和 JSON，全部外部能力都从构造函数进来，所以整条链路可以在
 * JVM 上用固定时钟和假适配器测。
 */
class RefreshTodayCoursesInteractor(
	private val settings: SettingsStore,
	private val snapshots: SnapshotStore,
	private val semesters: SemesterGateway,
	private val courses: CourseGateway,
	private val students: StudentGateway,
	private val clock: Clock = CampusClock.clock(),
	private val widgetId: WidgetId = WidgetId.TodayCourses,
) : RefreshTodayCourses {

	override suspend fun refresh(): TodayCourses {
		val date = LocalDate.now(clock)
		val settings = settings.load()
		val previous = snapshots.readTodayCourses(widgetId)

		val result = if (!settings.usesRemoteData) {
			demo(date)
		} else {
			remote(settings, date, previous)
		}

		snapshots.writeTodayCourses(widgetId, result)
		return result
	}

	private fun demo(date: LocalDate): TodayCourses {
		val week = weekOf(DemoTimetable.semesterStart(date), date)
		return TodayCourses(
			date = date,
			week = week,
			source = TodayCourses.Source.DEMO,
			studentName = DemoTimetable.studentName,
			sessions = DemoTimetable.courses().sessionsOn(date.dayOfWeek.value, week),
			failure = null,
			refreshedAt = clock.instant(),
		)
	}

	private suspend fun remote(
		settings: Settings,
		date: LocalDate,
		previous: TodayCourses?,
	): TodayCourses {
		val semester = try {
			semesters.current()
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (error: Exception) {
			return failed(error, date, week = 0, previous = previous)
		}
		val week = weekOf(semester.startDate, date)

		val timetable = try {
			courses.timetable(semester, settings.credentials)
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (error: Exception) {
			return failed(error, date, week, previous)
		}

		// 名字只是标题栏的装饰，取不到就用上一次的，不值得让整次刷新失败。
		val studentName = try {
			students.current(settings.credentials).name
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (_: Exception) {
			previous?.studentName
		}

		return TodayCourses(
			date = date,
			week = week,
			source = TodayCourses.Source.REMOTE,
			studentName = studentName,
			sessions = timetable.sessionsOn(date.dayOfWeek.value, week),
			failure = null,
			refreshedAt = clock.instant(),
		)
	}

	private fun failed(
		error: Exception,
		date: LocalDate,
		week: Int,
		previous: TodayCourses?,
	): TodayCourses = TodayCourses.ofFailure(
		date = date,
		week = week,
		message = error.message?.takeIf { it.isNotBlank() } ?: "课表刷新失败",
		previous = previous,
		refreshedAt = clock.instant(),
	)
}
