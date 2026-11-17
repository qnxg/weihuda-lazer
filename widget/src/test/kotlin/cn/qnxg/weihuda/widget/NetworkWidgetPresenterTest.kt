// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import cn.qnxg.weihuda.widget.presentation.NetworkDensity
import cn.qnxg.weihuda.widget.presentation.NetworkTone
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetLayout
import cn.qnxg.weihuda.widget.presentation.NetworkWidgetPresenter
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 流量 presenter 测试：单位、百分比与状态文案只有这一处，视图只摆放。
 */
class NetworkWidgetPresenterTest {

	private val account = CampusNetwork(
		usedGb = 12.8,
		quotaGb = 40.0,
		usedRatio = 0.32,
		status = CampusNetwork.AccountStatus.NORMAL,
		overdueYuan = 0.0,
	)

	private fun snapshot(
		account: CampusNetwork? = this.account,
		failure: String? = null,
		state: NetworkTraffic.State = NetworkTraffic.State.LOADED,
	) = NetworkTraffic(
		state = state,
		account = account,
		failure = failure,
		refreshedAt = Instant.parse("2026-11-17T02:00:00Z"),
	)

	@Test
	fun `loaded account becomes the reference card labels`() {
		val view = NetworkWidgetPresenter.present(snapshot())

		assertTrue(view.hasData)
		assertEquals("12.80GB", view.usedLabel)
		assertEquals("12.8GB", view.usedLabelCompact)
		assertEquals("32%", view.percentLabel)
		assertEquals(0.32f, view.progress)
		assertEquals("40GB", view.quotaLabel)
		assertEquals("正常", view.accountLabel)
		assertEquals(NetworkTone.OK, view.accountTone)
		assertEquals("无欠费", view.overdueLabel)
		assertEquals(NetworkTone.OK, view.overdueTone)
		assertNull(view.emptyTitle)
		assertNull(view.failureLabel)
	}

	@Test
	fun `missing quota is hidden rather than shown as a real zero`() {
		val view = NetworkWidgetPresenter.present(snapshot(account = account.copy(quotaGb = 0.0)))

		assertNull(view.quotaLabel)
	}

	@Test
	fun `fractional quota keeps one decimal instead of a trailing zero`() {
		val view = NetworkWidgetPresenter.present(snapshot(account = account.copy(quotaGb = 39.5)))

		assertEquals("39.5GB", view.quotaLabel)
	}

	@Test
	fun `locked account and overdue payment are both errors`() {
		val view = NetworkWidgetPresenter.present(
			snapshot(account = account.copy(status = CampusNetwork.AccountStatus.LOCKED, overdueYuan = 12.5)),
		)

		assertEquals("已锁定", view.accountLabel)
		assertEquals(NetworkTone.BAD, view.accountTone)
		assertEquals("¥12.50", view.overdueLabel)
		assertEquals(NetworkTone.BAD, view.overdueTone)
	}

	@Test
	fun `unknown account status stays neutral rather than alarming`() {
		val view = NetworkWidgetPresenter.present(
			snapshot(account = account.copy(status = CampusNetwork.AccountStatus.UNKNOWN)),
		)

		assertEquals("未知", view.accountLabel)
		assertEquals(NetworkTone.NEUTRAL, view.accountTone)
	}

	@Test
	fun `usage above the quota keeps the real percentage and fills the bar`() {
		val view = NetworkWidgetPresenter.present(snapshot(account = account.copy(usedRatio = 1.2)))

		assertEquals("120%", view.percentLabel)
		assertEquals(1f, view.progress)
	}

	@Test
	fun `no snapshot yet still gives a non empty card`() {
		val view = NetworkWidgetPresenter.present(null)

		assertEquals("正在获取流量", view.emptyTitle)
		assertTrue(view.emptyDetail.orEmpty().isNotEmpty())
		assertEquals("--", view.usedLabel)
		assertNull(view.failureLabel)
	}

	@Test
	fun `signed out guides to login instead of showing zero usage`() {
		val view = NetworkWidgetPresenter.present(
			snapshot(account = null, state = NetworkTraffic.State.SIGNED_OUT),
		)

		assertEquals("未登录", view.emptyTitle)
		assertEquals("登录校园账号以使用此组件", view.emptyDetail)
	}

	@Test
	fun `failure without old data becomes the headline`() {
		val view = NetworkWidgetPresenter.present(
			snapshot(account = null, failure = "网络请求失败", state = NetworkTraffic.State.FAILED),
		)

		assertEquals("刷新失败", view.emptyTitle)
		assertEquals("网络请求失败", view.emptyDetail)
	}

	@Test
	fun `failure with old data keeps the numbers and adds a hint`() {
		val view = NetworkWidgetPresenter.present(snapshot(failure = "网络请求失败"))

		assertTrue(view.hasData)
		assertEquals("12.80GB", view.usedLabel)
		assertEquals("刷新失败 · 网络请求失败", view.failureLabel)
	}
}

class NetworkWidgetLayoutTest {

	@Test
	fun `2x2 keeps only the usage and the bar`() {
		assertEquals(NetworkDensity.COMPACT, NetworkWidgetLayout.density(110, 110))
	}

	@Test
	fun `4x2 has room for the number but not the footer`() {
		assertEquals(NetworkDensity.WIDE, NetworkWidgetLayout.density(250, 110))
	}

	@Test
	fun `4x3 and 4x4 both show the full reference layout`() {
		assertEquals(NetworkDensity.FULL, NetworkWidgetLayout.density(250, 170))
		assertEquals(NetworkDensity.FULL, NetworkWidgetLayout.density(250, 250))
	}
}
