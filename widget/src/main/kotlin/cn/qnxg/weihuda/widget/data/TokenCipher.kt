package cn.qnxg.weihuda.widget.data

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * 用 Android Keystore 里的 AES-256-GCM 密钥加密访问令牌。
 *
 * 不使用已被废弃的 `androidx.security:security-crypto`，直接走平台 Keystore API。
 */
object TokenCipher {
	private const val ALIAS = "weihuda_lazer_token"
	private const val KEYSTORE = "AndroidKeyStore"
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val IV_BYTES = 12
	private const val TAG_BITS = 128

	fun encrypt(plain: String): String {
		if (plain.isEmpty()) return ""
		return runCatching {
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.ENCRYPT_MODE, secretKey())
			val payload = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
			Base64.encodeToString(payload, Base64.NO_WRAP)
		}.getOrDefault("")
	}

	fun decrypt(payload: String): String {
		if (payload.isEmpty()) return ""
		return runCatching {
			val bytes = Base64.decode(payload, Base64.NO_WRAP)
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(
				Cipher.DECRYPT_MODE,
				secretKey(),
				GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES),
			)
			String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
		}.getOrDefault("")
	}

	private fun secretKey(): SecretKey {
		val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
		(keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

		val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
		generator.init(
			KeyGenParameterSpec.Builder(
				ALIAS,
				KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
			)
				.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
				.setKeySize(256)
				.build(),
		)
		return generator.generateKey()
	}
}
