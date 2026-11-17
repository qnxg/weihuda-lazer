// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.widget

import cn.qnxg.weihuda.widget.presentation.WidgetDensity
import cn.qnxg.weihuda.widget.presentation.WidgetLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WidgetLayoutTest {

	@Test
	fun `narrow cells fall back to the focus card`() {
		assertEquals(WidgetDensity.COMPACT, WidgetLayout.density(110, 110))
		assertEquals(0, WidgetLayout.visibleRows(110, WidgetDensity.COMPACT))
	}

	@Test
	fun `4x2 shows two single line rows`() {
		assertEquals(WidgetDensity.WIDE, WidgetLayout.density(250, 110))
		assertEquals(2, WidgetLayout.visibleRows(110, WidgetDensity.WIDE))
	}

	@Test
	fun `4x3 switches to the compact two line rows and fits three`() {
		assertEquals(WidgetDensity.MEDIUM, WidgetLayout.density(250, 170))
		assertEquals(3, WidgetLayout.visibleRows(170, WidgetDensity.MEDIUM))
		assertEquals(3, WidgetLayout.visibleRows(153, WidgetDensity.MEDIUM))
	}

	@Test
	fun `4x4 switches to the two line rows and fits four`() {
		assertEquals(WidgetDensity.LARGE, WidgetLayout.density(250, 250))
		assertEquals(4, WidgetLayout.visibleRows(250, WidgetDensity.LARGE))
	}
}

class MediumRowHeightTest {

	@Test
	fun `four courses still use three rows when the overflow line fits`() {
		assertTrue(WidgetLayout.hasRoomForHidden(170, WidgetDensity.MEDIUM, 3))
		assertEquals(false, WidgetLayout.hasRoomForHidden(153, WidgetDensity.MEDIUM, 3))
		assertEquals(false, WidgetLayout.hasRoomForHidden(250, WidgetDensity.LARGE, 4))
	}

	@Test
	fun `two courses stretch to the reference row height`() {
		assertEquals(48, WidgetLayout.mediumRowHeight(153, 2, hasHidden = false))
	}

	@Test
	fun `three courses fall back to a compact two line row`() {
		assertEquals(33, WidgetLayout.mediumRowHeight(153, 3, hasHidden = false))
	}
}
