package me.onetwo.aiautotrade.common.enums

enum class LlmModel(
    val modelName: String,
    val maxTokens: Int,
    val supportedFeatures: Set<ModelFeature>
) {
    GEMINI_1_5_PRO(
        modelName = "gemini-1.5-pro",
        maxTokens = 8192,
        supportedFeatures = setOf(
            ModelFeature.TEXT_GENERATION,
            ModelFeature.FUNCTION_CALLING,
            ModelFeature.JSON_OUTPUT
        )
    ),
    GEMINI_1_5_FLASH(
        modelName = "gemini-1.5-flash",
        maxTokens = 8192,
        supportedFeatures = setOf(
            ModelFeature.TEXT_GENERATION,
            ModelFeature.FUNCTION_CALLING,
            ModelFeature.JSON_OUTPUT
        )
    ),
    GEMINI_1_0_PRO(
        modelName = "gemini-1.0-pro",
        maxTokens = 2048,
        supportedFeatures = setOf(
            ModelFeature.TEXT_GENERATION
        )
    );

    enum class ModelFeature {
        TEXT_GENERATION,
        FUNCTION_CALLING,
        JSON_OUTPUT,
        VISION
    }
}