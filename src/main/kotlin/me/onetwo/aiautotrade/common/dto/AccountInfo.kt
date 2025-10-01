package me.onetwo.aiautotrade.common.dto

import java.math.BigDecimal

/**
 * 거래소 계정 정보
 *
 * @property balances 보유 자산 목록
 * @property totalBalance 총 자산 가치 (KRW 기준)
 */
data class AccountInfo(
    val balances: List<Balance>,
    val totalBalance: BigDecimal
)

/**
 * 보유 자산 정보
 *
 * @property currency 통화 코드 (예: KRW, BTC, ETH)
 * @property balance 보유 수량
 * @property locked 주문 중으로 묶여있는 수량
 * @property avgBuyPrice 평균 매수 가격
 */
data class Balance(
    val currency: String,
    val balance: BigDecimal,
    val locked: BigDecimal,
    val avgBuyPrice: BigDecimal? = null
)
