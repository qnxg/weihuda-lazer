// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import cn.qnxg.weihuda.widget.presentation.CountdownWidgetPresenter
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 倒计时 presenter 测试：阶段、日期与天数的文案只有这一处，视图只摆放。
 */
class CountdownWidgetPresenterTest {

	private val semester = Semester(
		year = 2026,
		term = 1,
		startDate = LocalDate.of(2026, 9, 13),
		vacation = LocalDate.of(2027, 1, 3),
		next = LocalDate.of(2027, 2, 21),
	)

	private fun snapshot(semester: Semester? = this.semester, failure: String? = null) = SemesterCountdown(
		semester = semester,
		failure = failure,
		refreshedAt = Instant.parse("2026-11-17T02:00:00Z"),
	)

	@Test
	fun `in term the card counts down to the end of the semester`() {
		val view = CountdownWidgetPresenter.present(snapshot(), LocalDate.of(2026, 11, 17))

		assertTrue(view.hasData)
		assertEquals("距离学期结束", view.eyebrow)
		assertEquals("2026 秋季学期", view.title)
		assertEquals("1 月 3 日结束 · 共 16 周", view.detail)
		assertEquals("还有", view.metricLabel)
		assertEquals("47", view.metricValue)
		assertEquals("天", view.metricUnit)
		assertNull(view.failureLabel)
	}

	@Test
	fun `the day count follows the render date instead of the snapshot`() {
		val view = CountdownWidgetPresenter.present(snapshot(), LocalDate.of(2026, 11, 18))

		assertEquals("46", view.metricValue)
	}

	@Test
	fun `during the vacation the card counts down to the next semester start`() {
		val view = CountdownWidgetPresenter.present(snapshot(), LocalDate.of(2027, 1, 20))

		assertEquals("距离开学", view.eyebrow)
		assertEquals("2026 秋季学期", view.title)
		assertEquals("下学期 2 月 21 日开学", view.detail)
		assertEquals("32", view.metricValue)
	}

	@Test
	fun `before the term the card counts down to the start date`() {
		val view = CountdownWidgetPresenter.present(snapshot(), LocalDate.of(2026, 9, 10))

		assertEquals("距离学期开始", view.eyebrow)
		assertEquals("9 月 13 日开始 · 共 16 周", view.detail)
		assertEquals("3", view.metricValue)
	}

	@Test
	fun `missing semester data is a failure state carrying the reason`() {
		val view = CountdownWidgetPresenter.present(
			snapshot(semester = null, failure = "网络请求失败"),
			LocalDate.of(2026, 11, 17),
		)

		assertTrue(!view.hasData)
		assertEquals("刷新失败", view.emptyTitle)
		assertEquals("网络请求失败", view.emptyDetail)
	}

	@Test
	fun `no snapshot at all means the first refresh has not finished`() {
		val view = CountdownWidgetPresenter.present(null, LocalDate.of(2026, 11, 17))

		assertEquals("正在获取学期信息", view.emptyTitle)
	}

	@Test
	fun `a failure with old data keeps the numbers and adds one line`() {
		val view = CountdownWidgetPresenter.present(snapshot(failure = "网络请求失败"), LocalDate.of(2026, 11, 17))

		assertTrue(view.hasData)
		assertEquals("47", view.metricValue)
		assertEquals("刷新失败 · 网络请求失败", view.failureLabel)
	}

	@Test
	fun `a missing vacation date keeps the semester name and hides the day count`() {
		val view = CountdownWidgetPresenter.present(
			snapshot(semester = semester.copy(vacation = null)),
			LocalDate.of(2026, 11, 17),
		)

		assertTrue(view.hasData)
		assertEquals("2026 秋季学期", view.title)
		assertEquals("后端没有提供假期或开学日期", view.detail)
		assertEquals("--", view.metricValue)
		assertNull(view.metricUnit)
	}

	@Test
	fun `a calendar that does not end on a week boundary rounds to the nearest week`() {
		val view = CountdownWidgetPresenter.present(
			snapshot(semester = semester.copy(startDate = LocalDate.of(2026, 9, 10))),
			LocalDate.of(2026, 11, 17),
		)

		assertEquals("1 月 3 日结束 · 共 16 周", view.detail)
	}
}
