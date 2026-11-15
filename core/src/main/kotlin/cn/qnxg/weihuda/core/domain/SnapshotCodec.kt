package cn.qnxg.weihuda.core.domain

import kotlinx.serialization.json.Json

/** 小组件快照的序列化入口（Sp/存储都走这里，避免各处各写一份 Json 配置）。 */
object SnapshotCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun encode(snapshot: WidgetSnapshot): String = json.encodeToString(WidgetSnapshot.serializer(), snapshot)

    fun decode(text: String?): WidgetSnapshot? {
        if (text.isNullOrBlank()) return null
        return runCatching { json.decodeFromString(WidgetSnapshot.serializer(), text) }.getOrNull()
    }
}
