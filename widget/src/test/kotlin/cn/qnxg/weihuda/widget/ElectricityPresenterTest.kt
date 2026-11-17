// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.DormElectricity
import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import cn.qnxg.weihuda.widget.presentation.ElectricityPresenter
import cn.qnxg.weihuda.widget.presentation.MetricIcon
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 电量 presenter 测试：单位与小数位在这里定，视图只摆放。 */
class ElectricityPresenterTest {

	private val reading = DormElectricity(remainingKwh = 186.5)

	private fun snapshot(
		electricity: DormElectricity? = this.reading,
		failure: String? = null,
		state: ElectricityBalance.State = ElectricityBalance.State.LOADED,
	) = ElectricityBalance(
		state = state,
		electricity = electricity,
		failure = failure,
		refreshedAt = Instant.parse("2026-11-17T02:00:00Z"),
	)

	@Test
	fun `loaded reading becomes the reference card labels`() {
		val view = ElectricityPresenter.present(snapshot())

		assertTrue(view.hasData)
		assertEquals(MetricIcon.DORM_ELECTRICITY, view.icon)
		assertEquals("宿舍剩余电量", view.label)
		assertEquals("186.50度", view.value)
		assertNull(view.failureLabel)
	}

	@Test
	fun `whole degrees still show two decimals`() {
		val view = ElectricityPresenter.present(snapshot(electricity = DormElectricity(remainingKwh = 12.0)))

		assertEquals("12.00度", view.value)
	}

	@Test
	fun `stale reading keeps the number and adds a failure line`() {
		val view = ElectricityPresenter.present(snapshot(failure = "网络请求失败"))

		assertTrue(view.hasData)
		assertEquals("186.50度", view.value)
		assertEquals("刷新失败 · 网络请求失败", view.failureLabel)
	}

	@Test
	fun `signed out guides to login instead of showing zero degrees`() {
		val view = ElectricityPresenter.present(
			snapshot(electricity = null, state = ElectricityBalance.State.SIGNED_OUT),
		)

		assertEquals("未登录", view.emptyTitle)
		assertEquals("--", view.value)
	}

	@Test
	fun `unsupported dormitory surfaces the backend message`() {
		val view = ElectricityPresenter.present(
			snapshot(electricity = null, failure = "尚不支持你的宿舍", state = ElectricityBalance.State.FAILED),
		)

		assertEquals("刷新失败", view.emptyTitle)
		assertEquals("尚不支持你的宿舍", view.emptyDetail)
	}

	@Test
	fun `no snapshot yet still gives a non empty card`() {
		val view = ElectricityPresenter.present(null)

		assertEquals("正在获取电量", view.emptyTitle)
		assertEquals("--", view.value)
	}
}
