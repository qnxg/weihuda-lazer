package cn.qnxg.weihuda.core.entity

/** 当前登录的学生，来自 `GET /info/user`。 */
data class Student(
	val name: String? = null,
	val studentId: String? = null,
	val college: String? = null,
	val major: String? = null,
	val className: String? = null,
)
