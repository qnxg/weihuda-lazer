// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.core.usecase

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
 * 「今天上什么课」的编排：读设置 → 未登录就输出登录态，否则请求后端 → 合并小节 → 写快照。
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
			TodayCourses.signedOut(date, clock.instant())
		} else {
			remote(settings, date, previous)
		}

		snapshots.writeTodayCourses(widgetId, result)
		return result
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

		// 名字与学院只是首页标题栏的装饰，取不到就用上一次的，不值得让整次刷新失败。
		val student = try {
			students.current(settings.credentials)
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (_: Exception) {
			null
		}

		return TodayCourses(
			date = date,
			week = week,
			state = TodayCourses.State.LOADED,
			studentName = student?.name ?: previous?.studentName,
			studentCollege = student?.college ?: previous?.studentCollege,
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
