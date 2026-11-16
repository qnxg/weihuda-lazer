package cn.qnxg.weihuda.widget.presentation

/**
 * 小组件的尺寸规则：给定桌面单元格的实际 dp，决定信息密度和能放几行。
 *
 * Glance 视图与应用内预览都从这里取值。以前两边各写一份 `when (size)`，
 * 于是预览与桌面会在临界尺寸上给出不同的行数——这类偏差只有用户才发现得了。
 *
 * 数字来自真机测量（440dpi、默认字号）：一行 4×2 课程占用约 20dp，
 * 头部约 20dp，分割线 7dp，页脚约 20dp，卡片内边距 8+8dp。
 */
enum class WidgetDensity {
	/** 2×2：只放大一节课。 */
	COMPACT,

	/** 4×2 与多数启动器的 4×3：逐行列出。 */
	WIDE,

	/** 4×4：两行一节课，带状态文字。 */
	LARGE,
}

object WidgetLayout {
	/** 宽度小于这个值就按 2×2 处理：方形小尺寸里一行字放不下。 */
	private const val COMPACT_WIDTH_DP = 180

	private const val LARGE_HEIGHT_DP = 230

	private const val CHROME_DP = 65

	private const val WIDE_ROW_DP = 20

	private const val LARGE_ROW_DP = 34

	/** 4×4 顶部那一行「上次刷新失败」的高度。 */
	private const val FAILURE_LINE_DP = 12

	fun density(widthDp: Int, heightDp: Int): WidgetDensity = when {
		widthDp < COMPACT_WIDTH_DP -> WidgetDensity.COMPACT
		heightDp >= LARGE_HEIGHT_DP -> WidgetDensity.LARGE
		else -> WidgetDensity.WIDE
	}

	/**
	 * 当前高度能完整放下的课程行数。
	 *
	 * 返回 0 表示放不下整行——此时视图只显示聚焦卡片/空态，不让半行文字压在页脚上。
	 */
	fun visibleRows(heightDp: Int, density: WidgetDensity, hasFailure: Boolean): Int = when (density) {
		WidgetDensity.COMPACT -> 0
		WidgetDensity.WIDE -> (heightDp - CHROME_DP) / WIDE_ROW_DP
		WidgetDensity.LARGE -> {
			val chrome = CHROME_DP + if (hasFailure) FAILURE_LINE_DP else 0
			(heightDp - chrome) / LARGE_ROW_DP
		}
	}.coerceAtLeast(0)
}
