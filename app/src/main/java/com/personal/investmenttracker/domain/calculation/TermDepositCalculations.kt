package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate
import java.time.YearMonth

data class FixedDepositTerms(
    val principalMinor: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val annualRateBasisPoints: Int,
)

data class RecurringDepositTerms(
    val monthlyAmountMinor: Long,
    val startMonth: YearMonth,
    val endMonth: YearMonth,
    val annualRateBasisPoints: Int,
)

object TermDepositCalculations {

    fun buildFixedDepositInput(terms: FixedDepositTerms): InterestCalculationInput {
        val startMonth = YearMonth.from(terms.startDate)
        return InterestCalculationInput(
            accountType = InvestmentType.FD,
            openingBalanceMinor = terms.principalMinor,
            openingBalanceDate = terms.startDate,
            openingBalanceIncludesCreditedInterest = true,
            monthlyDeposits = emptyList(),
            ratePeriods = listOf(
                RatePeriodInput(
                    effectiveYear = startMonth.year,
                    effectiveMonth = startMonth.monthValue,
                    annualRateBasisPoints = terms.annualRateBasisPoints,
                ),
            ),
            accrualEndDate = terms.endDate,
        )
    }

    fun calculateFixedDeposit(terms: FixedDepositTerms, asOfDate: LocalDate): InterestCalculationResult {
        if (terms.endDate.isBefore(terms.startDate)) {
            return InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("End date must be on or after start date."),
            )
        }
        return InterestCalculationEngine.calculate(buildFixedDepositInput(terms), asOfDate)
    }

    fun fdMonthlyBreakdown(terms: FixedDepositTerms, asOfDate: LocalDate): List<MonthlyInterestPoint> =
        InterestCalculationEngine.monthlyBreakdown(buildFixedDepositInput(terms), asOfDate)

    fun buildRecurringDepositInput(terms: RecurringDepositTerms): InterestCalculationInput? {
        if (terms.endMonth.isBefore(terms.startMonth)) return null
        if (terms.monthlyAmountMinor <= 0L) return null
        val deposits = mutableListOf<MonthlyDepositInput>()
        var month = terms.startMonth
        while (!month.isAfter(terms.endMonth)) {
            deposits += MonthlyDepositInput(month.year, month.monthValue, terms.monthlyAmountMinor)
            month = month.plusMonths(1)
        }
        return InterestCalculationInput(
            accountType = InvestmentType.RD,
            openingBalanceMinor = 0L,
            openingBalanceDate = terms.startMonth.atDay(1),
            openingBalanceIncludesCreditedInterest = true,
            monthlyDeposits = deposits,
            ratePeriods = listOf(
                RatePeriodInput(terms.startMonth.year, terms.startMonth.monthValue, terms.annualRateBasisPoints),
            ),
            accrualEndDate = terms.endMonth.atEndOfMonth(),
        )
    }
}
