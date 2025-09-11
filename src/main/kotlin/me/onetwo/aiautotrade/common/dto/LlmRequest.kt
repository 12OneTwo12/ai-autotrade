package me.onetwo.aiautotrade.common.dto

import me.onetwo.aiautotrade.common.enums.LlmModel

data class LlmRequest(
    val prompt: String,
    val context: Map<String, Any> = emptyMap(),
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val model: LlmModel = LlmModel.GEMINI_1_5_PRO
)