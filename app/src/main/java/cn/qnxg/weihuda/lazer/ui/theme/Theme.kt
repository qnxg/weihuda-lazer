package cn.qnxg.weihuda.lazer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 设计令牌，与 AGENTS.md 的「视觉」一节一一对应。
 *
 * 小组件侧的那份在 `widget/WidgetContent.kt`：RemoteViews 不能用 MaterialTheme，
 * 所以颜色只能各写一份；新增令牌时两处都要加。
 */
object LazerColors {
	val Brand = Color(0xFF318CCB)
	val BrandDark = Color(0xFF1F5F8B)
	val Ink = Color(0xFF1B2733)
	val InkMuted = Color(0xFF6A7883)
	val PageBackground = Color(0xFFF4F6F9)
	val Line = Color(0xFFEDF1F5)
	val Danger = Color(0xFFD64545)
	val Success = Color(0xFF2E7D32)
	val Warning = Color(0xFFE08600)
	val Finished = Color(0xFF9AA5AF)
}

private val ColorScheme = lightColorScheme(
	primary = LazerColors.Brand,
	onPrimary = Color.White,
	secondary = LazerColors.BrandDark,
	background = LazerColors.PageBackground,
	onBackground = LazerColors.Ink,
	surface = Color.White,
	onSurface = LazerColors.Ink,
	outline = LazerColors.Line,
	error = LazerColors.Danger,
)

@Composable
fun LazerTheme(content: @Composable () -> Unit) {
	MaterialTheme(colorScheme = ColorScheme, content = content)
}
