package me.onetwo.aiautotrade.infrastructure.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AesEncryptionServiceTest {

    private lateinit var encryptionService: AesEncryptionService

    @BeforeEach
    fun setUp() {
        val encryptionKey = "0123456789abcdef0123456789abcdef" // 32 characters
        val initVector = "fedcba9876543210" // 16 characters
        encryptionService = AesEncryptionService(encryptionKey, initVector)
    }

    @Test
    fun `encrypt should return different string from plain text`() {
        // given
        val plainText = "my-secret-api-key"

        // when
        val encrypted = encryptionService.encrypt(plainText)

        // then
        assertNotEquals(plainText, encrypted)
        assertTrue(encrypted.isNotEmpty())
    }

    @Test
    fun `decrypt should return original plain text`() {
        // given
        val plainText = "my-secret-api-key"
        val encrypted = encryptionService.encrypt(plainText)

        // when
        val decrypted = encryptionService.decrypt(encrypted)

        // then
        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work for long strings`() {
        // given
        val plainText = "a".repeat(1000)

        // when
        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        // then
        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work for special characters`() {
        // given
        val plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?"

        // when
        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        // then
        assertEquals(plainText, decrypted)
    }

    @Test
    fun `same plain text should produce same encrypted text`() {
        // given
        val plainText = "my-secret-api-key"

        // when
        val encrypted1 = encryptionService.encrypt(plainText)
        val encrypted2 = encryptionService.encrypt(plainText)

        // then
        assertEquals(encrypted1, encrypted2)
    }

    @Test
    fun `should throw exception for invalid encryption key length`() {
        // given
        val invalidKey = "short-key"
        val validIv = "fedcba9876543210"

        // when & then
        assertThrows<IllegalArgumentException> {
            AesEncryptionService(invalidKey, validIv)
        }
    }

    @Test
    fun `should throw exception for invalid initialization vector length`() {
        // given
        val validKey = "0123456789abcdef0123456789abcdef"
        val invalidIv = "short"

        // when & then
        assertThrows<IllegalArgumentException> {
            AesEncryptionService(validKey, invalidIv)
        }
    }
}
