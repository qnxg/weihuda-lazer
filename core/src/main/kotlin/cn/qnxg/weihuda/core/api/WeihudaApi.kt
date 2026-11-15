package cn.qnxg.weihuda.core.api

import cn.qnxg.weihuda.core.model.ApiEnvelope
import cn.qnxg.weihuda.core.model.CourseItem
import cn.qnxg.weihuda.core.model.SemesterInfo
import cn.qnxg.weihuda.core.model.UserInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

/** 生产环境后端地址，与小程序、移动端 Web 使用同一个 API 网关。 */
const val DEFAULT_BASE_URL: String = "https://microlife.hnu.edu.cn/api/weihuda"

class ApiException(
    val code: Int,
    override val message: String,
) : Exception(message)

/**
 * 微生活后端的最小客户端。
 *
 * 鉴权使用裸 JWT（后端直接读取 `Authorization` 头，不加 `Bearer` 前缀）。
 */
class WeihudaApi @JvmOverloads constructor(
    baseUrl: String = DEFAULT_BASE_URL,
    @PublishedApi internal val http: HttpClient = defaultHttpClient(),
) {
    @PublishedApi
    internal val base: String = baseUrl.trimEnd('/')

    suspend fun ping(): String = fetch("$base/ping", token = null)

    suspend fun semester(): SemesterInfo = fetch("$base/info/smester", token = null)

    suspend fun classTable(xn: Int, xq: Int, token: String): List<CourseItem> =
        fetch("$base/hdjw/class-table?xn=$xn&xq=$xq", token)

    suspend fun userInfo(token: String): UserInfo = fetch("$base/info/user", token)

    fun close() {
        http.close()
    }

    @PublishedApi
    internal suspend inline fun <reified T> fetch(url: String, token: String?): T {
        val response = http.get(url) {
            token?.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, it) }
        }
        val text = response.bodyAsText()
        if (response.status.value !in 200..299) {
            throw ApiException(response.status.value, "接口返回 HTTP ${response.status.value}")
        }
        val envelope = runCatching {
            json.decodeFromString<ApiEnvelope<T>>(text)
        }.getOrElse {
            throw ApiException(-1, "无法解析接口响应")
        }
        val data = envelope.data
        if (envelope.code != 200 || data == null) {
            throw ApiException(envelope.code, envelope.msg?.takeIf { it.isNotBlank() } ?: "接口返回异常")
        }
        return data
    }

    companion object {
        @PublishedApi
        internal val json: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        fun defaultHttpClient(): HttpClient = HttpClient(Android) {
            expectSuccess = false
            install(HttpTimeout) {
                connectTimeoutMillis = 8_000
                requestTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}
