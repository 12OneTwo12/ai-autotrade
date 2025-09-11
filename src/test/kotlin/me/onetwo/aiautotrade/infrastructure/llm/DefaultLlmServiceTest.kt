package me.onetwo.aiautotrade.infrastructure.llm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.dto.LlmResponse
import me.onetwo.aiautotrade.common.enums.LlmModel
import me.onetwo.aiautotrade.common.enums.TradeAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * DefaultLlmService에 대한 단위 테스트
 */
class DefaultLlmServiceTest {

    private val llmProvider = mockk<LlmProvider>()
    private val promptTemplateService = mockk<PromptTemplateService>()
    private lateinit var objectMapper: ObjectMapper
    private lateinit var llmService: DefaultLlmService

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        
        every { llmProvider.getProviderName() } returns "test-provider"
        every { llmProvider.isAvailable() } returns true
        every { llmProvider.initialize() } returns Unit
        every { llmProvider.shutdown() } returns Unit
        
        llmService = DefaultLlmService(
            llmProvider = llmProvider,
            promptTemplateService = promptTemplateService,
            objectMapper = objectMapper,
            executor = Executors.newVirtualThreadPerTaskExecutor()
        )
    }

    @Test
    fun `generateText - LLM 제공업체에게 요청을 전달한다`() {
        // Given
        val request = LlmRequest(
            prompt = "테스트 프롬프트",
            model = LlmModel.GEMINI_1_5_PRO
        )
        val expectedResponse = LlmResponse(
            content = "테스트 응답",
            model = "gemini-1.5-pro",
            timestamp = LocalDateTime.now()
        )
        
        every { llmProvider.generateText(request) } returns CompletableFuture.completedFuture(expectedResponse)
        
        // When
        val result = llmService.generateText(request).get()
        
        // Then
        assertEquals(expectedResponse.content, result.content)
        assertEquals(expectedResponse.model, result.model)
        verify { llmProvider.generateText(request) }
    }

    @Test
    fun `analyzeTradingSignal - 프롬프트 생성 및 LLM 호출 테스트`() {
        // Given
        val marketData = mapOf("price" to 50000, "volume" to 1000000)
        val technicalIndicators = mapOf("rsi" to 45.5, "macd" to 2.3)
        val newsContext = listOf("긍정적인 실적 발표")
        
        val expectedPrompt = "테스트 프롬프트"
        val mockResponse = LlmResponse(
            content = "{\"action\": \"BUY\", \"confidence\": 0.8, \"reason\": \"Test reason\"}",
            model = "test-model",
            timestamp = LocalDateTime.now()
        )
        
        every { promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext) } returns expectedPrompt
        every { llmProvider.generateText(any()) } returns CompletableFuture.completedFuture(mockResponse)
        
        // When
        val result = llmService.analyzeTradingSignal(marketData, technicalIndicators, newsContext).get()
        
        // Then
        assertEquals(TradeAction.BUY, result.action)
        assertEquals(0.8, result.confidence)
        verify { promptTemplateService.buildTradingAnalysisPrompt(marketData, technicalIndicators, newsContext) }
        verify { llmProvider.generateText(any()) }
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