package me.onetwo.aiautotrade.infrastructure.llm.exception

/**
 * LLM 제공업체가 사용 불가능한 상태일 때 발생하는 예외
 */
class LlmProviderUnavailableException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * LLM 텍스트 생성 과정에서 발생하는 예외
 */
class LlmGenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * LLM 제공업체 초기화 실패 시 발생하는 예외
 */
class LlmInitializationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 매매 결정 파싱 실패 시 발생하는 예외
 */
class TradingDecisionParseException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)