// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.widget.presentation.CountdownWidgetLayout
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 只有一档尺寸切换：低于门槛时放弃图标底片，保住天数。 */
class CountdownWidgetLayoutTest {

	@Test
	fun `a one cell high card is roomy`() {
		// 真机实测：Mi 10 Pro 上 MIUI 的一个单元格约 77dp
		assertFalse(CountdownWidgetLayout.compact(77))
		assertFalse(CountdownWidgetLayout.compact(72))
	}

	@Test
	fun `the declared minimum height falls back to the compact form`() {
		assertTrue(CountdownWidgetLayout.compact(60))
		assertTrue(CountdownWidgetLayout.compact(71))
	}
}
