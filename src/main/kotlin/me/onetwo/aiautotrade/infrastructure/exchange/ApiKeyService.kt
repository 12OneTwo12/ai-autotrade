package me.onetwo.aiautotrade.infrastructure.exchange

import me.onetwo.aiautotrade.common.dto.ApiKeyInfo

/**
 * API 키 관리 서비스 인터페이스
 */
interface ApiKeyService {

    /**
     * API 키를 저장합니다.
     *
     * @param userId 사용자 ID
     * @param exchangeName 거래소 이름
     * @param accessKey Access Key (평문)
     * @param secretKey Secret Key (평문)
     * @return 저장된 API 키 정보
     */
    fun saveApiKey(
        userId: Long,
        exchangeName: String,
        accessKey: String,
        secretKey: String
    ): ApiKeyInfo

    /**
     * 사용자의 특정 거래소 API 키를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param exchangeName 거래소 이름
     * @return API 키 정보 (복호화됨), 없으면 null
     */
    fun getApiKey(userId: Long, exchangeName: String): ApiKeyInfo?

    /**
     * 사용자의 모든 API 키를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return API 키 목록 (복호화됨)
     */
    fun getAllApiKeys(userId: Long): List<ApiKeyInfo>

    /**
     * API 키를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param exchangeName 거래소 이름
     */
    fun deleteApiKey(userId: Long, exchangeName: String)

    /**
     * API 키를 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param exchangeName 거래소 이름
     * @param accessKey 새로운 Access Key (평문)
     * @param secretKey 새로운 Secret Key (평문)
     * @return 업데이트된 API 키 정보
     */
    fun updateApiKey(
        userId: Long,
        exchangeName: String,
        accessKey: String,
        secretKey: String
    ): ApiKeyInfo
}
