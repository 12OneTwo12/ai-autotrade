package me.onetwo.aiautotrade.infrastructure.exchange

import me.onetwo.aiautotrade.common.dto.ApiKeyInfo
import me.onetwo.aiautotrade.infrastructure.security.EncryptionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * API 키 관리 서비스 구현체
 *
 * API 키를 암호화하여 저장하고 관리합니다.
 * 현재는 메모리에 저장하며, 추후 데이터베이스 연동 시 Repository로 대체 가능합니다.
 */
@Service
class DefaultApiKeyService(
    private val encryptionService: EncryptionService
) : ApiKeyService {

    private val logger = LoggerFactory.getLogger(DefaultApiKeyService::class.java)

    // 임시 메모리 저장소 (추후 데이터베이스로 대체)
    private val apiKeyStorage = ConcurrentHashMap<String, ApiKeyInfo>()
    private val idGenerator = AtomicLong(1)

    override fun saveApiKey(
        userId: Long,
        exchangeName: String,
        accessKey: String,
        secretKey: String
    ): ApiKeyInfo {
        logger.info("Saving API key for user: $userId, exchange: $exchangeName")

        val encryptedAccessKey = encryptionService.encrypt(accessKey)
        val encryptedSecretKey = encryptionService.encrypt(secretKey)

        val apiKeyInfo = ApiKeyInfo(
            id = idGenerator.getAndIncrement(),
            userId = userId,
            exchangeName = exchangeName,
            accessKey = encryptedAccessKey,
            secretKey = encryptedSecretKey,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val key = generateKey(userId, exchangeName)
        apiKeyStorage[key] = apiKeyInfo

        logger.info("API key saved successfully for user: $userId, exchange: $exchangeName")
        return apiKeyInfo.copy(
            accessKey = accessKey,
            secretKey = secretKey
        )
    }

    override fun getApiKey(userId: Long, exchangeName: String): ApiKeyInfo? {
        logger.debug("Retrieving API key for user: $userId, exchange: $exchangeName")

        val key = generateKey(userId, exchangeName)
        val apiKeyInfo = apiKeyStorage[key] ?: return null

        return apiKeyInfo.copy(
            accessKey = encryptionService.decrypt(apiKeyInfo.accessKey),
            secretKey = encryptionService.decrypt(apiKeyInfo.secretKey)
        )
    }

    override fun getAllApiKeys(userId: Long): List<ApiKeyInfo> {
        logger.debug("Retrieving all API keys for user: $userId")

        return apiKeyStorage.values
            .filter { it.userId == userId }
            .map { apiKeyInfo ->
                apiKeyInfo.copy(
                    accessKey = encryptionService.decrypt(apiKeyInfo.accessKey),
                    secretKey = encryptionService.decrypt(apiKeyInfo.secretKey)
                )
            }
    }

    override fun deleteApiKey(userId: Long, exchangeName: String) {
        logger.info("Deleting API key for user: $userId, exchange: $exchangeName")

        val key = generateKey(userId, exchangeName)
        apiKeyStorage.remove(key)

        logger.info("API key deleted successfully for user: $userId, exchange: $exchangeName")
    }

    override fun updateApiKey(
        userId: Long,
        exchangeName: String,
        accessKey: String,
        secretKey: String
    ): ApiKeyInfo {
        logger.info("Updating API key for user: $userId, exchange: $exchangeName")

        val key = generateKey(userId, exchangeName)
        val existingApiKey = apiKeyStorage[key]
            ?: throw IllegalArgumentException("API key not found for user: $userId, exchange: $exchangeName")

        val encryptedAccessKey = encryptionService.encrypt(accessKey)
        val encryptedSecretKey = encryptionService.encrypt(secretKey)

        val updatedApiKeyInfo = existingApiKey.copy(
            accessKey = encryptedAccessKey,
            secretKey = encryptedSecretKey,
            updatedAt = LocalDateTime.now()
        )

        apiKeyStorage[key] = updatedApiKeyInfo

        logger.info("API key updated successfully for user: $userId, exchange: $exchangeName")
        return updatedApiKeyInfo.copy(
            accessKey = accessKey,
            secretKey = secretKey
        )
    }

    /**
     * 저장소 키 생성
     */
    private fun generateKey(userId: Long, exchangeName: String): String {
        return "$userId:$exchangeName"
    }
}
