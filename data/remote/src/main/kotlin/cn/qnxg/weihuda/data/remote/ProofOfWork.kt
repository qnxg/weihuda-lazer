// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.port.GatewayException
import java.security.MessageDigest

/**
 * 后端绑定的工作量证明：`sha256("<ticket>:<answer>")` 的十六进制要以 [difficulty] 个 0 开头。
 *
 * 难度取自后端下发值而不是常量——它在 `config.toml` 里可调，写死会在服务端调高后让登录永远失败。
 * 上限用来兜住配置写错（比如难度 8）导致的长时间空转。
 */
internal fun solveProofOfWork(ticket: String, difficulty: Int): Long {
	val prefix = "0".repeat(difficulty.coerceAtLeast(0))
	val digest = MessageDigest.getInstance("SHA-256")
	var answer = 0L
	while (answer < MAX_ATTEMPTS) {
		if (hexOf(digest.digest("$ticket:$answer".toByteArray())).startsWith(prefix)) {
			return answer
		}
		answer++
	}
	throw GatewayException(code = -1, message = "工作量证明失败：难度配置过高")
}

internal fun hexOf(bytes: ByteArray): String {
	val table = "0123456789abcdef"
	val out = StringBuilder(bytes.size * 2)
	for (byte in bytes) {
		val value = byte.toInt() and 0xff
		out.append(table[value ushr 4]).append(table[value and 0x0f])
	}
	return out.toString()
}

private const val MAX_ATTEMPTS: Long = 50_000_000
