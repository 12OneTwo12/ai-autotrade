package me.onetwo.aiautotrade.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * LLM 관련 설정 클래스
 */
@Configuration
class LlmConfig {
    
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
}

/**
 * LLM 제공업체 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "llm")
data class LlmProperties(
    var provider: String = "vertex-ai",
    var timeoutSeconds: Int = 30,
    var retryCount: Int = 3,
    var defaultModel: String = "GEMINI_1_5_PRO"
)