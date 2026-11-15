package cn.qnxg.weihuda.lazer.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.core.domain.WidgetSnapshot
import cn.qnxg.weihuda.widget.CourseWidgets
import cn.qnxg.weihuda.widget.data.AppSettings
import cn.qnxg.weihuda.widget.data.SettingsStore
import cn.qnxg.weihuda.widget.data.WidgetStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { SettingsStore(context) }
    val initial = remember { store.load() }

    var baseUrl by remember { mutableStateOf(initial.baseUrl) }
    var token by remember { mutableStateOf(initial.token) }
    var demoMode by remember { mutableStateOf(initial.demoMode) }
    var snapshot by remember { mutableStateOf(WidgetStore.read(context)) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun save(): AppSettings = AppSettings(
        baseUrl = baseUrl,
        token = token,
        demoMode = demoMode,
    ).also(store::save)

    fun refresh() {
        val settings = save()
        scope.launch {
            busy = true
            runCatching { CourseWidgets.refresh(context, settings) }
                .onSuccess { loaded ->
                    snapshot = loaded
                    isError = loaded.error != null
                    status = loaded.error?.let { "刷新失败：$it" }
                        ?: "已刷新（${if (loaded.isDemo) "演示数据" else "实时数据"}）"
                }
                .onFailure {
                    isError = true
                    status = "刷新失败：${it.message ?: "未知错误"}"
                }
            busy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        Header()
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SourceCard(
                demoMode = demoMode,
                onDemoModeChange = { demoMode = it },
                baseUrl = baseUrl,
                onBaseUrlChange = { baseUrl = it },
                token = token,
                onTokenChange = { token = it },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = ::refresh,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (busy) "刷新中…" else "保存并刷新")
                }
                OutlinedButton(
                    onClick = { pinWidget(context) { message, error -> status = message; isError = error } },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("添加到桌面")
                }
            }

            status?.let { text ->
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = if (isError) Danger else Success,
                )
            }

            PreviewCard(snapshot)

            NotesCard()
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Brand, BrandDark)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column {
            Text(
                text = "微生活小组件",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "湖南大学课程桌面卡片 · Android",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SourceCard(
    demoMode: Boolean,
    onDemoModeChange: (Boolean) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    token: String,
    onTokenChange: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("使用演示数据", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "未配置令牌时展示内置课表，可离线预览",
                        fontSize = 12.sp,
                        color = InkMuted,
                    )
                }
                Switch(checked = demoMode, onCheckedChange = onDemoModeChange)
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                label = { Text("后端地址") },
                enabled = !demoMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                label = { Text("访问令牌（JWT）") },
                enabled = !demoMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "后端目前只提供微信 jscode 登录，令牌取自已登录的小程序侧；" +
                    "拿到后粘贴到这里即可读取真实课表。",
                fontSize = 11.sp,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun PreviewCard(snapshot: WidgetSnapshot?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("小组件预览", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("4×2 / 2×2 / 4×4 三种尺寸", fontSize = 12.sp, color = InkMuted)
            Spacer(Modifier.height(12.dp))
            WidgetMock(snapshot, widthDp = 250, heightDp = 110, mode = PreviewMode.WIDE)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetMock(snapshot, widthDp = 110, heightDp = 110, mode = PreviewMode.COMPACT)
                WidgetMock(snapshot, widthDp = 250, heightDp = 250, mode = PreviewMode.LARGE)
            }
        }
    }
}

@Composable
private fun NotesCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("刷新机制", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                "· 桌面小组件每 15 分钟由 WorkManager 后台刷新一次\n" +
                    "· 点小组件里的「刷新」立即拉取；点其余区域打开本应用\n" +
                    "· 刷新失败时保留当天上一次成功的课表，仅在顶部提示错误",
                fontSize = 12.sp,
                color = InkMuted,
            )
        }
    }
}

private fun pinWidget(context: Context, report: (String, Boolean) -> Unit) {
    if (CourseWidgets.pin(context)) {
        report("请在弹出的系统弹窗里确认添加", false)
    } else {
        report("当前桌面不支持一键添加，请长按桌面空白处 → 添加小组件 → 今日课程", false)
    }
}
