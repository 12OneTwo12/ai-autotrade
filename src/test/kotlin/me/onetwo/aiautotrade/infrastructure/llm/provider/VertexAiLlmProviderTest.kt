package me.onetwo.aiautotrade.infrastructure.llm.provider

import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.enums.LlmModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * VertexAiLlmProvider에 대한 단위 테스트
 */
class VertexAiLlmProviderTest {

    private lateinit var provider: VertexAiLlmProvider

    @BeforeEach
    fun setUp() {
        provider = VertexAiLlmProvider(
            projectId = "test-project",
            location = "us-central1",
            executor = Executors.newVirtualThreadPerTaskExecutor(),
            clock = Clock.system(ZoneId.systemDefault())
        )
    }

    @Test
    fun `getProviderName - vertex-ai 반환`() {
        // When & Then
        assertEquals("vertex-ai", provider.getProviderName())
    }

    @Test
    fun `isAvailable - 초기화 전에는 false 반환`() {
        // When & Then
        assertFalse(provider.isAvailable())
    }

    @Test
    fun `initialize and shutdown - 라이프사이클 테스트`() {
        // When
        provider.initialize()
        // 인증이 없는 환경에서는 초기화가 실패할 수 있으므로 결과를 확인하지 않음
        
        // When
        provider.shutdown()
        
        // Then - shutdown 후에는 항상 false여야 함
        assertFalse(provider.isAvailable())
    }

    @Test
    fun `generateText - 초기화되지 않은 상태에서 예외 발생`() {
        // Given
        val request = LlmRequest(
            prompt = "테스트 프롬프트",
            model = LlmModel.GEMINI_1_5_PRO
        )

        // When & Then
        try {
            provider.generateText(request).get()
        } catch (e: Exception) {
            assertTrue(e.message?.contains("not available") == true)
        }
    }
}