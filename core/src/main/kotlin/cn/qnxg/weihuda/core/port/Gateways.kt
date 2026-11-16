package cn.qnxg.weihuda.core.port

import cn.qnxg.weihuda.core.entity.Credentials
import cn.qnxg.weihuda.core.entity.Course
import cn.qnxg.weihuda.core.entity.Semester

/**
 * 后端接口整体不可用时的失败。
 *
 * [code] 是后端的业务码；传输层失败用 [NETWORK_FAILURE]，HTTP 层失败用真实状态码。
 * 用例只把这个消息交给界面显示，不解释它。
 */
class GatewayException(
	val code: Int,
	override val message: String,
) : Exception(message) {
	companion object {
		const val NETWORK_FAILURE: Int = -1

		fun network(message: String): GatewayException = GatewayException(NETWORK_FAILURE, message)
	}
}

/** 学期信息（`GET /info/smester`），不需要登录。 */
interface SemesterGateway {
	suspend fun current(): Semester
}

/** 课表（`GET /hdjw/class-table`），需要令牌。 */
interface CourseGateway {
	suspend fun timetable(semester: Semester, credentials: Credentials): List<Course>
}

/** 学生信息（`GET /info/user`），需要令牌。取不到不影响课表展示，所以用例会容忍它失败。 */
interface StudentGateway {
	suspend fun current(credentials: Credentials): cn.qnxg.weihuda.core.entity.Student
}

/**
 * 登录换令牌。
 *
 * 后端的登录入口只认微信 jscode，Android 拿不到 code，所以当前实现走的是后端的调试
 * 通道（`code=testing`）。换实现（比如后端补上 `POST /auth/login`）不需要动界面与用例。
 */
interface AuthGateway {
	suspend fun signIn(stuId: String, password: String): Credentials
}
