package me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * 업비트 주문 요청
 */
data class UpbitOrderRequest(
    /** 마켓 코드 (필수) */
    @JsonProperty("market")
    val market: String,

    /** 주문 종류 (필수) - bid: 매수, ask: 매도 */
    @JsonProperty("side")
    val side: String,

    /** 주문량 (지정가, 시장가 매도 시 필수) */
    @JsonProperty("volume")
    val volume: BigDecimal? = null,

    /** 주문 가격 (지정가 시 필수) */
    @JsonProperty("price")
    val price: BigDecimal? = null,

    /** 주문 타입 (필수) - limit: 지정가, price: 시장가 매수, market: 시장가 매도 */
    @JsonProperty("ord_type")
    val ordType: String
)
