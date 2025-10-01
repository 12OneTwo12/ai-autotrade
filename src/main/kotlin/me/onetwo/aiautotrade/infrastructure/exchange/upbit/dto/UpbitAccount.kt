package me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * 업비트 계좌 정보 응답
 */
data class UpbitAccount(
    /** 화폐 코드 */
    @JsonProperty("currency")
    val currency: String,

    /** 주문 가능 금액/수량 */
    @JsonProperty("balance")
    val balance: BigDecimal,

    /** 주문 중 묶여있는 금액/수량 */
    @JsonProperty("locked")
    val locked: BigDecimal,

    /** 매수 평균가 */
    @JsonProperty("avg_buy_price")
    val avgBuyPrice: BigDecimal,

    /** 매수/매도 가능 여부 */
    @JsonProperty("avg_buy_price_modified")
    val avgBuyPriceModified: Boolean,

    /** 평단가 기준 화폐 */
    @JsonProperty("unit_currency")
    val unitCurrency: String
)
