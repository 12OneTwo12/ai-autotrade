package me.onetwo.aiautotrade.common.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 시장 가격 정보
 *
 * @property market 마켓 코드
 * @property tradePrice 현재가
 * @property highPrice 고가
 * @property lowPrice 저가
 * @property openPrice 시가
 * @property prevClosingPrice 전일 종가
 * @property changeRate 변화율
 * @property accTradeVolume24h 24시간 누적 거래량
 * @property timestamp 시각
 */
data class MarketPrice(
    val market: String,
    val tradePrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val openPrice: BigDecimal,
    val prevClosingPrice: BigDecimal,
    val changeRate: BigDecimal,
    val accTradeVolume24h: BigDecimal,
    val timestamp: LocalDateTime
)
