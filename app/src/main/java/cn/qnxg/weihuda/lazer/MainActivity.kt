package cn.qnxg.weihuda.lazer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.qnxg.weihuda.lazer.ui.LazerApp
import cn.qnxg.weihuda.lazer.ui.theme.LazerTheme
import cn.qnxg.weihuda.widget.CourseWidgetComponent

/**
 * 唯一的 Activity：应用内所有页面都在同一个 Compose 树里。
 *
 * 它只搭主题和根组合函数，业务动作全部由 [LazerApp] 里的 AppState 转给组件门面。
 */
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		setContent {
			LazerTheme {
				LazerApp(component = CourseWidgetComponent.of(this))
			}
		}
	}
}
