package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate
import java.time.YearMonth

data class MonthlyDepositInput(
    val year: Int,
    val month: Int,
    val amountMinor: Long,
) {
    val yearMonth: YearMonth get() = YearMonth.of(year, month)
}

data class RatePeriodInput(
    val effectiveYear: Int,
    val effectiveMonth: Int,
    val annualRateBasisPoints: Int,
) {
    val effectiveYearMonth: YearMonth get() = YearMonth.of(effectiveYear, effectiveMonth)
}

data class InterestCalculationInput(
    val accountType: InvestmentType,
    val openingBalanceMinor: Long,
    val openingBalanceDate: LocalDate,
    val openingBalanceIncludesCreditedInterest: Boolean,
    val monthlyDeposits: List<MonthlyDepositInput>,
    val ratePeriods: List<RatePeriodInput>,
    /** Last date interest accrues (inclusive), e.g. FD maturity or RD end. */
    val accrualEndDate: LocalDate? = null,
)

enum class CalculationStatus {
    OK,
    OPENING_AFTER_AS_OF,
    MISSING_RATE,
    INVALID_INPUT,
}

data class InterestCalculationResult(
    val status: CalculationStatus,
    val openingBalanceMinor: Long = 0L,
    val totalDepositsIncludedMinor: Long = 0L,
    val creditedInterestMinor: Long = 0L,
    val uncreditedCompletedMonthsInterestMinor: Long = 0L,
    val provisionalCurrentMonthMinor: Long = 0L,
    val estimatedTotalMinor: Long = 0L,
    val messages: List<String> = emptyList(),
) {
    /** Credited + accrued (not yet credited) + current-month provisional — excludes principal and deposits. */
    fun accruedInterestMinor(): Long =
        creditedInterestMinor + uncreditedCompletedMonthsInterestMinor + provisionalCurrentMonthMinor
}
