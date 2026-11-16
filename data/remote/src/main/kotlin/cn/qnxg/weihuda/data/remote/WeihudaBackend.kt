package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.Student
import cn.qnxg.weihuda.core.port.CourseGateway
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.StudentGateway
import cn.qnxg.weihuda.data.remote.dto.CourseDto
import cn.qnxg.weihuda.data.remote.dto.SemesterDto
import cn.qnxg.weihuda.data.remote.dto.StudentDto
import cn.qnxg.weihuda.data.remote.dto.toEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException

/** 生产环境后端地址，与小程序、移动端 Web 使用同一个 API 网关。 */
const val DEFAULT_BASE_URL: String = "https://microlife.hnu.edu.cn/api/weihuda"

/**
 * 微生活后端的适配器：三个端口共用一处传输与拆包逻辑。
 *
 * 鉴权是裸 JWT（后端直接读 `Authorization` 头，没有 `Bearer` 前缀）。
 * 引擎由调用方注入，所以这个模块是纯 JVM 的，可以在 JVM 上直接编译与测试。
 */
class WeihudaBackend(
	private val http: HttpClient,
	baseUrl: String = DEFAULT_BASE_URL,
) : SemesterGateway, CourseGateway, StudentGateway {

	private val base: String = baseUrl.trimEnd('/')

	override suspend fun current(): Semester =
		fetch<SemesterDto>("$base/info/smester", credentials = null).toEntity()

	override suspend fun timetable(semester: Semester, credentials: Credentials): List<Course> =
		fetch<List<CourseDto>>(
			"$base/hdjw/class-table?xn=${semester.year}&xq=${semester.term}",
			credentials,
		).map { it.toEntity() }

	override suspend fun current(credentials: Credentials): Student =
		fetch<StudentDto>("$base/info/user", credentials).toEntity()

	private suspend inline fun <reified T> fetch(path: String, credentials: Credentials?): T {
		val text = try {
			val response = http.get(path) {
				credentials?.takeIf { it.isPresent }?.let { header(HttpHeaders.Authorization, it.token) }
			}
			if (response.status.value !in 200..299) {
				throw GatewayException(
					code = response.status.value,
					message = "接口返回 HTTP ${response.status.value}",
				)
			}
			response.bodyAsText()
		} catch (cancelled: CancellationException) {
			throw cancelled
		} catch (error: GatewayException) {
			throw error
		} catch (error: Exception) {
			throw GatewayException.network(error.message?.takeIf { it.isNotBlank() } ?: "网络请求失败")
		}
		return unwrapEnvelope(text)
	}
}

/**
 * 与后端约定的传输参数：短超时，失败交给用例去消化。
 *
 * 引擎由宿主选择（Android 用 `Android`，测试用 `MockEngine`），这里只固定超时与重定向。
 */
fun weihudaHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
	expectSuccess = false
	install(HttpTimeout) {
		connectTimeoutMillis = 8_000
		requestTimeoutMillis = 15_000
		socketTimeoutMillis = 15_000
	}
}
