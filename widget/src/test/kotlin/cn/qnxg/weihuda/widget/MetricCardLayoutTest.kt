// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.widget.presentation.MetricCardDensity
import cn.qnxg.weihuda.widget.presentation.MetricCardLayout
import kotlin.test.Test
import kotlin.test.assertEquals

/** 字号档位的边界：110dp 的 2 格卡片必须走小字号，否则「186.50度」会盖住图标。 */
class MetricCardLayoutTest {

	@Test
	fun `the narrowest two cell launcher falls into the compact tier`() {
		assertEquals(MetricCardDensity.COMPACT, MetricCardLayout.density(110))
	}

	@Test
	fun `wide cells keep the larger type`() {
		assertEquals(MetricCardDensity.WIDE, MetricCardLayout.density(170))
	}

	@Test
	fun `the threshold itself belongs to the wide tier`() {
		assertEquals(MetricCardDensity.COMPACT, MetricCardLayout.density(139))
		assertEquals(MetricCardDensity.WIDE, MetricCardLayout.density(140))
	}
}
