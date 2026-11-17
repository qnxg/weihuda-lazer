// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget.presentation

import androidx.annotation.DrawableRes
import cn.qnxg.weihuda.widget.R

/**
 * 图标枚举 → 模块内的 vector。
 *
 * 资源只有这一份：桌面（Glance）与应用内预览都从这里取，所以两边不会画出两个不同的图标。
 * 应用侧引用 `cn.qnxg.weihuda.widget.R` 是资源级依赖，不涉及表现层之外的任何类型。
 */
@DrawableRes
fun MetricIcon.drawableRes(): Int = when (this) {
	MetricIcon.CAMPUS_CARD -> R.drawable.ic_metric_card
	MetricIcon.DORM_ELECTRICITY -> R.drawable.ic_metric_bolt
}
