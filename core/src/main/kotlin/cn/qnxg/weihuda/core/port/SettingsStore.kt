// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.port

import cn.qnxg.weihuda.core.entity.Settings

/**
 * 应用设置的读写端口。
 *
 * 令牌在这里已经是明文：加密是 `data:local` 的细节，用例与界面只认 [Settings]。
 */
interface SettingsStore {
	suspend fun load(): Settings

	suspend fun save(settings: Settings)
}
