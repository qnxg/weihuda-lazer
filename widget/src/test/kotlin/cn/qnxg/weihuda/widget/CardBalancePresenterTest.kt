// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.core.entity.CampusCard
import cn.qnxg.weihuda.core.usecase.CardBalance
import cn.qnxg.weihuda.widget.presentation.CardBalancePresenter
import cn.qnxg.weihuda.widget.presentation.MetricIcon
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 余额 presenter 测试：货币符号、小数位与空态文案只有这一处，视图只摆放。 */
class CardBalancePresenterTest {

	private val card = CampusCard(balanceYuan = 86.5)

	private fun snapshot(
		card: CampusCard? = this.card,
		failure: String? = null,
		state: CardBalance.State = CardBalance.State.LOADED,
	) = CardBalance(
		state = state,
		card = card,
		failure = failure,
		refreshedAt = Instant.parse("2026-11-17T02:00:00Z"),
	)

	@Test
	fun `loaded balance becomes the reference card labels`() {
		val view = CardBalancePresenter.present(snapshot())

		assertTrue(view.hasData)
		assertEquals(MetricIcon.CAMPUS_CARD, view.icon)
		assertEquals("校园卡金额", view.label)
		assertEquals("¥86.50", view.value)
		assertNull(view.failureLabel)
		assertNull(view.emptyTitle)
	}

	@Test
	fun `whole yuan still shows the cents`() {
		val view = CardBalancePresenter.present(snapshot(card = CampusCard(balanceYuan = 40.0)))

		assertEquals("¥40.00", view.value)
	}

	@Test
	fun `stale balance keeps the number and adds a failure line`() {
		val view = CardBalancePresenter.present(snapshot(failure = "网络请求失败"))

		assertTrue(view.hasData)
		assertEquals("¥86.50", view.value)
		assertEquals("刷新失败 · 网络请求失败", view.failureLabel)
	}

	@Test
	fun `signed out guides to login instead of showing a zero balance`() {
		val view = CardBalancePresenter.present(
			snapshot(card = null, state = CardBalance.State.SIGNED_OUT),
		)

		assertEquals("未登录", view.emptyTitle)
		assertTrue(view.emptyDetail.orEmpty().isNotEmpty())
		assertEquals("--", view.value)
		assertNull(view.failureLabel)
	}

	@Test
	fun `failed first refresh shows the reason`() {
		val view = CardBalancePresenter.present(
			snapshot(card = null, failure = "令牌已过期", state = CardBalance.State.FAILED),
		)

		assertEquals("刷新失败", view.emptyTitle)
		assertEquals("令牌已过期", view.emptyDetail)
	}

	@Test
	fun `no snapshot yet still gives a non empty card`() {
		val view = CardBalancePresenter.present(null)

		assertEquals("正在获取余额", view.emptyTitle)
		assertEquals("--", view.value)
	}
}
