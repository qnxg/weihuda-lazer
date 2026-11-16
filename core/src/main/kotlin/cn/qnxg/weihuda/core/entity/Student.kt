// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.core.entity

/** 当前登录的学生，来自 `GET /info/user`。 */
data class Student(
	val name: String? = null,
	val studentId: String? = null,
	val college: String? = null,
	val major: String? = null,
	val className: String? = null,
)
