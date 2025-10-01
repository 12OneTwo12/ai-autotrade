package me.onetwo.aiautotrade.common.dto

import java.time.LocalDateTime

/**
 * API 키 정보
 *
 * @property id API 키 ID
 * @property userId 사용자 ID
 * @property exchangeName 거래소 이름 (예: upbit, binance)
 * @property accessKey Access Key (암호화됨)
 * @property secretKey Secret Key (암호화됨)
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 */
data class ApiKeyInfo(
    val id: Long? = null,
    val userId: Long,
    val exchangeName: String,
    val accessKey: String,
    val secretKey: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
