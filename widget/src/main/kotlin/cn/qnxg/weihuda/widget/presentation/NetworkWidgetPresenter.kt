// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.usecase.NetworkTraffic
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 把流量用例的输出变成视图模型：单位、百分比与状态文案都只在这里出现。
 *
 * 应用内预览与桌面共用它，所以两端不会出现「桌面 32%、应用 33%」这类偏差。
 */
object NetworkWidgetPresenter {

	fun present(snapshot: NetworkTraffic?): NetworkWidgetView {
		snapshot ?: return empty(
			title = "正在获取流量",
			detail = "点按应用完成登录或刷新",
		)

		val account = snapshot.account
		if (account == null) {
			val signedOut = snapshot.state == NetworkTraffic.State.SIGNED_OUT
			return empty(
				title = if (signedOut) "未登录" else "刷新失败",
				detail = if (signedOut) "登录校园账号以使用此组件" else snapshot.failure.orEmpty(),
			)
		}

		return NetworkWidgetView(
			usedLabel = gb(account.usedGb),
			usedLabelCompact = gb(account.usedGb, digits = 1),
			percentLabel = "${(account.usedRatio * 100).roundToInt().coerceAtLeast(0)}%",
			progress = account.usedRatio.coerceIn(0.0, 1.0).toFloat(),
			// 额度缺失（0）时宁可显示「—」，也不要写一个像真的 0GB
			quotaLabel = quota(account.quotaGb).takeIf { account.quotaGb > 0.0 },
			accountLabel = account.status.label,
			accountTone = account.status.tone,
			overdueLabel = overdue(account.overdueYuan),
			overdueTone = if (account.overdueYuan > 0.0) NetworkTone.BAD else NetworkTone.OK,
			emptyTitle = null,
			emptyDetail = null,
			failureLabel = snapshot.failure?.let { "刷新失败 · $it" },
		)
	}

	private fun empty(title: String, detail: String) = NetworkWidgetView(
		usedLabel = "--",
		usedLabelCompact = "--",
		percentLabel = null,
		progress = null,
		quotaLabel = null,
		accountLabel = null,
		accountTone = NetworkTone.NEUTRAL,
		overdueLabel = null,
		overdueTone = NetworkTone.NEUTRAL,
		emptyTitle = title,
		emptyDetail = detail.takeIf { it.isNotBlank() },
		failureLabel = null,
	)

	private val CampusNetwork.AccountStatus.label: String
		get() = when (this) {
			CampusNetwork.AccountStatus.NORMAL -> "正常"
			CampusNetwork.AccountStatus.LOCKED -> "已锁定"
			CampusNetwork.AccountStatus.UNKNOWN -> "未知"
		}

	private val CampusNetwork.AccountStatus.tone: NetworkTone
		get() = when (this) {
			CampusNetwork.AccountStatus.NORMAL -> NetworkTone.OK
			CampusNetwork.AccountStatus.LOCKED -> NetworkTone.BAD
			CampusNetwork.AccountStatus.UNKNOWN -> NetworkTone.NEUTRAL
		}

	private fun gb(value: Double, digits: Int = 2): String =
		String.format(Locale.US, "%.${digits}fGB", value)

	/** `40GB` / `39.5GB`：整数不带小数点，和小程序、学校页面上的写法一致。 */
	private fun quota(value: Double): String {
		val text = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
		return "${text}GB"
	}

	private fun overdue(value: Double): String =
		if (value <= 0.0) "无欠费" else String.format(Locale.US, "¥%.2f", value)
}
