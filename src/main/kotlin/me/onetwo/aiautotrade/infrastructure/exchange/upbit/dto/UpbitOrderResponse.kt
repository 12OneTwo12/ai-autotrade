package me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * 업비트 주문 응답
 */
data class UpbitOrderResponse(
    /** 주문 고유 ID */
    @JsonProperty("uuid")
    val uuid: String,

    /** 주문 종류 */
    @JsonProperty("side")
    val side: String,

    /** 주문 타입 */
    @JsonProperty("ord_type")
    val ordType: String,

    /** 주문 가격 */
    @JsonProperty("price")
    val price: BigDecimal?,

    /** 주문 상태 */
    @JsonProperty("state")
    val state: String,

    /** 마켓 코드 */
    @JsonProperty("market")
    val market: String,

    /** 주문 생성 시각 */
    @JsonProperty("created_at")
    val createdAt: String,

    /** 주문량 */
    @JsonProperty("volume")
    val volume: BigDecimal?,

    /** 남은 주문량 */
    @JsonProperty("remaining_volume")
    val remainingVolume: BigDecimal?,

    /** 예약 주문량 */
    @JsonProperty("reserved_fee")
    val reservedFee: BigDecimal?,

    /** 남은 수수료 */
    @JsonProperty("remaining_fee")
    val remainingFee: BigDecimal?,

    /** 사용된 수수료 */
    @JsonProperty("paid_fee")
    val paidFee: BigDecimal?,

    /** 잠긴 금액 */
    @JsonProperty("locked")
    val locked: BigDecimal?,

    /** 체결된 양 */
    @JsonProperty("executed_volume")
    val executedVolume: BigDecimal?,

    /** 체결된 거래 수 */
    @JsonProperty("trades_count")
    val tradesCount: Int?
)
