package me.onetwo.aiautotrade.common.dto

import java.time.LocalDateTime

data class LlmResponse(
    val content: String,
    val model: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val usage: Usage? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)