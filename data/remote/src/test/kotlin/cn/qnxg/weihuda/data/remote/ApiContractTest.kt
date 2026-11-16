package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.data.remote.dto.CourseDto
import cn.qnxg.weihuda.data.remote.dto.SemesterDto
import cn.qnxg.weihuda.data.remote.dto.toEntity
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 后端契约的回归测试：信封、字段映射、失败翻译。
 *
 * 全部是纯函数，不启 HTTP —— 接口契约的变化最先在这里露头。
 */
class ApiContractTest {

	@Test
	fun `envelope unwraps data when code is 200`() {
		val text = """{"code":200,"data":{"startDate":"2026-09-13","term":1,"year":2026},"msg":"请求成功"}"""

		val semester = unwrapEnvelope<SemesterDto>(text)

		assertEquals("2026-09-13", semester.startDate)
		assertEquals(1, semester.term)
	}

	@Test
	fun `business code other than 200 becomes a gateway failure carrying the message`() {
		val text = """{"code":401,"msg":"令牌无效"}"""

		val error = assertFailsWith<GatewayException> { unwrapEnvelope<SemesterDto>(text) }

		assertEquals(401, error.code)
		assertEquals("令牌无效", error.message)
	}

	@Test
	fun `malformed body is reported as an unparsable response`() {
		val error = assertFailsWith<GatewayException> { unwrapEnvelope<SemesterDto>("<html>502</html>") }

		assertEquals(-1, error.code)
		assertEquals("无法解析接口响应", error.message)
	}

	@Test
	fun `course fields are mapped onto the entity`() {
		val text = """
			{"code":200,"data":[{
				"courseName":"操作系统","courseId":"CS30501","type":"必修","className":"计科2301",
				"place":"软件大楼301","area":"南校区","teacher":"孙敏","weeks":[1,2,3],
				"day":0,"time":5,"credit":4.0,"customizeId":-1,"people":56,"unknownField":"忽略"
			}]}
		""".trimIndent()

		val course = unwrapEnvelope<List<CourseDto>>(text).single().toEntity()

		assertEquals("操作系统", course.name)
		assertEquals("CS30501", course.code)
		assertEquals("软件大楼301", course.place)
		assertEquals(listOf(1, 2, 3), course.weeks)
		assertEquals(0, course.dayOfWeek)
		assertEquals(5, course.period)
		assertEquals(4.0f, course.credits)
	}

	@Test
	fun `semester dto becomes an entity with a parsed start date`() {
		val semester = SemesterDto(startDate = "2026-09-13", term = 2, year = 2026).toEntity()

		assertEquals(LocalDate.of(2026, 9, 13), semester.startDate)
	}

	@Test
	fun `unparsable semester start date is a gateway failure`() {
		val error = assertFailsWith<GatewayException> {
			SemesterDto(startDate = "第三周", term = 1, year = 2026).toEntity()
		}

		assertEquals(-1, error.code)
	}
}
