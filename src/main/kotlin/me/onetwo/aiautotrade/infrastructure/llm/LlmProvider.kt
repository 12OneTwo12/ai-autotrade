package me.onetwo.aiautotrade.infrastructure.llm

import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.dto.LlmResponse
import java.util.concurrent.CompletableFuture

/**
 * LLM 제공업체 인터페이스
 * 
 * 다양한 LLM 제공업체(Vertex AI, OpenAI, Claude 등)를 추상화합니다.
 * 새로운 LLM 제공업체를 추가할 때는 이 인터페이스를 구현하면 됩니다.
 */
interface LlmProvider {
    
    /**
     * LLM 제공업체의 이름을 반환합니다.
     * 
     * @return 제공업체 이름 (예: "vertex-ai", "openai", "claude")
     */
    fun getProviderName(): String
    
    /**
     * LLM 제공업체가 사용 가능한 상태인지 확인합니다.
     * 
     * @return 사용 가능하면 true, 그렇지 않으면 false
     */
    fun isAvailable(): Boolean
    
    /**
     * LLM을 사용하여 텍스트를 생성합니다.
     * 
     * @param request LLM 요청 정보
     * @return 생성된 텍스트와 메타데이터를 포함한 응답
     */
    fun generateText(request: LlmRequest): CompletableFuture<LlmResponse>
    
    /**
     * 제공업체별 초기화 작업을 수행합니다.
     * 인증, 연결 설정 등의 작업을 여기서 처리합니다.
     */
    fun initialize()
    
    /**
     * 제공업체별 정리 작업을 수행합니다.
     * 연결 해제, 리소스 정리 등의 작업을 여기서 처리합니다.
     */
    fun shutdown()
}