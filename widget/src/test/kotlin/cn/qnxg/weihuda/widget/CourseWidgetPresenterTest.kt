// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.CourseSession
import cn.qnxg.weihuda.core.entity.CourseStatus
import cn.qnxg.weihuda.core.usecase.TodayCourses
import cn.qnxg.weihuda.widget.presentation.CourseWidgetPresenter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Presenter 测试：状态在渲染时现算，文案只有一处。
 *
 * 视图（RemoteViews）不可测，但它的全部输入都在这里，所以「桌面会显示什么」是可测的。
 */
class CourseWidgetPresenterTest {

	private val monday = LocalDate.of(2026, 11, 16)

	private fun snapshot(
		sessions: List<CourseSession> = listOf(morning, afternoon),
		failure: String? = null,
		state: TodayCourses.State = TodayCourses.State.LOADED,
		week: Int = 10,
	) = TodayCourses(
		date = monday,
		week = week,
		state = state,
		studentName = "李同学",
		studentCollege = "电气与信息工程学院",
		sessions = sessions,
		failure = failure,
		refreshedAt = Instant.parse("2026-11-16T00:30:00Z"),
	)

	@Test
	fun `no snapshot yet still gives a non empty card`() {
		val view = CourseWidgetPresenter.present(null, LocalTime.of(9, 0))

		assertEquals("正在获取课表", view.emptyTitle)
		assertTrue(view.emptyDetail.orEmpty().isNotEmpty())
		assertEquals("等待首次刷新", view.sourceLabel)
		assertEquals("微生活课表", view.weekLabel)
	}

	@Test
	fun `status is recomputed at render time rather than frozen in the snapshot`() {
		val view = CourseWidgetPresenter.present(snapshot(), LocalTime.of(9, 0))

		assertEquals(CourseStatus.ONGOING, view.rows.first().status)
		assertEquals("上课中", view.rows.first().statusLabel)
		assertEquals(CourseStatus.UPCOMING, view.rows.last().status)
	}

	@Test
	fun `header shows the semester week and the campus date`() {
		val view = CourseWidgetPresenter.present(snapshot(), LocalTime.of(9, 0))

		assertEquals("第 10 周", view.weekLabel)
		assertEquals("周一 11/16", view.dayLabel)
		assertEquals("周一", view.dayLabelShort)
		assertEquals("08:00", view.rows.first().startTime)
		assertEquals("1-2", view.rows.first().periodLabel)
	}

	@Test
	fun `out of range week reads as vacation`() {
		val view = CourseWidgetPresenter.present(snapshot(week = 0), LocalTime.of(9, 0))

		assertEquals("假期", view.weekLabel)
	}

	@Test
	fun `empty day explains itself instead of showing a blank canvas`() {
		val view = CourseWidgetPresenter.present(snapshot(sessions = emptyList()), LocalTime.of(9, 0))

		assertEquals("今日无课", view.emptyTitle)
		assertTrue(view.emptyDetail.orEmpty().isNotEmpty())
		assertNull(view.focus)
	}

	@Test
	fun `failure on an empty day becomes the headline`() {
		val view = CourseWidgetPresenter.present(
			snapshot(sessions = emptyList(), failure = "网络请求失败", state = TodayCourses.State.FAILED),
			LocalTime.of(9, 0),
		)

		assertEquals("刷新失败", view.emptyTitle)
		assertEquals("网络请求失败", view.emptyDetail)
	}

	@Test
	fun `failure with data keeps the sessions and adds a hint`() {
		val view = CourseWidgetPresenter.present(snapshot(failure = "网络请求失败"), LocalTime.of(9, 0))

		assertNull(view.emptyTitle)
		assertEquals(2, view.rows.size)
		assertEquals("上次刷新失败：网络请求失败", view.failureLabel)
	}

	@Test
	fun `compact card focuses the ongoing session and counts the day`() {
		// 15:00 时上午那节已结束，聚焦的是下午正在上的那节
		val view = CourseWidgetPresenter.present(snapshot(), LocalTime.of(15, 0))

		assertEquals("编译原理", view.focus?.name)
		assertEquals(CourseStatus.ONGOING, view.focus?.status)
		assertEquals("今日共 2 节", view.focusTotalLabel)
	}

	@Test
	fun `a failed refresh is visible in the footer even when rows are shown`() {
		val view = CourseWidgetPresenter.present(snapshot(failure = "令牌已过期"), LocalTime.of(9, 0))

		assertTrue(view.footerIsError)
		assertEquals("刷新失败 · 令牌已过期", view.footerLabel)
	}

	@Test
	fun `a healthy snapshot keeps the source in the footer`() {
		val view = CourseWidgetPresenter.present(snapshot(), LocalTime.of(9, 0))

		assertEquals(false, view.footerIsError)
		assertTrue(view.footerLabel.startsWith("实时数据"))
	}

	@Test
	fun `signed out snapshot asks the user to sign in`() {
		val view = CourseWidgetPresenter.present(
			snapshot(sessions = emptyList(), state = TodayCourses.State.SIGNED_OUT),
			LocalTime.of(9, 0),
		)

		assertEquals("未登录", view.emptyTitle)
		assertEquals("登录校园账号以使用此组件", view.emptyDetail)
		assertEquals("微生活课表", view.weekLabel)
		assertTrue(view.sourceLabel.startsWith("未登录"))
		assertNull(view.failureLabel)
	}

	@Test
	fun `live snapshot keeps the source in the footer`() {
		assertTrue(CourseWidgetPresenter.present(snapshot(), LocalTime.of(9, 0)).sourceLabel.startsWith("实时数据"))
	}

	private val morning = CourseSession(
		name = "操作系统",
		place = "软件大楼301",
		teacher = "孙敏",
		startPeriod = 1,
		endPeriod = 2,
		startTime = LocalTime.of(8, 0),
		endTime = LocalTime.of(9, 40),
	)

	private val afternoon = CourseSession(
		name = "编译原理",
		place = "软件大楼402",
		teacher = "吴迪",
		startPeriod = 5,
		endPeriod = 6,
		startTime = LocalTime.of(14, 30),
		endTime = LocalTime.of(16, 0),
	)
}
