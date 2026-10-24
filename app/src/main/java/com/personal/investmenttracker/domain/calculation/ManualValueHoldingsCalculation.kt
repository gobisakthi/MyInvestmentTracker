package com.personal.investmenttracker.domain.calculation

object ManualValueHoldingsCalculation {

    fun calculate(currentValueMinor: Long?): InterestCalculationResult {
        if (currentValueMinor == null || currentValueMinor < 0L) {
            return InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("Enter a valid current value."),
            )
        }
        return InterestCalculationResult(
            status = CalculationStatus.OK,
            openingBalanceMinor = currentValueMinor,
            estimatedTotalMinor = currentValueMinor,
            messages = emptyList(),
        )
    }
}
