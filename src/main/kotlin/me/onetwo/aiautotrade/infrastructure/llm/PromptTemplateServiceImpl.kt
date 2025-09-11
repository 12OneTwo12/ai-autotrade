package me.onetwo.aiautotrade.infrastructure.llm

import org.springframework.stereotype.Service

@Service
class PromptTemplateServiceImpl : PromptTemplateService {

    /**
     * 주식 매매 분석을 위한 프롬프트를 생성합니다.
     *
     * @param marketData 시장 데이터 (가격, 거래량 등)
     * @param technicalIndicators 기술적 지표 (RSI, MACD, 이동평균 등)
     * @param newsContext 관련 뉴스 및 시장 정보
     * @return LLM 분석을 위한 구조화된 프롬프트
     */
    override fun buildTradingAnalysisPrompt(
        marketData: Map<String, Any>,
        technicalIndicators: Map<String, Any>,
        newsContext: List<String>
    ): String {
        val marketDataStr = marketData.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        val technicalIndicatorsStr = technicalIndicators.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        val newsContextStr = newsContext.joinToString("\n- ") { it }

        return """
            당신은 주식 자동매매 전문 AI 분석가입니다.
            아래 정보를 바탕으로 매매 결정을 내려주세요.

            ## 시장 데이터
            $marketDataStr

            ## 기술적 지표
            $technicalIndicatorsStr

            ${if (newsContext.isNotEmpty()) """
            ## 뉴스/시장 정보
            - $newsContextStr
            """ else ""}

            ## 요청사항
            위 정보를 종합하여 다음 JSON 형식으로 매매 결정을 내려주세요:

            {
                "action": "BUY|SELL|HOLD",
                "confidence": 0.0-1.0,
                "reason": "결정 근거를 자세히 설명",
                "suggestedAmount": 추천 거래 수량 (선택사항),
                "stopLossPrice": 손절가 (선택사항),
                "takeProfitPrice": 익절가 (선택사항)
            }

            ## 주의사항
            - 위험 관리를 최우선으로 고려하세요
            - 확신이 없으면 HOLD를 선택하세요
            - confidence는 정확한 수치로 표현하세요
            - 기술적 분석과 기업 펀더멘털을 종합적으로 판단하세요
            - 시장 개장/폐장 시간과 거래량을 고려하세요
        """.trimIndent()
    }

    /**
     * 주식 시장 분석을 위한 프롬프트를 생성합니다.
     *
     * @param symbol 분석할 주식 종목 코드
     * @param timeframe 분석 시간 프레임 (1일, 1주, 1개월 등)
     * @param marketData 시장 데이터
     * @return 시장 분석을 위한 구조화된 프롬프트
     */
    override fun buildMarketAnalysisPrompt(
        symbol: String,
        timeframe: String,
        marketData: Map<String, Any>
    ): String {
        val marketDataStr = marketData.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        return """
            당신은 주식 시장 분석 전문가입니다.
            다음 정보를 바탕으로 $symbol 종목의 시장 분석을 제공해주세요.

            ## 분석 대상
            - 종목: $symbol
            - 시간대: $timeframe

            ## 시장 데이터
            $marketDataStr

            ## 요청사항
            다음 항목들을 포함한 상세한 시장 분석을 제공해주세요:

            1. **현재 주가 상황 요약**
            2. **가격 동향 분석**
            3. **거래량 분석**
            4. **주요 지지/저항선**
            5. **기업 펀더멘털 고려사항**
            6. **단기/중기 전망**
            7. **위험 요소**
            8. **투자자 권고사항**

            분석은 명확하고 이해하기 쉽게 작성해주세요.
            불확실성이 있는 부분은 명시해주세요.
        """.trimIndent()
    }

    /**
     * 주식 포지션 리스크 분석을 위한 프롬프트를 생성합니다.
     *
     * @param position 현재 보유 포지션 정보
     * @param marketConditions 현재 시장 상황
     * @return 리스크 분석을 위한 구조화된 프롬프트
     */
    override fun buildRiskAnalysisPrompt(
        position: Map<String, Any>,
        marketConditions: Map<String, Any>
    ): String {
        val positionStr = position.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        val marketConditionsStr = marketConditions.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        return """
            당신은 주식 리스크 관리 전문가입니다.
            현재 포지션과 시장 상황을 분석하여 위험도를 평가해주세요.

            ## 현재 포지션
            $positionStr

            ## 시장 상황
            $marketConditionsStr

            ## 요청사항
            다음 항목들을 분석해주세요:

            1. **포지션 위험도** (1-10 점수)
            2. **주요 위험 요소들**
            3. **리스크 완화 방안**
            4. **권장 손절 수준**
            5. **포지션 크기 조정 권고**
            6. **시장 변동성 대응 전략**
            7. **섹터별/업종별 리스크**

            객관적이고 보수적인 관점에서 분석해주세요.
        """.trimIndent()
    }
}