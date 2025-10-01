package me.onetwo.aiautotrade.infrastructure.exchange

import me.onetwo.aiautotrade.common.dto.AccountInfo
import me.onetwo.aiautotrade.common.dto.MarketPrice
import me.onetwo.aiautotrade.common.dto.OrderRequest
import me.onetwo.aiautotrade.common.dto.OrderResult
import me.onetwo.aiautotrade.common.enums.OrderSide
import me.onetwo.aiautotrade.common.enums.OrderType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Exchange 서비스 구현체
 *
 * 거래소와의 모든 상호작용을 담당합니다.
 * Provider 패턴을 사용하여 다양한 거래소를 지원합니다.
 *
 * @property exchangeProvider 거래소 제공업체 (업비트, 바이낸스 등)
 */
@Service
class DefaultExchangeService(
    private val exchangeProvider: ExchangeProvider
) : ExchangeService {

    private val logger = LoggerFactory.getLogger(DefaultExchangeService::class.java)

    @PostConstruct
    fun init() {
        exchangeProvider.initialize()
        logger.info("Exchange Service initialized with provider: {}", exchangeProvider.getProviderName())
    }

    @PreDestroy
    fun destroy() {
        exchangeProvider.shutdown()
        logger.info("Exchange Service shutdown completed")
    }

    override fun getAccountInfo(): CompletableFuture<AccountInfo> {
        logger.info("Fetching account info from {}", exchangeProvider.getProviderName())
        return exchangeProvider.getAccountInfo()
    }

    override fun getMarketPrice(market: String): CompletableFuture<MarketPrice> {
        logger.info("Fetching market price for {} from {}", market, exchangeProvider.getProviderName())
        return exchangeProvider.getMarketPrice(market)
    }

    override fun buy(
        market: String,
        price: BigDecimal?,
        volume: BigDecimal?
    ): CompletableFuture<OrderResult> {
        val orderType = determineOrderType(OrderSide.BID, price, volume)
        val orderRequest = OrderRequest(
            market = market,
            side = OrderSide.BID,
            orderType = orderType,
            price = price,
            volume = volume
        )

        logger.info("Placing buy order for {} with type {}", market, orderType)
        return exchangeProvider.placeOrder(orderRequest)
    }

    override fun sell(
        market: String,
        price: BigDecimal?,
        volume: BigDecimal?
    ): CompletableFuture<OrderResult> {
        val orderType = determineOrderType(OrderSide.ASK, price, volume)
        val orderRequest = OrderRequest(
            market = market,
            side = OrderSide.ASK,
            orderType = orderType,
            price = price,
            volume = volume
        )

        logger.info("Placing sell order for {} with type {}", market, orderType)
        return exchangeProvider.placeOrder(orderRequest)
    }

    override fun cancelOrder(orderId: String): CompletableFuture<OrderResult> {
        logger.info("Cancelling order: {}", orderId)
        return exchangeProvider.cancelOrder(orderId)
    }

    override fun getOrderStatus(orderId: String): CompletableFuture<OrderResult> {
        logger.info("Fetching order status: {}", orderId)
        return exchangeProvider.getOrder(orderId)
    }

    /**
     * 주문 타입 결정
     *
     * 가격과 수량 정보를 바탕으로 적절한 주문 타입을 결정합니다.
     *
     * @param side 주문 종류 (매수/매도)
     * @param price 주문 가격
     * @param volume 주문 수량
     * @return 주문 타입
     */
    private fun determineOrderType(
        side: OrderSide,
        price: BigDecimal?,
        volume: BigDecimal?
    ): OrderType {
        return when {
            // 지정가 주문: 가격과 수량이 모두 지정된 경우
            price != null && volume != null -> OrderType.LIMIT

            // 시장가 매수: 매수이고 가격만 지정된 경우 (총 매수 금액)
            side == OrderSide.BID && price != null -> OrderType.PRICE

            // 시장가 매도: 매도이고 수량만 지정된 경우
            side == OrderSide.ASK && volume != null -> OrderType.MARKET

            else -> throw IllegalArgumentException(
                "Invalid order parameters: side=$side, price=$price, volume=$volume"
            )
        }
    }
}
