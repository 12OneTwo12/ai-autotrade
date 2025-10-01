package me.onetwo.aiautotrade.infrastructure.llm

import me.onetwo.aiautotrade.common.dto.LlmRequest
import me.onetwo.aiautotrade.common.dto.LlmResponse
import me.onetwo.aiautotrade.trading.dto.TradingDecision
import java.util.concurrent.CompletableFuture

/**
 * LLM(Large Language Model) 서비스 인터페이스
 * 
 * 자동매매 시스템에서 AI 분석을 위한 핵심 서비스입니다.
 * Vertex AI를 통해 매매 결정, 시장 분석 등의 기능을 제공합니다.
 */
interface LlmService {
    
    /**
     * LLM을 사용하여 텍스트를 생성합니다.
     *
     * @param request LLM 요청 정보 (프롬프트, 모델 설정 등)
     * @return LLM 응답을 포함한 CompletableFuture
     */
    fun generateText(request: LlmRequest): CompletableFuture<LlmResponse>
    
    /**
     * 매매 신호를 분석하여 매매 결정을 제공합니다.
     *
     * @param marketData 시장 데이터 (가격, 거래량 등)
     * @param technicalIndicators 기술적 지표 (RSI, MACD, 이동평균 등)
     * @param newsContext 관련 뉴스 및 시장 정보 (선택사항)
     * @return 매매 결정 정보를 포함한 CompletableFuture
     */
    fun analyzeTradingSignal(
        marketData: Map<String, Any>,
        technicalIndicators: Map<String, Any>,
        newsContext: List<String> = emptyList()
    ): CompletableFuture<TradingDecision>
    
    /**
     * 특정 종목의 시장 분석 리포트를 생성합니다.
     *
     * @param symbol 종목 코드
     * @param timeframe 분석 시간 프레임
     * @param marketData 시장 데이터
     * @return 시장 분석 리포트를 포함한 CompletableFuture
     */
    fun generateMarketAnalysis(
        symbol: String,
        timeframe: String,
        marketData: Map<String, Any>
    ): CompletableFuture<String>
}