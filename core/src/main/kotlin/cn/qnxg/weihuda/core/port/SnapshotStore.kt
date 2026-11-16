// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.port

import cn.qnxg.weihuda.core.entity.WidgetId
import cn.qnxg.weihuda.core.usecase.TodayCourses

/**
 * 小组件快照的读写端口。
 *
 * 快照是「取数」与「渲染」之间唯一的接口：刷新把结果写成快照，桌面只读快照，
 * 于是刷新失败、没有网络、令牌过期时桌面都不会空白。序列化格式是适配器的细节。
 */
interface SnapshotStore {
	suspend fun readTodayCourses(id: WidgetId): TodayCourses?

	suspend fun writeTodayCourses(id: WidgetId, snapshot: TodayCourses)
}
