package com.personal.investmenttracker.domain.calculation

import java.math.BigDecimal
import java.math.RoundingMode

object StockHoldingsCalculation {

    fun calculate(shareQuantity: Double?, currentSharePriceMinor: Long?): InterestCalculationResult {
        if (shareQuantity == null || currentSharePriceMinor == null) {
            return InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("Enter share quantity and current price."),
            )
        }
        if (shareQuantity <= 0.0) {
            return InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("Share quantity must be greater than zero."),
            )
        }
        if (currentSharePriceMinor < 0L) {
            return InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("Share price cannot be negative."),
            )
        }
        val marketValue = BigDecimal.valueOf(shareQuantity)
            .multiply(BigDecimal.valueOf(currentSharePriceMinor))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
        return InterestCalculationResult(
            status = CalculationStatus.OK,
            openingBalanceMinor = marketValue,
            estimatedTotalMinor = marketValue,
            messages = emptyList(),
        )
    }
}
