package me.onetwo.aiautotrade.infrastructure.llm

interface PromptTemplateService {
    
    fun buildTradingAnalysisPrompt(
        marketData: Map<String, Any>,
        technicalIndicators: Map<String, Any>,
        newsContext: List<String>
    ): String
    
    fun buildMarketAnalysisPrompt(
        symbol: String,
        timeframe: String,
        marketData: Map<String, Any>
    ): String
    
    fun buildRiskAnalysisPrompt(
        position: Map<String, Any>,
        marketConditions: Map<String, Any>
    ): String
}