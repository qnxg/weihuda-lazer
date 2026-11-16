// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0
package cn.qnxg.weihuda.data.remote

import cn.qnxg.weihuda.core.port.GatewayException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

/**
 * 绑定接口要求前端先加密密码：RSA-2048 / PKCS#1 v1.5，密文再 Base64。
 *
 * 公钥与 `weihuda_weapp/src/utils/encrypt.ts` 里的是同一把；私钥只在后端，
 * 所以密码在传输链路上不是明文。
 */
internal object FrontendRsa {
	fun encrypt(plain: String): String = try {
		val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
		cipher.init(Cipher.ENCRYPT_MODE, publicKey)
		Base64.getEncoder().encodeToString(cipher.doFinal(plain.toByteArray(Charsets.UTF_8)))
	} catch (error: Exception) {
		throw GatewayException(code = -1, message = "无法加密密码：${error.message ?: "未知错误"}")
	}

	private val publicKey: PublicKey by lazy {
		val pem = PEM.lineSequence()
			.filterNot { it.startsWith("-----") }
			.joinToString("")
		KeyFactory.getInstance("RSA")
			.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(pem)))
	}

	private const val PEM: String = """
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Z0KfeZ0d5zXaL0LvCKP
o6FtvbUeOE28y1k7OWnMNAkkkKrsUAlgVSWxBjUXUgsz24J6Ce4F0vmVlJjrpQnF
/08vlUKu/3F3nN56wYzxNXar177ms0qQ9pZYR3d3kHjWAfq8IWpyJqgbJpa+QcJP
IYjVN0ZL/wQ7yDV19zCTC7uqZsjoqgXT2oyutjCCfUn9a6PNOthtPS5TjazhOVSq
C5GA8kvKKPuMPjGoX4ANdtaikfpSaw0WF4EiViJJ3PEl7fEw35vzE4A3notWUuTh
5E4NN28Re4oD3ZB5lmL32lkJRgjUAdZgnbi5SBHsNRoGu+QGKrFOCmyqMpoDKIwU
BwIDAQAB
-----END PUBLIC KEY-----
"""
}
