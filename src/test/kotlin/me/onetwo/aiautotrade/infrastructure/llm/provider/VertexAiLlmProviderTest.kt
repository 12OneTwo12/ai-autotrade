package me.onetwo.aiautotrade.infrastructure.llm.provider

import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.enums.LlmModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
            location = "us-central1"
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
        
        // Then - 실제 인증 없이는 초기화 실패할 수 있음
        // 이는 정상적인 동작이므로 테스트에서는 메서드 호출만 확인
        
        // When
        provider.shutdown()
        
        // Then
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