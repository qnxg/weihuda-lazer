// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import cn.qnxg.weihuda.core.entity.SemesterPhase
import cn.qnxg.weihuda.core.entity.countdownOn
import cn.qnxg.weihuda.core.entity.teachingWeeks
import cn.qnxg.weihuda.core.usecase.SemesterCountdown
import java.time.LocalDate

/**
 * 把学期快照变成视图模型：阶段、剩余天数与日期文案都只在这里出现。
 *
 * 天数按 [today] 现算而不是从快照读——快照里的天数第二天就是错的。
 * 应用内预览与桌面共用它，所以两端不会出现「桌面 46 天、应用 47 天」这类偏差。
 */
object CountdownWidgetPresenter {

	fun present(snapshot: SemesterCountdown?, today: LocalDate): CountdownWidgetView {
		snapshot ?: return empty("正在获取学期信息", "点按应用完成刷新")

		val semester = snapshot.semester ?: return empty(
			title = "刷新失败",
			detail = snapshot.failure.orEmpty().ifBlank { "无法获取学期信息" },
		)

		val failureLabel = snapshot.failure?.let { "刷新失败 · $it" }
		val target = semester.countdownOn(today)
		val weeks = semester.teachingWeeks()

		// 后端只保证 startDate：没有假期/开学日期时仍然把学期名放上去，只把天数留空
		if (target == null) {
			return CountdownWidgetView(
				eyebrow = "学期倒计时",
				title = semester.name,
				detail = "后端没有提供假期或开学日期",
				metricLabel = null,
				metricValue = "--",
				metricUnit = null,
				failureLabel = failureLabel,
				emptyTitle = null,
				emptyDetail = null,
			)
		}

		return CountdownWidgetView(
			eyebrow = when (target.phase) {
				SemesterPhase.BEFORE_START -> "距离学期开始"
				SemesterPhase.IN_TERM -> "距离学期结束"
				SemesterPhase.AFTER_TERM -> "距离开学"
			},
			title = semester.name,
			detail = when (target.phase) {
				SemesterPhase.BEFORE_START -> monthDay(target.date) + "开始" + teachingWeeksSuffix(weeks)
				SemesterPhase.IN_TERM -> monthDay(target.date) + "结束" + teachingWeeksSuffix(weeks)
				// 下学期的教学周数无从得知，所以假期里不说「共 N 周」
				SemesterPhase.AFTER_TERM -> "下学期 " + monthDay(target.date) + "开学"
			},
			metricLabel = "还有",
			metricValue = target.daysLeft.toString(),
			metricUnit = "天",
			failureLabel = failureLabel,
			emptyTitle = null,
			emptyDetail = null,
		)
	}

	private fun empty(title: String, detail: String) = CountdownWidgetView(
		eyebrow = "",
		title = "",
		detail = null,
		metricLabel = null,
		metricValue = "--",
		metricUnit = null,
		failureLabel = null,
		emptyTitle = title,
		emptyDetail = detail.takeIf { it.isNotBlank() },
	)

	private fun teachingWeeksSuffix(weeks: Int?): String = weeks?.let { " · 共 $it 周" }.orEmpty()

	/** `1 月 3 日`：月份与日都不补零，和截图、小程序上的写法一致。 */
	private fun monthDay(date: LocalDate): String = "${date.monthValue} 月 ${date.dayOfMonth} 日"
}
