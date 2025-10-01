package me.onetwo.aiautotrade.infrastructure.exchange

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.onetwo.aiautotrade.common.dto.*
import me.onetwo.aiautotrade.common.enums.OrderSide
import me.onetwo.aiautotrade.common.enums.OrderState
import me.onetwo.aiautotrade.common.enums.OrderType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class DefaultExchangeServiceTest {

    private lateinit var exchangeProvider: ExchangeProvider
    private lateinit var exchangeService: ExchangeService

    @BeforeEach
    fun setUp() {
        exchangeProvider = mockk(relaxed = true)
        exchangeService = DefaultExchangeService(exchangeProvider)

        every { exchangeProvider.getProviderName() } returns "mock-exchange"
    }

    @Test
    fun `getAccountInfo should delegate to provider`() {
        // given
        val expectedAccountInfo = AccountInfo(
            balances = listOf(
                Balance("KRW", BigDecimal("1000000"), BigDecimal.ZERO)
            ),
            totalBalance = BigDecimal("1000000")
        )

        every { exchangeProvider.getAccountInfo() } returns CompletableFuture.completedFuture(expectedAccountInfo)

        // when
        val result = exchangeService.getAccountInfo().get()

        // then
        assertNotNull(result)
        assertEquals(expectedAccountInfo, result)
        verify { exchangeProvider.getAccountInfo() }
    }

    @Test
    fun `getMarketPrice should delegate to provider`() {
        // given
        val market = "KRW-BTC"
        val expectedPrice = MarketPrice(
            market = market,
            tradePrice = BigDecimal("50000000"),
            highPrice = BigDecimal("51000000"),
            lowPrice = BigDecimal("49000000"),
            openPrice = BigDecimal("50000000"),
            prevClosingPrice = BigDecimal("49500000"),
            changeRate = BigDecimal("0.01"),
            accTradeVolume24h = BigDecimal("100"),
            timestamp = LocalDateTime.now()
        )

        every { exchangeProvider.getMarketPrice(market) } returns CompletableFuture.completedFuture(expectedPrice)

        // when
        val result = exchangeService.getMarketPrice(market).get()

        // then
        assertNotNull(result)
        assertEquals(expectedPrice, result)
        verify { exchangeProvider.getMarketPrice(market) }
    }

    @Test
    fun `buy with price and volume should place limit order`() {
        // given
        val market = "KRW-BTC"
        val price = BigDecimal("50000000")
        val volume = BigDecimal("0.1")

        val expectedOrder = OrderResult(
            orderId = "order-123",
            market = market,
            side = OrderSide.BID,
            orderType = OrderType.LIMIT,
            price = price,
            volume = volume,
            executedVolume = BigDecimal.ZERO,
            remainingVolume = volume,
            state = OrderState.WAIT,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.placeOrder(any()) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.buy(market, price, volume).get()

        // then
        assertNotNull(result)
        assertEquals(OrderType.LIMIT, result.orderType)
        verify {
            exchangeProvider.placeOrder(match {
                it.orderType == OrderType.LIMIT && it.side == OrderSide.BID
            })
        }
    }

    @Test
    fun `buy with only price should place market price order`() {
        // given
        val market = "KRW-BTC"
        val price = BigDecimal("5000000")

        val expectedOrder = OrderResult(
            orderId = "order-123",
            market = market,
            side = OrderSide.BID,
            orderType = OrderType.PRICE,
            price = price,
            volume = BigDecimal.ZERO,
            executedVolume = BigDecimal.ZERO,
            remainingVolume = BigDecimal.ZERO,
            state = OrderState.WAIT,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.placeOrder(any()) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.buy(market, price, null).get()

        // then
        assertNotNull(result)
        assertEquals(OrderType.PRICE, result.orderType)
        verify {
            exchangeProvider.placeOrder(match {
                it.orderType == OrderType.PRICE && it.side == OrderSide.BID
            })
        }
    }

    @Test
    fun `sell with price and volume should place limit order`() {
        // given
        val market = "KRW-BTC"
        val price = BigDecimal("50000000")
        val volume = BigDecimal("0.1")

        val expectedOrder = OrderResult(
            orderId = "order-123",
            market = market,
            side = OrderSide.ASK,
            orderType = OrderType.LIMIT,
            price = price,
            volume = volume,
            executedVolume = BigDecimal.ZERO,
            remainingVolume = volume,
            state = OrderState.WAIT,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.placeOrder(any()) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.sell(market, price, volume).get()

        // then
        assertNotNull(result)
        assertEquals(OrderType.LIMIT, result.orderType)
        verify {
            exchangeProvider.placeOrder(match {
                it.orderType == OrderType.LIMIT && it.side == OrderSide.ASK
            })
        }
    }

    @Test
    fun `sell with only volume should place market order`() {
        // given
        val market = "KRW-BTC"
        val volume = BigDecimal("0.1")

        val expectedOrder = OrderResult(
            orderId = "order-123",
            market = market,
            side = OrderSide.ASK,
            orderType = OrderType.MARKET,
            price = null,
            volume = volume,
            executedVolume = BigDecimal.ZERO,
            remainingVolume = volume,
            state = OrderState.WAIT,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.placeOrder(any()) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.sell(market, null, volume).get()

        // then
        assertNotNull(result)
        assertEquals(OrderType.MARKET, result.orderType)
        verify {
            exchangeProvider.placeOrder(match {
                it.orderType == OrderType.MARKET && it.side == OrderSide.ASK
            })
        }
    }

    @Test
    fun `cancelOrder should delegate to provider`() {
        // given
        val orderId = "order-123"
        val expectedOrder = OrderResult(
            orderId = orderId,
            market = "KRW-BTC",
            side = OrderSide.BID,
            orderType = OrderType.LIMIT,
            price = BigDecimal("50000000"),
            volume = BigDecimal("0.1"),
            executedVolume = BigDecimal.ZERO,
            remainingVolume = BigDecimal("0.1"),
            state = OrderState.CANCEL,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.cancelOrder(orderId) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.cancelOrder(orderId).get()

        // then
        assertNotNull(result)
        assertEquals(OrderState.CANCEL, result.state)
        verify { exchangeProvider.cancelOrder(orderId) }
    }

    @Test
    fun `getOrderStatus should delegate to provider`() {
        // given
        val orderId = "order-123"
        val expectedOrder = OrderResult(
            orderId = orderId,
            market = "KRW-BTC",
            side = OrderSide.BID,
            orderType = OrderType.LIMIT,
            price = BigDecimal("50000000"),
            volume = BigDecimal("0.1"),
            executedVolume = BigDecimal("0.1"),
            remainingVolume = BigDecimal.ZERO,
            state = OrderState.DONE,
            createdAt = LocalDateTime.now()
        )

        every { exchangeProvider.getOrder(orderId) } returns CompletableFuture.completedFuture(expectedOrder)

        // when
        val result = exchangeService.getOrderStatus(orderId).get()

        // then
        assertNotNull(result)
        assertEquals(OrderState.DONE, result.state)
        verify { exchangeProvider.getOrder(orderId) }
    }
}
