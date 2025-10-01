package me.onetwo.aiautotrade.infrastructure.exchange.upbit

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import me.onetwo.aiautotrade.infrastructure.exchange.upbit.dto.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * 업비트 API 클라이언트
 *
 * 업비트 REST API와의 통신을 담당합니다.
 * JWT 토큰 생성 및 HTTP 요청을 처리합니다.
 */
@Component
class UpbitApiClient(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(UpbitApiClient::class.java)

    companion object {
        private const val API_BASE_URL = "https://api.upbit.com"
        private const val ACCOUNT_ENDPOINT = "/v1/accounts"
        private const val TICKER_ENDPOINT = "/v1/ticker"
        private const val ORDERS_ENDPOINT = "/v1/orders"
        private const val ORDER_ENDPOINT = "/v1/order"
    }

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(API_BASE_URL)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    /**
     * 계좌 정보 조회
     *
     * @param accessKey API Access Key
     * @param secretKey API Secret Key
     * @return 계좌 정보 목록
     */
    fun getAccounts(accessKey: String, secretKey: String): Mono<List<UpbitAccount>> {
        val token = generateToken(accessKey, secretKey)

        return webClient.get()
            .uri(ACCOUNT_ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono<List<UpbitAccount>>()
            .doOnError { error ->
                logger.error("Failed to get accounts", error)
            }
    }

    /**
     * 시세 티커 조회
     *
     * @param markets 마켓 코드 목록 (예: ["KRW-BTC", "KRW-ETH"])
     * @return 시세 정보 목록
     */
    fun getTicker(markets: List<String>): Mono<List<UpbitTicker>> {
        val marketsParam = markets.joinToString(",")

        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(TICKER_ENDPOINT)
                    .queryParam("markets", marketsParam)
                    .build()
            }
            .retrieve()
            .bodyToMono<List<UpbitTicker>>()
            .doOnError { error ->
                logger.error("Failed to get ticker for markets: $markets", error)
            }
    }

    /**
     * 주문하기
     *
     * @param accessKey API Access Key
     * @param secretKey API Secret Key
     * @param orderRequest 주문 요청 정보
     * @return 주문 결과
     */
    fun placeOrder(
        accessKey: String,
        secretKey: String,
        orderRequest: UpbitOrderRequest
    ): Mono<UpbitOrderResponse> {
        val queryString = buildQueryString(orderRequest)
        val token = generateToken(accessKey, secretKey, queryString)

        return webClient.post()
            .uri(ORDERS_ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .bodyValue(orderRequest)
            .retrieve()
            .bodyToMono<UpbitOrderResponse>()
            .doOnError { error ->
                logger.error("Failed to place order: $orderRequest", error)
            }
    }

    /**
     * 주문 조회
     *
     * @param accessKey API Access Key
     * @param secretKey API Secret Key
     * @param orderId 주문 ID
     * @return 주문 정보
     */
    fun getOrder(
        accessKey: String,
        secretKey: String,
        orderId: String
    ): Mono<UpbitOrderResponse> {
        val queryParams = mapOf("uuid" to orderId)
        val queryString = buildQueryString(queryParams)
        val token = generateToken(accessKey, secretKey, queryString)

        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(ORDER_ENDPOINT)
                    .queryParam("uuid", orderId)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono<UpbitOrderResponse>()
            .doOnError { error ->
                logger.error("Failed to get order: $orderId", error)
            }
    }

    /**
     * 주문 취소
     *
     * @param accessKey API Access Key
     * @param secretKey API Secret Key
     * @param orderId 주문 ID
     * @return 취소된 주문 정보
     */
    fun cancelOrder(
        accessKey: String,
        secretKey: String,
        orderId: String
    ): Mono<UpbitOrderResponse> {
        val queryParams = mapOf("uuid" to orderId)
        val queryString = buildQueryString(queryParams)
        val token = generateToken(accessKey, secretKey, queryString)

        return webClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path(ORDER_ENDPOINT)
                    .queryParam("uuid", orderId)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono<UpbitOrderResponse>()
            .doOnError { error ->
                logger.error("Failed to cancel order: $orderId", error)
            }
    }

    /**
     * JWT 토큰 생성
     *
     * @param accessKey API Access Key
     * @param secretKey API Secret Key
     * @param queryString 쿼리 스트링 (선택)
     * @return JWT 토큰
     */
    private fun generateToken(
        accessKey: String,
        secretKey: String,
        queryString: String? = null
    ): String {
        val claims = mutableMapOf<String, Any>(
            "access_key" to accessKey,
            "nonce" to UUID.randomUUID().toString()
        )

        if (!queryString.isNullOrBlank()) {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(queryString.toByteArray(Charsets.UTF_8))

            val queryHash = String.format("%0128x", BigInteger(1, md.digest()))

            claims["query_hash"] = queryHash
            claims["query_hash_alg"] = "SHA512"
        }

        val key = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")

        return Jwts.builder()
            .claims(claims)
            .signWith(key)
            .compact()
    }

    /**
     * 쿼리 스트링 생성
     *
     * @param params 파라미터 맵
     * @return 쿼리 스트링 (URL 인코딩 적용)
     */
    private fun buildQueryString(params: Map<String, Any?>): String {
        return params.entries
            .filter { it.value != null }
            .joinToString("&") {
                "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value.toString(), "UTF-8")}"
            }
    }

    /**
     * 객체를 쿼리 스트링으로 변환
     *
     * @param obj 변환할 객체
     * @return 쿼리 스트링
     */
    private fun buildQueryString(obj: Any): String {
        val map = objectMapper.convertValue(obj, Map::class.java)
        @Suppress("UNCHECKED_CAST")
        return buildQueryString(map as Map<String, Any?>)
    }
}
