package me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * 업비트 시세 티커 응답
 */
data class UpbitTicker(
    /** 마켓 코드 */
    @JsonProperty("market")
    val market: String,

    /** 현재가 */
    @JsonProperty("trade_price")
    val tradePrice: BigDecimal,

    /** 고가 */
    @JsonProperty("high_price")
    val highPrice: BigDecimal,

    /** 저가 */
    @JsonProperty("low_price")
    val lowPrice: BigDecimal,

    /** 시가 */
    @JsonProperty("opening_price")
    val openingPrice: BigDecimal,

    /** 전일 종가 */
    @JsonProperty("prev_closing_price")
    val prevClosingPrice: BigDecimal,

    /** 변화율 */
    @JsonProperty("signed_change_rate")
    val signedChangeRate: BigDecimal,

    /** 24시간 누적 거래량 */
    @JsonProperty("acc_trade_volume_24h")
    val accTradeVolume24h: BigDecimal,

    /** 타임스탬프 (밀리초) */
    @JsonProperty("timestamp")
    val timestamp: Long
)
