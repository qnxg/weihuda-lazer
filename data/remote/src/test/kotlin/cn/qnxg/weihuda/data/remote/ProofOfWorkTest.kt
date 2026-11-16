package cn.qnxg.weihuda.data.remote

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** PoW 求解必须和后端的校验规则一致，否则登录会被服务端判为“pow 验证失败”。 */
class ProofOfWorkTest {

	@Test
	fun `solved answer produces the zero prefix the server requires`() {
		val ticket = "6d1f8a2c4b7e4f0a9c3d5e7b1a2c3d4e"
		val difficulty = 4

		val answer = solveProofOfWork(ticket, difficulty)
		val hash = hexOf(
			MessageDigest.getInstance("SHA-256").digest("$ticket:$answer".toByteArray()),
		)

		assertTrue(hash.startsWith("0".repeat(difficulty)), "实际哈希 $hash")
	}

	@Test
	fun `zero difficulty accepts the first answer`() {
		assertEquals(0L, solveProofOfWork("any-ticket", 0))
	}

	@Test
	fun `negative difficulty is treated as no requirement`() {
		assertEquals(0L, solveProofOfWork("any-ticket", -3))
	}

	@Test
	fun `hex helper keeps leading zeroes`() {
		assertEquals("000f", hexOf(byteArrayOf(0x00, 0x0f)))
	}
}
