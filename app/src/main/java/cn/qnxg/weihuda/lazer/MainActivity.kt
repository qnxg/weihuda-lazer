package cn.qnxg.weihuda.lazer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.qnxg.weihuda.lazer.ui.LazerTheme
import cn.qnxg.weihuda.lazer.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LazerTheme {
                SettingsScreen()
            }
        }
    }
}
