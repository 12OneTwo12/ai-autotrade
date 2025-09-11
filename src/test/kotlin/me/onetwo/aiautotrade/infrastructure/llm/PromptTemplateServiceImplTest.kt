package me.onetwo.aiautotrade.infrastructure.llm

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * PromptTemplateServiceImpl에 대한 단위 테스트
 */
class PromptTemplateServiceImplTest {

    private lateinit var promptTemplateService: PromptTemplateServiceImpl

    @BeforeEach
    fun setUp() {
        promptTemplateService = PromptTemplateServiceImpl()
    }

    @Test
    fun `buildTradingAnalysisPrompt - 기본 매매 분석 프롬프트 생성 테스트`() {
        // Given
        val marketData = mapOf(
            "currentPrice" to 50000,
            "volume" to 1000000,
            "change" to 2.5
        )
        val technicalIndicators = mapOf(
            "rsi" to 45.5,
            "macd" to 2.3,
            "movingAverage20" to 49500
        )
        val newsContext = listOf("긍정적인 실적 발표", "시장 상승 전망")

        // When
        val prompt = promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext)

        // Then
        assertNotNull(prompt)
        assertContains(prompt, "주식 자동매매 전문 AI 분석가")
        assertContains(prompt, "currentPrice: 50000")
        assertContains(prompt, "rsi: 45.5")
        assertContains(prompt, "긍정적인 실적 발표")
        assertContains(prompt, "BUY|SELL|HOLD")
        assertContains(prompt, "confidence")
        assertContains(prompt, "기술적 분석과 기업 펀더멘털")
    }

    @Test
    fun `buildTradingAnalysisPrompt - 뉴스 컨텍스트 없는 경우 테스트`() {
        // Given
        val marketData = mapOf("price" to 100000)
        val technicalIndicators = mapOf("rsi" to 70.0)
        val newsContext = emptyList<String>()

        // When
        val prompt = promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext)

        // Then
        assertNotNull(prompt)
        assertContains(prompt, "주식 자동매매 전문 AI 분석가")
        // 뉴스 섹션이 포함되지 않았는지 확인
        assertTrue(prompt.contains("## 시장 데이터"), "Prompt should contain market data section")
        assertTrue(prompt.contains("## 기술적 지표"), "Prompt should contain technical indicators section")
        // 빈 뉴스 컨텍스트의 경우 뉴스 섹션이 없어야 함
    }

    @Test
    fun `buildMarketAnalysisPrompt - 시장 분석 프롬프트 생성 테스트`() {
        // Given
        val symbol = "005930" // 삼성전자
        val timeframe = "1D"
        val marketData = mapOf(
            "openPrice" to 70000,
            "highPrice" to 72000,
            "lowPrice" to 69500,
            "currentPrice" to 71500,
            "volume" to 15000000
        )

        // When
        val prompt = promptTemplateService.buildMarketAnalysisPrompt(symbol, timeframe, marketData)

        // Then
        assertNotNull(prompt)
        assertContains(prompt, "주식 시장 분석 전문가")
        assertContains(prompt, "005930")
        assertContains(prompt, "1D")
        assertContains(prompt, "openPrice: 70000")
        assertContains(prompt, "현재 주가 상황 요약")
        assertContains(prompt, "기업 펀더멘털 고려사항")
        assertContains(prompt, "투자자 권고사항")
    }

    @Test
    fun `buildRiskAnalysisPrompt - 리스크 분석 프롬프트 생성 테스트`() {
        // Given
        val position = mapOf(
            "symbol" to "AAPL",
            "quantity" to 100,
            "averagePrice" to 150.0,
            "currentPrice" to 155.0,
            "unrealizedPnL" to 500.0
        )
        val marketConditions = mapOf(
            "volatility" to "HIGH",
            "marketTrend" to "BULLISH",
            "economicIndicators" to "MIXED"
        )

        // When
        val prompt = promptTemplateService.buildRiskAnalysisPrompt(position, marketConditions)

        // Then
        assertNotNull(prompt)
        assertContains(prompt, "주식 리스크 관리 전문가")
        assertContains(prompt, "AAPL")
        assertContains(prompt, "quantity: 100")
        assertContains(prompt, "volatility: HIGH")
        assertContains(prompt, "포지션 위험도")
        assertContains(prompt, "섹터별/업종별 리스크")
        assertContains(prompt, "객관적이고 보수적인 관점")
    }

    @Test
    fun `프롬프트들이 적절한 길이를 가지는지 테스트`() {
        // Given
        val marketData = mapOf("price" to 50000)
        val technicalIndicators = mapOf("rsi" to 50.0)
        val newsContext = listOf("테스트 뉴스")

        // When
        val tradingPrompt = promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext)
        val marketPrompt = promptTemplateService.buildMarketAnalysisPrompt("TEST", "1D", marketData)
        val riskPrompt = promptTemplateService.buildRiskAnalysisPrompt(marketData, mapOf("volatility" to "LOW"))

        // Then
        assertTrue(tradingPrompt.length > 100, "Trading prompt should be substantial but was ${tradingPrompt.length} characters")
        assertTrue(marketPrompt.length > 100, "Market prompt should be substantial but was ${marketPrompt.length} characters")
        assertTrue(riskPrompt.length > 100, "Risk prompt should be substantial but was ${riskPrompt.length} characters")
    }
}