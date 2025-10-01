package me.onetwo.aiautotrade.common.dto

import me.onetwo.aiautotrade.common.enums.OrderSide
import me.onetwo.aiautotrade.common.enums.OrderState
import me.onetwo.aiautotrade.common.enums.OrderType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 결과 정보
 *
 * @property orderId 주문 고유 ID
 * @property market 마켓 코드
 * @property side 주문 종류
 * @property orderType 주문 타입
 * @property price 주문 가격
 * @property volume 주문 수량
 * @property executedVolume 체결된 수량
 * @property remainingVolume 미체결 수량
 * @property state 주문 상태
 * @property createdAt 주문 생성 시각
 */
data class OrderResult(
    val orderId: String,
    val market: String,
    val side: OrderSide,
    val orderType: OrderType,
    val price: BigDecimal?,
    val volume: BigDecimal,
    val executedVolume: BigDecimal,
    val remainingVolume: BigDecimal,
    val state: OrderState,
    val createdAt: LocalDateTime
)
