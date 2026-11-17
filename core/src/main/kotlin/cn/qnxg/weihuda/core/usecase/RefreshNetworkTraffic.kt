// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.core.usecase

/**
 * 输入端口：刷新「校园网流量」快照。
 *
 * 界面、Worker、测试都只认这个接口，谁也看不见适配器。
 */
interface RefreshNetworkTraffic {
	/**
	 * 取一次流量并写入快照，返回写进去的内容。
	 *
	 * 失败被消化成带 [NetworkTraffic.failure] 的正常返回，不抛异常——调用方是后台任务。
	 */
	suspend fun refresh(): NetworkTraffic
}
