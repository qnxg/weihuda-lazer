// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.entity.CampusNetwork
import cn.qnxg.weihuda.core.port.GatewayException
import cn.qnxg.weihuda.data.remote.dto.CardInfoDto
import cn.qnxg.weihuda.data.remote.dto.CourseDto
import cn.qnxg.weihuda.data.remote.dto.NetflowDto
import cn.qnxg.weihuda.data.remote.dto.NetflowMonthDto
import cn.qnxg.weihuda.data.remote.dto.SemesterDto
import cn.qnxg.weihuda.data.remote.dto.parseDormElectricity
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
	fun `vacation and next semester dates are parsed for the countdown`() {
		val semester = SemesterDto(
			startDate = "2026-09-13",
			term = 1,
			year = 2026,
			vacation = "2027-01-03",
			next = "2027-02-21",
		).toEntity()

		assertEquals(LocalDate.of(2027, 1, 3), semester.vacation)
		assertEquals(LocalDate.of(2027, 2, 21), semester.next)
	}

	@Test
	fun `unparsable vacation or next date is dropped instead of failing the refresh`() {
		val semester = SemesterDto(
			startDate = "2026-09-13",
			term = 1,
			year = 2026,
			vacation = "另行通知",
			next = null,
		).toEntity()

		assertEquals(null, semester.vacation)
		assertEquals(null, semester.next)
	}

	@Test
	fun `unparsable semester start date is a gateway failure`() {
		val error = assertFailsWith<GatewayException> {
			SemesterDto(startDate = "第三周", term = 1, year = 2026).toEntity()
		}

		assertEquals(-1, error.code)
	}

	@Test
	fun `netflow fields are mapped onto the campus network entity`() {
		val text = """
			{"code":200,"data":{
				"thisMonth":{
					"download":"12.1G","upload":"0.7G","all":"12.8G",
					"allBasePackageAmount":40.0,"basePackageUsed":12.8,"basePackageUsedPer":0.32,
					"extendPackageUsed":0.0,"unknownField":"忽略"
				},
				"unlock":"未锁定","payInfo":0.0
			}}
		""".trimIndent()

		val network = unwrapEnvelope<NetflowDto>(text).toEntity()

		assertEquals(12.8, network.usedGb)
		assertEquals(40.0, network.quotaGb)
		assertEquals(0.32, network.usedRatio)
		assertEquals(CampusNetwork.AccountStatus.NORMAL, network.status)
		assertEquals(0.0, network.overdueYuan)
	}

	@Test
	fun `locked account and overdue payment both survive the mapping`() {
		val dto = NetflowDto(
			thisMonth = NetflowMonthDto(allBasePackageAmount = 40.0, basePackageUsed = 41.0, basePackageUsedPer = 1.025),
			unlock = "已锁定",
			payInfo = 12.5,
		)

		val network = dto.toEntity()

		assertEquals(CampusNetwork.AccountStatus.LOCKED, network.status)
		assertEquals(12.5, network.overdueYuan)
		// 超用 2.5% 仍然是合法的小数比例，不能被当成百分数再除一百
		assertEquals(1.025, network.usedRatio)
	}

	@Test
	fun `ratio given as a whole percentage is normalized`() {
		val dto = NetflowDto(
			thisMonth = NetflowMonthDto(allBasePackageAmount = 40.0, basePackageUsed = 12.8, basePackageUsedPer = 32.0),
			unlock = "未知状态",
		)

		val network = dto.toEntity()

		assertEquals(0.32, network.usedRatio)
		assertEquals(CampusNetwork.AccountStatus.UNKNOWN, network.status)
	}

	@Test
	fun `missing ratio falls back to used over quota`() {
		val dto = NetflowDto(
			thisMonth = NetflowMonthDto(allBasePackageAmount = 40.0, basePackageUsed = 10.0, basePackageUsedPer = 0.0),
		)

		assertEquals(0.25, dto.toEntity().usedRatio)
	}

	@Test
	fun `card info keeps the balance and ignores the account number`() {
		val text = """{"code":200,"data":{"account":368246,"balance":86.5,"unknownField":"忽略"}}"""

		val card = unwrapEnvelope<CardInfoDto>(text).toEntity()

		assertEquals(86.5, card.balanceYuan)
	}

	@Test
	fun `electricity string is parsed into degrees and keeps its decimals`() {
		assertEquals(186.5, parseDormElectricity("186.50度").remainingKwh)
		assertEquals(0.0, parseDormElectricity("0度").remainingKwh)
	}

	@Test
	fun `electricity without a unit is still a number`() {
		assertEquals(79.6, parseDormElectricity("79.6").remainingKwh)
	}

	@Test
	fun `electricity text without any number is a gateway failure`() {
		val error = assertFailsWith<GatewayException> { parseDormElectricity("暂无数据") }

		assertEquals(-1, error.code)
	}
}
