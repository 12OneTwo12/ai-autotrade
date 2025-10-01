package me.onetwo.aiautotrade.infrastructure.security

/**
 * 암호화 서비스 인터페이스
 *
 * 민감한 정보(API 키 등)를 암호화하고 복호화합니다.
 */
interface EncryptionService {

    /**
     * 텍스트를 암호화합니다.
     *
     * @param plainText 평문
     * @return 암호화된 텍스트 (Base64 인코딩)
     */
    fun encrypt(plainText: String): String

    /**
     * 암호화된 텍스트를 복호화합니다.
     *
     * @param encryptedText 암호화된 텍스트 (Base64 인코딩)
     * @return 평문
     */
    fun decrypt(encryptedText: String): String
}
