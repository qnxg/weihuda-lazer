package cn.qnxg.weihuda.core.entity

/**
 * 教务处课表里的一条课程记录，即「某门课在某天的某一小节」。
 *
 * 后端每小节一条记录，连续小节各有一条；把连续小节合并成一节课是展示规则，
 * 由 [CourseSession] 负责，实体本身保持与教务数据一一对应。
 *
 * - [dayOfWeek]：周一为 1 … 周日为 7（后端历史版本出现过 0 表示周日，读取时用
 *   [normalizeDayOfWeek] 兜底）
 * - [period]：小节次 1…12
 * - [weeks]：上课周次，例如 `[1, 2, 3]`
 * - [customizeId]：自定义课程为数据库主键，教务课程为 -1
 */
data class Course(
	val name: String,
	val code: String? = null,
	val kind: String = "",
	val className: String? = null,
	val place: String? = null,
	val campus: String? = null,
	val teacher: String? = null,
	val weeks: List<Int> = emptyList(),
	val dayOfWeek: Int = 1,
	val period: Int = 1,
	val credits: Float? = null,
	val note: String? = null,
	val customizeId: Int = -1,
	val students: Int = 0,
)

/** 后端用 0 和 7 都表示周日，这里统一成「周一 = 1 … 周日 = 7」。 */
fun normalizeDayOfWeek(day: Int): Int = when (day) {
	0, 7 -> 7
	else -> day.coerceIn(1, 7)
}
