package cn.qnxg.weihuda.lazer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Brand = Color(0xFF318CCB)
val BrandDark = Color(0xFF1F5F8B)
val Ink = Color(0xFF1B2733)
val InkMuted = Color(0xFF6A7883)
val PageBackground = Color(0xFFF4F6F9)
val Line = Color(0xFFEDF1F5)
val Danger = Color(0xFFD64545)
val Success = Color(0xFF2E7D32)
val Warning = Color(0xFFE08600)

private val ColorScheme = lightColorScheme(
	primary = Brand,
	onPrimary = Color.White,
	secondary = BrandDark,
	background = PageBackground,
	onBackground = Ink,
	surface = Color.White,
	onSurface = Ink,
	outline = Line,
	error = Danger,
)

@Composable
fun LazerTheme(content: @Composable () -> Unit) {
	MaterialTheme(colorScheme = ColorScheme, content = content)
}
