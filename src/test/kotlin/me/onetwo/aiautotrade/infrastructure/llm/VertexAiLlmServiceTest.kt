package me.onetwo.aiautotrade.infrastructure.llm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.enums.LlmModel
import me.onetwo.aiautotrade.common.enums.TradeAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * VertexAiLlmService에 대한 단위 테스트
 */
class VertexAiLlmServiceTest {

    private val promptTemplateService = mockk<PromptTemplateService>()
    private lateinit var objectMapper: ObjectMapper
    private lateinit var llmService: VertexAiLlmService

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        
        // 실제 Vertex AI 호출을 피하기 위해 테스트용 projectId 사용
        llmService = VertexAiLlmService(
            projectId = "test-project",
            location = "us-central1",
            promptTemplateService = promptTemplateService,
            objectMapper = objectMapper
        )
    }

    @Test
    fun `analyzeTradingSignal - 프롬프트 템플릿 서비스가 올바르게 호출되는지 확인`() {
        // Given
        val marketData = mapOf("price" to 50000, "volume" to 1000000)
        val technicalIndicators = mapOf("rsi" to 45.5, "macd" to 2.3)
        val newsContext = listOf("긍정적인 실적 발표", "시장 상승 전망")
        
        val expectedPrompt = "테스트 프롬프트"
        every { promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext) } returns expectedPrompt

        // When & Then - Vertex AI 실제 호출 없이 프롬프트 생성만 테스트
        // 실제 Vertex AI 호출은 통합 테스트에서 별도로 테스트
        try {
            llmService.analyzeTradingSignal(marketData, technicalIndicators, newsContext).get()
        } catch (e: ExecutionException) {
            // Vertex AI 호출 실패는 예상됨 (실제 인증 없음)
            assertTrue(e.cause is RuntimeException)
        }
        
        // 프롬프트 템플릿 서비스가 올바른 파라미터로 호출되었는지 검증
        verify { promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext) }
    }

    @Test
    fun `generateMarketAnalysis - 프롬프트 템플릿 서비스가 올바르게 호출되는지 확인`() {
        // Given
        val symbol = "005930" // 삼성전자
        val timeframe = "1D"
        val marketData = mapOf("price" to 70000, "volume" to 50000000)
        
        val expectedPrompt = "시장 분석 프롬프트"
        every { promptTemplateService.buildMarketAnalysisPrompt(symbol, timeframe, marketData) } returns expectedPrompt

        // When & Then
        try {
            llmService.generateMarketAnalysis(symbol, timeframe, marketData).get()
        } catch (e: ExecutionException) {
            // Vertex AI 호출 실패는 예상됨
            assertTrue(e.cause is RuntimeException || e.message?.contains("시장 분석을 생성할 수 없습니다") == true)
        }
        
        // 프롬프트 템플릿 서비스가 올바른 파라미터로 호출되었는지 검증
        verify { promptTemplateService.buildMarketAnalysisPrompt(symbol, timeframe, marketData) }
    }

    @Test
    fun `LlmRequest 생성 테스트`() {
        // Given
        val prompt = "테스트 프롬프트"
        val model = LlmModel.GEMINI_1_5_PRO
        
        // When
        val request = LlmRequest(
            prompt = prompt,
            model = model,
            temperature = 0.3f,
            maxTokens = 500
        )
        
        // Then
        assertEquals(prompt, request.prompt)
        assertEquals(model, request.model)
        assertEquals(0.3f, request.temperature)
        assertEquals(500, request.maxTokens)
    }

    @Test
    fun `TradeAction enum 값 테스트`() {
        // Given & When & Then
        assertEquals("매수", TradeAction.BUY.description)
        assertEquals("매도", TradeAction.SELL.description)
        assertEquals("보유", TradeAction.HOLD.description)
    }

    @Test
    fun `LlmModel enum 속성 테스트`() {
        // Given & When
        val model = LlmModel.GEMINI_1_5_PRO
        
        // Then
        assertEquals("gemini-1.5-pro", model.modelName)
        assertEquals(8192, model.maxTokens)
        assertTrue(model.supportedFeatures.contains(LlmModel.ModelFeature.TEXT_GENERATION))
        assertTrue(model.supportedFeatures.contains(LlmModel.ModelFeature.JSON_OUTPUT))
    }
}