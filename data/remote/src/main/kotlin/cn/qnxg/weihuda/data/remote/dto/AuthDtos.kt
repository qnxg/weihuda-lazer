package cn.qnxg.weihuda.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /bind/pow?stuId=` */
@Serializable
internal data class PowTicketDto(
	val ticket: String,
	val difficulty: Int = 0,
)

/** `POST /bind` */
@Serializable
internal data class BindRequestDto(
	val powTicket: String,
	val powAnswer: Long,
	val code: String,
	val stuId: String,
	val password: String,
)
