package me.onetwo.aiautotrade.infrastructure.exchange

import me.onetwo.aiautotrade.common.dto.AccountInfo
import me.onetwo.aiautotrade.common.dto.MarketPrice
import me.onetwo.aiautotrade.common.dto.OrderResult
import java.util.concurrent.CompletableFuture

/**
 * 거래소 서비스 인터페이스
 *
 * 거래소와의 모든 상호작용을 담당합니다.
 */
interface ExchangeService {

    /**
     * 계정 정보를 조회합니다.
     *
     * @return 보유 자산 및 잔고 정보
     */
    fun getAccountInfo(): CompletableFuture<AccountInfo>

    /**
     * 특정 마켓의 현재 가격 정보를 조회합니다.
     *
     * @param market 마켓 코드 (예: KRW-BTC)
     * @return 시장 가격 정보
     */
    fun getMarketPrice(market: String): CompletableFuture<MarketPrice>

    /**
     * 매수 주문을 실행합니다.
     *
     * @param market 마켓 코드
     * @param price 주문 가격
     * @param volume 주문 수량
     * @return 주문 결과
     */
    fun buy(market: String, price: java.math.BigDecimal?, volume: java.math.BigDecimal?): CompletableFuture<OrderResult>

    /**
     * 매도 주문을 실행합니다.
     *
     * @param market 마켓 코드
     * @param price 주문 가격
     * @param volume 주문 수량
     * @return 주문 결과
     */
    fun sell(market: String, price: java.math.BigDecimal?, volume: java.math.BigDecimal?): CompletableFuture<OrderResult>

    /**
     * 주문을 취소합니다.
     *
     * @param orderId 주문 ID
     * @return 취소된 주문 정보
     */
    fun cancelOrder(orderId: String): CompletableFuture<OrderResult>

    /**
     * 주문 상태를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 주문 정보
     */
    fun getOrderStatus(orderId: String): CompletableFuture<OrderResult>
}
