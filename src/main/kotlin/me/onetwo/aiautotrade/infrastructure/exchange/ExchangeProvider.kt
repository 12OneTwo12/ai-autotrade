package me.onetwo.aiautotrade.infrastructure.exchange

import me.onetwo.aiautotrade.common.dto.AccountInfo
import me.onetwo.aiautotrade.common.dto.MarketPrice
import me.onetwo.aiautotrade.common.dto.OrderRequest
import me.onetwo.aiautotrade.common.dto.OrderResult
import java.util.concurrent.CompletableFuture

/**
 * 거래소 제공업체 인터페이스
 *
 * 다양한 거래소(업비트, 바이낸스, 빗썸 등)를 추상화합니다.
 * 새로운 거래소를 추가할 때는 이 인터페이스를 구현하면 됩니다.
 */
interface ExchangeProvider {

    /**
     * 거래소 제공업체의 이름을 반환합니다.
     *
     * @return 제공업체 이름 (예: "upbit", "binance", "bithumb")
     */
    fun getProviderName(): String

    /**
     * 거래소 제공업체가 사용 가능한 상태인지 확인합니다.
     *
     * @return 사용 가능하면 true, 그렇지 않으면 false
     */
    fun isAvailable(): Boolean

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
     * 주문을 실행합니다.
     *
     * @param orderRequest 주문 요청 정보
     * @return 주문 결과
     */
    fun placeOrder(orderRequest: OrderRequest): CompletableFuture<OrderResult>

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
    fun getOrder(orderId: String): CompletableFuture<OrderResult>

    /**
     * 제공업체별 초기화 작업을 수행합니다.
     * 인증, 연결 설정 등의 작업을 여기서 처리합니다.
     */
    fun initialize()

    /**
     * 제공업체별 정리 작업을 수행합니다.
     * 연결 해제, 리소스 정리 등의 작업을 여기서 처리합니다.
     */
    fun shutdown()
}
