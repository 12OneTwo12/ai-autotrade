package me.onetwo.aiautotrade.infrastructure.llm.provider

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.api.Part
import com.google.cloud.vertexai.api.Content
import com.google.cloud.vertexai.generativeai.GenerativeModel
import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.dto.LlmResponse
import me.onetwo.aiautotrade.common.dto.Usage
import me.onetwo.aiautotrade.infrastructure.llm.LlmProvider
import me.onetwo.aiautotrade.infrastructure.llm.exception.LlmGenerationException
import me.onetwo.aiautotrade.infrastructure.llm.exception.LlmInitializationException
import me.onetwo.aiautotrade.infrastructure.llm.exception.LlmProviderUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Google Vertex AI LLM 제공업체 구현체
 * 
 * Google Cloud Vertex AI의 Gemini 모델을 사용하여 텍스트를 생성합니다.
 * 
 * @property projectId Google Cloud 프로젝트 ID
 * @property location Vertex AI 리전
 */
@Component
@ConditionalOnProperty(name = ["llm.provider"], havingValue = "vertex-ai", matchIfMissing = true)
class VertexAiLlmProvider(
    @Value("\${vertex.ai.project-id}") private val projectId: String,
    @Value("\${vertex.ai.location:us-central1}") private val location: String,
    @Qualifier("llmExecutor") private val executor: Executor,
    private val clock: Clock
) : LlmProvider {

    private val logger = LoggerFactory.getLogger(VertexAiLlmProvider::class.java)
    
    private var vertexAI: VertexAI? = null
    private var isInitialized = false

    override fun getProviderName(): String = "vertex-ai"

    override fun isAvailable(): Boolean = isInitialized && vertexAI != null

    /**
     * Vertex AI 클라이언트를 초기화합니다.
     */
    override fun initialize() {
        try {
            vertexAI = VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .build()
            isInitialized = true
            logger.info("Vertex AI provider initialized successfully with project: {}, location: {}", projectId, location)
        } catch (e: Exception) {
            logger.error("Failed to initialize Vertex AI provider", e)
            isInitialized = false
            throw LlmInitializationException("Failed to initialize Vertex AI provider", e)
        }
    }

    /**
     * Vertex AI 리소스를 정리합니다.
     */
    override fun shutdown() {
        try {
            vertexAI?.close()
        } catch (e: Exception) {
            // 인증이 없는 환경에서는 shutdown 시에도 에러가 발생할 수 있음
            logger.error("Error during Vertex AI provider shutdown (this is normal without credentials): {}", e.message)
        } finally {
            vertexAI = null
            isInitialized = false
            logger.info("Vertex AI provider shutdown completed")
        }
    }

    /**
     * Vertex AI를 사용하여 텍스트를 생성합니다.
     *
     * @param request LLM 요청 정보
     * @return 생성된 텍스트와 메타데이터를 포함한 응답
     * @throws RuntimeException Vertex AI 호출 실패 시
     */
    override fun generateText(request: LlmRequest): CompletableFuture<LlmResponse> {
        return CompletableFuture.supplyAsync({
            if (!isAvailable()) {
                throw LlmProviderUnavailableException("Vertex AI provider is not available. Call initialize() first.")
            }

            try {
                val model = GenerativeModel.Builder()
                    .setModelName(request.model.modelName)
                    .setVertexAi(vertexAI!!)
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
                    timestamp = LocalDateTime.now(clock),
                    usage = usage,
                    metadata = mapOf("provider" to getProviderName())
                )
            } catch (e: Exception) {
                logger.error("Error generating text with Vertex AI", e)
                throw LlmGenerationException("Failed to generate text with Vertex AI", e)
            }
        }, executor)
    }
}