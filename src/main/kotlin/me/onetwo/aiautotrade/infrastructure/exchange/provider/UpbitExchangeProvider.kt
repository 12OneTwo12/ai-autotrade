package me.onetwo.aiautotrade.infrastructure.exchange.provider

import me.onetwo.aiautotrade.common.dto.*
import me.onetwo.aiautotrade.common.enums.OrderSide
import me.onetwo.aiautotrade.common.enums.OrderState
import me.onetwo.aiautotrade.common.enums.OrderType
import me.onetwo.aiautotrade.infrastructure.exchange.ExchangeProvider
import me.onetwo.aiautotrade.infrastructure.exchange.upbit.UpbitApiClient
import me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto.UpbitOrderRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

/**
 * 업비트 거래소 Provider 구현체
 *
 * 업비트 API를 통해 거래소 기능을 제공합니다.
 */
@Component
class UpbitExchangeProvider(
    private val upbitApiClient: UpbitApiClient,
    @Value("\${upbit.access-key:}") private val accessKey: String,
    @Value("\${upbit.secret-key:}") private val secretKey: String
) : ExchangeProvider {

    private val logger = LoggerFactory.getLogger(UpbitExchangeProvider::class.java)

    override fun getProviderName(): String = "upbit"

    override fun isAvailable(): Boolean {
        return accessKey.isNotBlank() && secretKey.isNotBlank()
    }

    override fun getAccountInfo(): CompletableFuture<AccountInfo> {
        return upbitApiClient.getAccounts(accessKey, secretKey)
            .toFuture()
            .thenApply { upbitAccounts ->
                val balances = upbitAccounts.map { account ->
                    Balance(
                        currency = account.currency,
                        balance = account.balance,
                        locked = account.locked,
                        avgBuyPrice = account.avgBuyPrice
                    )
                }

                // TODO: 모든 자산을 KRW로 환산하여 총 자산 계산
                // 현재는 KRW 잔액만 계산하며, 추후 코인 자산도 현재가 기준으로 KRW 환산 필요
                val totalBalance = balances
                    .filter { it.currency == "KRW" }
                    .sumOf { it.balance + it.locked }

                AccountInfo(
                    balances = balances,
                    totalBalance = totalBalance
                )
            }
            .exceptionally { throwable ->
                logger.error("Failed to get account info", throwable)
                throw RuntimeException("Failed to get account info", throwable)
            }
    }

    override fun getMarketPrice(market: String): CompletableFuture<MarketPrice> {
        return upbitApiClient.getTicker(listOf(market))
            .toFuture()
            .thenApply { tickers ->
                val ticker = tickers.firstOrNull()
                    ?: throw IllegalStateException("No ticker data for market: $market")

                MarketPrice(
                    market = ticker.market,
                    tradePrice = ticker.tradePrice,
                    highPrice = ticker.highPrice,
                    lowPrice = ticker.lowPrice,
                    openPrice = ticker.openingPrice,
                    prevClosingPrice = ticker.prevClosingPrice,
                    changeRate = ticker.signedChangeRate,
                    accTradeVolume24h = ticker.accTradeVolume24h,
                    timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(ticker.timestamp),
                        ZoneId.systemDefault()
                    )
                )
            }
            .exceptionally { throwable ->
                logger.error("Failed to get market price for: $market", throwable)
                throw RuntimeException("Failed to get market price", throwable)
            }
    }

    override fun placeOrder(orderRequest: OrderRequest): CompletableFuture<OrderResult> {
        val upbitOrderRequest = UpbitOrderRequest(
            market = orderRequest.market,
            side = mapOrderSide(orderRequest.side),
            ordType = mapOrderType(orderRequest.orderType),
            volume = orderRequest.volume,
            price = orderRequest.price
        )

        return upbitApiClient.placeOrder(accessKey, secretKey, upbitOrderRequest)
            .toFuture()
            .thenApply { response ->
                OrderResult(
                    orderId = response.uuid,
                    market = response.market,
                    side = OrderSide.valueOf(response.side.uppercase()),
                    orderType = mapUpbitOrderType(response.ordType),
                    price = response.price,
                    volume = response.volume ?: BigDecimal.ZERO,
                    executedVolume = response.executedVolume ?: BigDecimal.ZERO,
                    remainingVolume = response.remainingVolume ?: BigDecimal.ZERO,
                    state = mapOrderState(response.state),
                    createdAt = parseDateTime(response.createdAt)
                )
            }
            .exceptionally { throwable ->
                logger.error("Failed to place order: $orderRequest", throwable)
                throw RuntimeException("Failed to place order", throwable)
            }
    }

    override fun cancelOrder(orderId: String): CompletableFuture<OrderResult> {
        return upbitApiClient.cancelOrder(accessKey, secretKey, orderId)
            .toFuture()
            .thenApply { response ->
                OrderResult(
                    orderId = response.uuid,
                    market = response.market,
                    side = OrderSide.valueOf(response.side.uppercase()),
                    orderType = mapUpbitOrderType(response.ordType),
                    price = response.price,
                    volume = response.volume ?: BigDecimal.ZERO,
                    executedVolume = response.executedVolume ?: BigDecimal.ZERO,
                    remainingVolume = response.remainingVolume ?: BigDecimal.ZERO,
                    state = mapOrderState(response.state),
                    createdAt = parseDateTime(response.createdAt)
                )
            }
            .exceptionally { throwable ->
                logger.error("Failed to cancel order: $orderId", throwable)
                throw RuntimeException("Failed to cancel order", throwable)
            }
    }

    override fun getOrder(orderId: String): CompletableFuture<OrderResult> {
        return upbitApiClient.getOrder(accessKey, secretKey, orderId)
            .toFuture()
            .thenApply { response ->
                OrderResult(
                    orderId = response.uuid,
                    market = response.market,
                    side = OrderSide.valueOf(response.side.uppercase()),
                    orderType = mapUpbitOrderType(response.ordType),
                    price = response.price,
                    volume = response.volume ?: BigDecimal.ZERO,
                    executedVolume = response.executedVolume ?: BigDecimal.ZERO,
                    remainingVolume = response.remainingVolume ?: BigDecimal.ZERO,
                    state = mapOrderState(response.state),
                    createdAt = parseDateTime(response.createdAt)
                )
            }
            .exceptionally { throwable ->
                logger.error("Failed to get order: $orderId", throwable)
                throw RuntimeException("Failed to get order", throwable)
            }
    }

    override fun initialize() {
        logger.info("Upbit Exchange Provider initialized")
        if (!isAvailable()) {
            logger.warn("Upbit API keys are not configured")
        }
    }

    override fun shutdown() {
        logger.info("Upbit Exchange Provider shutdown")
    }

    /**
     * OrderSide를 업비트 형식으로 변환
     */
    private fun mapOrderSide(side: OrderSide): String = when (side) {
        OrderSide.BID -> "bid"
        OrderSide.ASK -> "ask"
    }

    /**
     * OrderType을 업비트 형식으로 변환
     */
    private fun mapOrderType(type: OrderType): String = when (type) {
        OrderType.LIMIT -> "limit"
        OrderType.PRICE -> "price"
        OrderType.MARKET -> "market"
    }

    /**
     * 업비트 주문 타입을 OrderType으로 변환
     */
    private fun mapUpbitOrderType(ordType: String): OrderType = when (ordType) {
        "limit" -> OrderType.LIMIT
        "price" -> OrderType.PRICE
        "market" -> OrderType.MARKET
        else -> throw IllegalArgumentException("Unknown order type from Upbit: $ordType")
    }

    /**
     * 업비트 주문 상태를 OrderState로 변환
     */
    private fun mapOrderState(state: String): OrderState = when (state) {
        "wait" -> OrderState.WAIT
        "watch" -> OrderState.WATCH
        "done" -> OrderState.DONE
        "cancel" -> OrderState.CANCEL
        else -> throw IllegalArgumentException("Unknown order state from Upbit: $state")
    }

    /**
     * ISO 8601 형식의 날짜 문자열을 LocalDateTime으로 변환
     */
    private fun parseDateTime(dateString: String): LocalDateTime {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    }
}
