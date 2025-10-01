package me.onetwo.aiautotrade.common.enums

/**
 * 주문 타입
 */
enum class OrderType {
    /** 지정가 주문 */
    LIMIT,

    /** 시장가 주문 (매수) */
    PRICE,

    /** 시장가 주문 (매도) */
    MARKET
}
