package me.onetwo.aiautotrade.trading.dto

import me.onetwo.aiautotrade.common.enums.TradeAction

data class TradingDecision(
    val action: TradeAction,
    val confidence: Double,
    val reason: String,
    val suggestedAmount: Double? = null,
    val stopLossPrice: Double? = null,
    val takeProfitPrice: Double? = null
)