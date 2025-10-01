package me.onetwo.aiautotrade.common.dto

import me.onetwo.aiautotrade.common.enums.OrderSide
import me.onetwo.aiautotrade.common.enums.OrderType
import java.math.BigDecimal

/**
 * 주문 요청 정보
 *
 * @property market 마켓 코드 (예: KRW-BTC)
 * @property side 주문 종류 (매수/매도)
 * @property orderType 주문 타입 (지정가/시장가)
 * @property price 주문 가격 (지정가 주문 시)
 * @property volume 주문 수량
 */
data class OrderRequest(
    val market: String,
    val side: OrderSide,
    val orderType: OrderType,
    val price: BigDecimal? = null,
    val volume: BigDecimal? = null
)
