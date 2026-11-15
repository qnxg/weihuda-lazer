package cn.qnxg.weihuda.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 后端统一响应信封：`{ "code": 200, "data": ..., "msg": "请求成功" }`
 */
@Serializable
data class ApiEnvelope<T>(
    val code: Int = 0,
    val data: T? = null,
    val msg: String? = null,
)

/**
 * `GET /info/smester`
 *
 * `startDate` 是当前学期第一周的周日，格式 `yyyy-MM-dd`。
 */
@Serializable
data class SemesterInfo(
    val startDate: String,
    val term: Int,
    val year: Int,
    val vacation: String? = null,
    val next: String? = null,
)

/**
 * `GET /hdjw/class-table` 返回的单条课程记录。
 *
 * - [day]：周一为 1 … 周日为 7（后端历史版本也曾出现过 0 表示周日，读取时做兜底）
 * - [time]：小节次 1…12，同一课程连续小节各自一条记录
 * - [customizeId]：自定义课程为数据库主键，教务课程为 -1
 */
@Serializable
data class CourseItem(
    val courseName: String,
    val courseId: String? = null,
    @SerialName("type") val type: String = "",
    val className: String? = null,
    val place: String? = null,
    val area: String? = null,
    val teacher: String? = null,
    val weeks: List<Int> = emptyList(),
    val day: Int = 1,
    val time: Int = 1,
    val credit: Float? = null,
    val extra: String? = null,
    val customizeId: Int = -1,
    val people: Int = 0,
)

/** `GET /info/user` */
@Serializable
data class UserInfo(
    val name: String? = null,
    @SerialName("stuId") val stuId: String? = null,
    val college: String? = null,
    val major: String? = null,
    @SerialName("class") val className: String? = null,
)
