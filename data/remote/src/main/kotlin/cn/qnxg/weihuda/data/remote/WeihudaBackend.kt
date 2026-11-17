// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Semester
import cn.qnxg.weihuda.core.entity.Student
import cn.qnxg.weihuda.core.port.AuthGateway
import cn.qnxg.weihuda.core.port.CourseGateway
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.core.port.NetworkGateway
import cn.qnxg.weihuda.core.port.SemesterGateway
import cn.qnxg.weihuda.core.port.StudentGateway
import cn.qnxg.weihuda.data.remote.dto.BindRequestDto
import cn.qnxg.weihuda.data.remote.dto.CourseDto
import cn.qnxg.weihuda.data.remote.dto.NetflowDto
import cn.qnxg.weihuda.data.remote.dto.PowTicketDto
import cn.qnxg.weihuda.data.remote.dto.SemesterDto
import cn.qnxg.weihuda.data.remote.dto.StudentDto
import cn.qnxg.weihuda.data.remote.dto.toEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement

/** 生产环境后端地址，与小程序、移动端 Web 使用同一个 API 网关。 */
const val DEFAULT_BASE_URL: String = "https://microlife.hnu.edu.cn/api/weihuda"

/** 学校统一认证的「找回密码」页：改完密码回本应用重新登录即可。 */
const val PASSWORD_RESET_URL: String = "https://cas.hnu.edu.cn/V3/securitycenter/findPwd/index.zf"

/**
 * 后端 `infra/wechat.rs` 对 `code == "testing"` 直接返回固定 openid，是移动端唯一不需要
 * 微信小程序的登录通道。代价是这个学号的 openid 会被改写成固定值，小程序侧随之分绑；
 * 后端补上 `POST /auth/login` 后这里应当删掉。
 */
private const val DEBUG_LOGIN_CODE: String = "testing"

/**
 * 微生活后端的适配器：三个端口共用一处传输与拆包逻辑。
 *
 * 鉴权是裸 JWT（后端直接读 `Authorization` 头，没有 `Bearer` 前缀）。
 * 引擎由调用方注入，所以这个模块是纯 JVM 的，可以在 JVM 上直接编译与测试。
 */
class WeihudaBackend(
	private val http: HttpClient,
	baseUrl: String = DEFAULT_BASE_URL,
) : SemesterGateway, CourseGateway, StudentGateway, NetworkGateway, AuthGateway {

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

	override suspend fun usage(credentials: Credentials): CampusNetwork =
		fetch<NetflowDto>("$base/netflow", credentials).toEntity()

	/**
	 * 绑定 + 取令牌两个请求拼成一次登录。
	 *
	 * 绑定接口会校验 PoW 并用 RSA 密文验证密码；拿到令牌再单独换一次，因为绑定的响应
	 * 只有一句成功文案。
	 */
	override suspend fun signIn(stuId: String, password: String): Credentials {
		val pow = fetch<PowTicketDto>("$base/bind/pow?stuId=${stuId.encodeURLParameter()}", credentials = null)
		// 求解是纯 CPU 循环，调用方可能是主线程 scope，不能让它在那里空转。
		val answer = withContext(Dispatchers.Default) { solveProofOfWork(pow.ticket, pow.difficulty) }
		val body = WeihudaJson.encodeToString(
			BindRequestDto.serializer(),
			BindRequestDto(
				powTicket = pow.ticket,
				powAnswer = answer,
				code = DEBUG_LOGIN_CODE,
				stuId = stuId,
				password = FrontendRsa.encrypt(password),
			),
		)
		postJson("$base/bind", body)
		return Credentials(fetch<String>("$base/token?code=$DEBUG_LOGIN_CODE", credentials = null))
	}

	private suspend fun postJson(path: String, body: String) {
		val text = try {
			val response = http.post(path) {
				contentType(ContentType.Application.Json)
				setBody(body)
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
		// 只关心成败，忽略响应体。
		unwrapEnvelope<JsonElement>(text)
	}

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
