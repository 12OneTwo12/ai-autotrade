package me.onetwo.aiautotrade.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES 암호화 서비스 구현체
 *
 * AES-256-CBC 알고리즘을 사용하여 데이터를 암호화/복호화합니다.
 */
@Service
class AesEncryptionService(
    @Value("\${encryption.key:0123456789abcdef0123456789abcdef}") private val encryptionKey: String,
    @Value("\${encryption.iv:fedcba9876543210}") private val initVector: String
) : EncryptionService {

    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5PADDING"
        private const val CHARSET = "UTF-8"
    }

    init {
        require(encryptionKey.length == 32) {
            "Encryption key must be 32 characters (256 bits) long"
        }
        require(initVector.length == 16) {
            "Initialization vector must be 16 characters (128 bits) long"
        }
    }

    override fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(encryptionKey.toByteArray(charset(CHARSET)), "AES")
        val ivSpec = IvParameterSpec(initVector.toByteArray(charset(CHARSET)))

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val encrypted = cipher.doFinal(plainText.toByteArray(charset(CHARSET)))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    override fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(encryptionKey.toByteArray(charset(CHARSET)), "AES")
        val ivSpec = IvParameterSpec(initVector.toByteArray(charset(CHARSET)))

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decodedValue = Base64.getDecoder().decode(encryptedText)
        val decrypted = cipher.doFinal(decodedValue)
        return String(decrypted, charset(CHARSET))
    }
}
