// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.usecase.ElectricityBalance
import java.util.Locale

/**
 * 把电量用例的输出变成视图模型：单位与小数位只在这里出现。
 *
 * 后端给的是「186.50度」这样的字符串，适配器把它拆成数值，这里再把它按参考设计拼回去——
 * 两端共用同一个 presenter，桌面与应用内的写法因此不会分叉。
 */
object ElectricityPresenter {

	fun present(snapshot: ElectricityBalance?): MetricCardView {
		snapshot ?: return empty(
			title = "正在获取电量",
			detail = "点按应用完成登录或刷新",
		)

		val electricity = snapshot.electricity
		if (electricity == null) {
			val signedOut = snapshot.state == ElectricityBalance.State.SIGNED_OUT
			return empty(
				title = if (signedOut) "未登录" else "刷新失败",
				detail = if (signedOut) "登录校园账号以使用此组件" else snapshot.failure.orEmpty(),
			)
		}

		return MetricCardView(
			icon = MetricIcon.DORM_ELECTRICITY,
			label = "宿舍剩余电量",
			value = kwh(electricity.remainingKwh),
			failureLabel = snapshot.failure?.let { "刷新失败 · $it" },
			emptyTitle = null,
			emptyDetail = null,
		)
	}

	private fun empty(title: String, detail: String) = MetricCardView(
		icon = MetricIcon.DORM_ELECTRICITY,
		label = "宿舍剩余电量",
		value = "--",
		failureLabel = null,
		emptyTitle = title,
		emptyDetail = detail.takeIf { it.isNotBlank() },
	)

	private fun kwh(value: Double): String = String.format(Locale.US, "%.2f度", value)
}
