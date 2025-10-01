package me.onetwo.aiautotrade.infrastructure.exchange

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.onetwo.aiautotrade.infrastructure.security.EncryptionService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DefaultApiKeyServiceTest {

    private lateinit var encryptionService: EncryptionService
    private lateinit var apiKeyService: ApiKeyService

    @BeforeEach
    fun setUp() {
        encryptionService = mockk()
        apiKeyService = DefaultApiKeyService(encryptionService)
    }

    @Test
    fun `saveApiKey should encrypt and store API keys`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"
        val accessKey = "my-access-key"
        val secretKey = "my-secret-key"
        val encryptedAccessKey = "encrypted-access-key"
        val encryptedSecretKey = "encrypted-secret-key"

        every { encryptionService.encrypt(accessKey) } returns encryptedAccessKey
        every { encryptionService.encrypt(secretKey) } returns encryptedSecretKey

        // when
        val result = apiKeyService.saveApiKey(userId, exchangeName, accessKey, secretKey)

        // then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(exchangeName, result.exchangeName)
        assertEquals(accessKey, result.accessKey) // 반환값은 평문
        assertEquals(secretKey, result.secretKey) // 반환값은 평문

        verify { encryptionService.encrypt(accessKey) }
        verify { encryptionService.encrypt(secretKey) }
    }

    @Test
    fun `getApiKey should decrypt and return API keys`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"
        val accessKey = "my-access-key"
        val secretKey = "my-secret-key"
        val encryptedAccessKey = "encrypted-access-key"
        val encryptedSecretKey = "encrypted-secret-key"

        every { encryptionService.encrypt(accessKey) } returns encryptedAccessKey
        every { encryptionService.encrypt(secretKey) } returns encryptedSecretKey
        every { encryptionService.decrypt(encryptedAccessKey) } returns accessKey
        every { encryptionService.decrypt(encryptedSecretKey) } returns secretKey

        apiKeyService.saveApiKey(userId, exchangeName, accessKey, secretKey)

        // when
        val result = apiKeyService.getApiKey(userId, exchangeName)

        // then
        assertNotNull(result)
        assertEquals(userId, result!!.userId)
        assertEquals(exchangeName, result.exchangeName)
        assertEquals(accessKey, result.accessKey)
        assertEquals(secretKey, result.secretKey)

        verify { encryptionService.decrypt(encryptedAccessKey) }
        verify { encryptionService.decrypt(encryptedSecretKey) }
    }

    @Test
    fun `getApiKey should return null for non-existent key`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"

        // when
        val result = apiKeyService.getApiKey(userId, exchangeName)

        // then
        assertNull(result)
    }

    @Test
    fun `updateApiKey should update existing API keys`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"
        val oldAccessKey = "old-access-key"
        val oldSecretKey = "old-secret-key"
        val newAccessKey = "new-access-key"
        val newSecretKey = "new-secret-key"

        every { encryptionService.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
        every { encryptionService.decrypt(any()) } answers { (firstArg() as String).removePrefix("encrypted-") }

        apiKeyService.saveApiKey(userId, exchangeName, oldAccessKey, oldSecretKey)

        // when
        val result = apiKeyService.updateApiKey(userId, exchangeName, newAccessKey, newSecretKey)

        // then
        assertNotNull(result)
        assertEquals(newAccessKey, result.accessKey)
        assertEquals(newSecretKey, result.secretKey)
    }

    @Test
    fun `updateApiKey should throw exception for non-existent key`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"
        val accessKey = "access-key"
        val secretKey = "secret-key"

        // when & then
        assertThrows<IllegalArgumentException> {
            apiKeyService.updateApiKey(userId, exchangeName, accessKey, secretKey)
        }
    }

    @Test
    fun `deleteApiKey should remove API keys`() {
        // given
        val userId = 1L
        val exchangeName = "upbit"
        val accessKey = "my-access-key"
        val secretKey = "my-secret-key"

        every { encryptionService.encrypt(any()) } returns "encrypted"

        apiKeyService.saveApiKey(userId, exchangeName, accessKey, secretKey)

        // when
        apiKeyService.deleteApiKey(userId, exchangeName)
        val result = apiKeyService.getApiKey(userId, exchangeName)

        // then
        assertNull(result)
    }

    @Test
    fun `getAllApiKeys should return all keys for user`() {
        // given
        val userId = 1L
        val exchange1 = "upbit"
        val exchange2 = "binance"

        every { encryptionService.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
        every { encryptionService.decrypt(any()) } answers { (firstArg() as String).removePrefix("encrypted-") }

        apiKeyService.saveApiKey(userId, exchange1, "access1", "secret1")
        apiKeyService.saveApiKey(userId, exchange2, "access2", "secret2")

        // when
        val result = apiKeyService.getAllApiKeys(userId)

        // then
        assertEquals(2, result.size)
        assertTrue(result.any { it.exchangeName == exchange1 })
        assertTrue(result.any { it.exchangeName == exchange2 })
    }

    @Test
    fun `getAllApiKeys should return only keys for specific user`() {
        // given
        val userId1 = 1L
        val userId2 = 2L
        val exchangeName = "upbit"

        every { encryptionService.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
        every { encryptionService.decrypt(any()) } answers { (firstArg() as String).removePrefix("encrypted-") }

        apiKeyService.saveApiKey(userId1, exchangeName, "access1", "secret1")
        apiKeyService.saveApiKey(userId2, exchangeName, "access2", "secret2")

        // when
        val result = apiKeyService.getAllApiKeys(userId1)

        // then
        assertEquals(1, result.size)
        assertEquals(userId1, result[0].userId)
    }
}
