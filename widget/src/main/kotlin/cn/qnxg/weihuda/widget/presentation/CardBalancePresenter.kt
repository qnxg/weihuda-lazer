// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.usecase.CardBalance
import java.util.Locale

/**
 * 把余额用例的输出变成视图模型：货币符号与小数位只在这里出现。
 *
 * 应用内预览与桌面共用它，所以两端不会出现「桌面 ¥86.5、应用 ¥86.50」这类偏差。
 */
object CardBalancePresenter {

	fun present(snapshot: CardBalance?): MetricCardView {
		snapshot ?: return empty(
			title = "正在获取余额",
			detail = "点按应用完成登录或刷新",
		)

		val card = snapshot.card
		if (card == null) {
			val signedOut = snapshot.state == CardBalance.State.SIGNED_OUT
			return empty(
				title = if (signedOut) "未登录" else "刷新失败",
				detail = if (signedOut) "登录校园账号以使用此组件" else snapshot.failure.orEmpty(),
			)
		}

		return MetricCardView(
			icon = MetricIcon.CAMPUS_CARD,
			label = "校园卡金额",
			value = yuan(card.balanceYuan),
			failureLabel = snapshot.failure?.let { "刷新失败 · $it" },
			emptyTitle = null,
			emptyDetail = null,
		)
	}

	private fun empty(title: String, detail: String) = MetricCardView(
		icon = MetricIcon.CAMPUS_CARD,
		label = "校园卡金额",
		value = "--",
		failureLabel = null,
		emptyTitle = title,
		emptyDetail = detail.takeIf { it.isNotBlank() },
	)

	/** 余额永远带两位小数：少一位会被当成「分」的精度，学校页面也是这么写的。 */
	private fun yuan(value: Double): String = String.format(Locale.US, "¥%.2f", value)
}
