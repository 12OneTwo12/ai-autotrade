package me.onetwo.aiautotrade.infrastructure.llm

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.api.Part
import com.google.cloud.vertexai.api.Content
import com.google.cloud.vertexai.generativeai.GenerativeModel
import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.dto.LlmResponse
import me.onetwo.aiautotrade.common.dto.Usage
import me.onetwo.aiautotrade.common.enums.TradeAction
import me.onetwo.aiautotrade.trading.dto.TradingDecision
import me.onetwo.aiautotrade.infrastructure.llm.LlmService
import me.onetwo.aiautotrade.infrastructure.llm.PromptTemplateService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Google Vertex AI를 사용한 LLM 서비스 구현체
 *
 * 주식 자동매매 시스템에서 AI 기반 분석 및 의사결정을 담당합니다.
 * Gemini 모델을 통해 시장 분석, 매매 신호 분석 등의 기능을 제공합니다.
 *
 * @property projectId Google Cloud 프로젝트 ID
 * @property location Vertex AI 리전 (기본값: us-central1)
 * @property promptTemplateService 프롬프트 템플릿 관리 서비스
 * @property objectMapper JSON 직렬화/역직렬화를 위한 ObjectMapper
 */
@Service
class VertexAiLlmService(
    @Value("\${vertex.ai.project-id}") private val projectId: String,
    @Value("\${vertex.ai.location:us-central1}") private val location: String,
    private val promptTemplateService: PromptTemplateService,
    private val objectMapper: ObjectMapper
) : LlmService {

    private val logger = LoggerFactory.getLogger(VertexAiLlmService::class.java)
    private val executor: Executor = Executors.newVirtualThreadPerTaskExecutor()
    
    private val vertexAI: VertexAI by lazy {
        VertexAI.Builder()
            .setProjectId(projectId)
            .setLocation(location)
            .build()
    }

    /**
     * LLM을 사용하여 텍스트를 생성합니다.
     *
     * @param request LLM 요청 정보
     * @return 생성된 텍스트와 메타데이터를 포함한 응답
     * @throws RuntimeException Vertex AI 호출 실패 시
     */
    override fun generateText(request: LlmRequest): CompletableFuture<LlmResponse> {
        return CompletableFuture.supplyAsync({
            try {
                val model = GenerativeModel.Builder()
                    .setModelName(request.model.modelName)
                    .setVertexAi(vertexAI)
                    .setGenerationConfig(
                        GenerationConfig.newBuilder()
                            .setTemperature(request.temperature)
                            .setMaxOutputTokens(request.maxTokens)
                            .build()
                    )
                    .build()

                val content = Content.newBuilder()
                    .setRole("user")
                    .addParts(Part.newBuilder().setText(request.prompt))
                    .build()

                val response = model.generateContent(content)
                val responseText = response.candidatesList.firstOrNull()?.content?.partsList?.firstOrNull()?.text ?: ""

                val usage = Usage(
                    promptTokens = response.usageMetadata?.promptTokenCount ?: 0,
                    completionTokens = response.usageMetadata?.candidatesTokenCount ?: 0,
                    totalTokens = response.usageMetadata?.totalTokenCount ?: 0
                )

                LlmResponse(
                    content = responseText,
                    model = request.model.modelName,
                    usage = usage
                )
            } catch (e: Exception) {
                logger.error("Error generating text with Vertex AI", e)
                throw RuntimeException("Failed to generate text", e)
            }
        }, executor)
    }

    /**
     * 주식 매매 신호를 분석하여 매매 결정을 제공합니다.
     *
     * @param marketData 시장 데이터 (가격, 거래량 등)
     * @param technicalIndicators 기술적 지표
     * @param newsContext 뉴스 및 시장 정보
     * @return AI 분석 결과를 바탕으로 한 매매 결정
     */
    override fun analyzeTradingSignal(
        marketData: Map<String, Any>,
        technicalIndicators: Map<String, Any>,
        newsContext: List<String>
    ): CompletableFuture<TradingDecision> {
        return CompletableFuture.supplyAsync({
            try {
                val prompt = promptTemplateService.buildTradingAnalysisPrompt(
                    marketData, technicalIndicators, newsContext
                )
                
                val request = LlmRequest(
                    prompt = prompt,
                    temperature = 0.3f,
                    maxTokens = 500
                )
                
                val response = generateText(request).get()
                parseTradingDecision(response.content)
            } catch (e: Exception) {
                logger.error("Error analyzing trading signal", e)
                TradingDecision(
                    action = TradeAction.HOLD,
                    confidence = 0.0,
                    reason = "Analysis failed: ${e.message}"
                )
            }
        }, executor)
    }

    /**
     * 특정 주식의 시장 분석 리포트를 생성합니다.
     *
     * @param symbol 주식 종목 코드
     * @param timeframe 분석 시간 프레임
     * @param marketData 시장 데이터
     * @return AI가 생성한 시장 분석 리포트
     */
    override fun generateMarketAnalysis(
        symbol: String,
        timeframe: String,
        marketData: Map<String, Any>
    ): CompletableFuture<String> {
        return CompletableFuture.supplyAsync({
            try {
                val prompt = promptTemplateService.buildMarketAnalysisPrompt(symbol, timeframe, marketData)
                
                val request = LlmRequest(
                    prompt = prompt,
                    temperature = 0.5f,
                    maxTokens = 1000
                )
                
                generateText(request).get().content
            } catch (e: Exception) {
                logger.error("Error generating market analysis", e)
                "시장 분석을 생성할 수 없습니다: ${e.message}"
            }
        }, executor)
    }

    /**
     * LLM 응답에서 매매 결정 정보를 파싱합니다.
     *
     * JSON 형식의 응답을 파싱하고, 실패할 경우 폴백 파싱을 수행합니다.
     *
     * @param responseText LLM의 원본 응답 텍스트
     * @return 파싱된 매매 결정 객체
     */
    private fun parseTradingDecision(responseText: String): TradingDecision {
        return try {
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonText = responseText.substring(jsonStart, jsonEnd)
                val parsed = objectMapper.readValue<Map<String, Any>>(jsonText)
                
                TradingDecision(
                    action = TradeAction.valueOf(parsed["action"]?.toString()?.uppercase() ?: "HOLD"),
                    confidence = (parsed["confidence"] as? Number)?.toDouble() ?: 0.0,
                    reason = parsed["reason"]?.toString() ?: "No reason provided",
                    suggestedAmount = (parsed["suggestedAmount"] as? Number)?.toDouble(),
                    stopLossPrice = (parsed["stopLossPrice"] as? Number)?.toDouble(),
                    takeProfitPrice = (parsed["takeProfitPrice"] as? Number)?.toDouble()
                )
            } else {
                throw IllegalArgumentException("No valid JSON found in response")
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse trading decision JSON, using fallback parsing", e)
            fallbackParseTradingDecision(responseText)
        }
    }

    /**
     * JSON 파싱이 실패했을 때 사용하는 폴백 파싱 메서드입니다.
     *
     * 응답 텍스트에서 키워드를 찾아 매매 행동을 결정합니다.
     *
     * @param responseText LLM의 원본 응답 텍스트
     * @return 기본 설정이 적용된 매매 결정 객체
     */
    private fun fallbackParseTradingDecision(responseText: String): TradingDecision {
        val upperText = responseText.uppercase()
        
        val action = when {
            upperText.contains("BUY") -> TradeAction.BUY
            upperText.contains("SELL") -> TradeAction.SELL
            else -> TradeAction.HOLD
        }
        
        return TradingDecision(
            action = action,
            confidence = 0.5,
            reason = "Fallback parsing from response: ${responseText.take(100)}..."
        )
    }
}