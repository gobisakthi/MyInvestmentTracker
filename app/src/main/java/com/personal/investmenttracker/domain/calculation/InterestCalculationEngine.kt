package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

object InterestCalculationEngine {

    fun calculate(input: InterestCalculationInput, asOfDate: LocalDate): InterestCalculationResult =
        simulate(input, asOfDate).result

    fun monthlyBreakdown(input: InterestCalculationInput, asOfDate: LocalDate): List<MonthlyInterestPoint> =
        simulate(input, asOfDate).monthlyPoints

    private data class SimulationResult(
        val result: InterestCalculationResult,
        val monthlyPoints: List<MonthlyInterestPoint>,
    )

    private fun simulate(input: InterestCalculationInput, asOfDate: LocalDate): SimulationResult {
        if (asOfDate.isBefore(input.openingBalanceDate)) {
            return SimulationResult(
                InterestCalculationResult(
                    status = CalculationStatus.OPENING_AFTER_AS_OF,
                    messages = listOf("As-of date is before the account opening balance date."),
                ),
                emptyList(),
            )
        }
        if (input.openingBalanceMinor < 0) {
            return SimulationResult(
                InterestCalculationResult(
                    status = CalculationStatus.INVALID_INPUT,
                    messages = listOf("Opening balance cannot be negative."),
                ),
                emptyList(),
            )
        }

        val firstMonth = firstAccrualYearMonth(input.openingBalanceDate)
        val asOfMonth = YearMonth.from(asOfDate)
        if (asOfMonth.isBefore(firstMonth)) {
            return SimulationResult(
                buildOk(input, input.openingBalanceMinor, 0L, 0L, 0L, 0L),
                emptyList(),
            )
        }

        val scheme = schemeRulesFor(input.accountType)
        val depositMap = input.monthlyDeposits.associateBy { it.yearMonth }
        val sortedRates = input.ratePeriods.sortedBy { it.effectiveYearMonth }

        var principalMinor = input.openingBalanceMinor
        var creditedInterestMinor = 0L
        var fyAccruedInterest = BigDecimal.ZERO
        var totalDepositsIncludedMinor = 0L
        var provisionalCurrentMonthMinor = 0L
        val monthlyPoints = mutableListOf<MonthlyInterestPoint>()

        var month = firstMonth
        while (!month.isAfter(asOfMonth)) {
            val rate = rateForMonth(sortedRates, month)
                ?: return SimulationResult(
                    InterestCalculationResult(
                        status = CalculationStatus.MISSING_RATE,
                        openingBalanceMinor = input.openingBalanceMinor,
                        totalDepositsIncludedMinor = totalDepositsIncludedMinor,
                        creditedInterestMinor = creditedInterestMinor,
                        uncreditedCompletedMonthsInterestMinor = fyAccruedInterest.toMinorHalfUp(),
                        messages = listOf("No interest rate defined for ${month.monthValue}/${month.year}."),
                    ),
                    monthlyPoints,
                )

            val depositMinor = depositForMonth(depositMap, month, input.openingBalanceDate)
            totalDepositsIncludedMinor += depositMinor

            val eligibleMinor = eligibleBalanceMinor(
                scheme.eligibleBalancePolicy,
                principalMinor,
                depositMinor,
            )
            val fullMonthInterest = monthlyInterestMinor(eligibleMinor, rate)

            val monthFullyComplete = month.isBefore(asOfMonth) ||
                (month == asOfMonth && asOfDate.dayOfMonth == month.lengthOfMonth())

            if (month == asOfMonth && !monthFullyComplete) {
                val elapsedDays = asOfDate.dayOfMonth - 1
                val daysInMonth = month.lengthOfMonth()
                provisionalCurrentMonthMinor = if (elapsedDays <= 0) {
                    0L
                } else {
                    fullMonthInterest
                        .multiply(BigDecimal.valueOf(elapsedDays.toLong()))
                        .divide(BigDecimal.valueOf(daysInMonth.toLong()), 12, RoundingMode.HALF_UP)
                        .toMinorHalfUp()
                }
                monthlyPoints += MonthlyInterestPoint(month, provisionalCurrentMonthMinor, isProvisional = true)
                principalMinor += depositMinor
            } else {
                val monthInterestMinor = fullMonthInterest.toMinorHalfUp()
                monthlyPoints += MonthlyInterestPoint(month, monthInterestMinor, isProvisional = false)
                fyAccruedInterest = fyAccruedInterest.add(fullMonthInterest)
                principalMinor += depositMinor
                if (isFinancialYearEndMonth(month)) {
                    val creditMinor = fyAccruedInterest.toMinorHalfUp()
                    creditedInterestMinor += creditMinor
                    principalMinor += creditMinor
                    fyAccruedInterest = BigDecimal.ZERO
                }
            }

            month = month.plusMonths(1)
        }

        val uncreditedMinor = fyAccruedInterest.toMinorHalfUp()
        return SimulationResult(
            buildOk(
                input,
                principalMinor,
                totalDepositsIncludedMinor,
                creditedInterestMinor,
                uncreditedMinor,
                provisionalCurrentMonthMinor,
            ),
            monthlyPoints,
        )
    }

    private fun buildOk(
        input: InterestCalculationInput,
        principalMinor: Long,
        totalDepositsMinor: Long,
        creditedInterestMinor: Long,
        uncreditedMinor: Long,
        provisionalMinor: Long,
    ): InterestCalculationResult {
        val estimatedTotalMinor = principalMinor + uncreditedMinor + provisionalMinor
        val messages = buildList {
            if (input.accountType == InvestmentType.PF) {
                add("PF/EPF value is an estimate from a single monthly deposit; not an official EPFO statement.")
            }
            if (provisionalMinor > 0L) {
                add("Total includes provisional current-month interest (not capitalized).")
            }
        }
        return InterestCalculationResult(
            status = CalculationStatus.OK,
            openingBalanceMinor = input.openingBalanceMinor,
            totalDepositsIncludedMinor = totalDepositsMinor,
            creditedInterestMinor = creditedInterestMinor,
            uncreditedCompletedMonthsInterestMinor = uncreditedMinor,
            provisionalCurrentMonthMinor = provisionalMinor,
            estimatedTotalMinor = estimatedTotalMinor,
            messages = messages,
        )
    }

    private fun rateForMonth(rates: List<RatePeriodInput>, month: YearMonth): Int? =
        rates.filter { !it.effectiveYearMonth.isAfter(month) }
            .maxByOrNull { it.effectiveYearMonth }
            ?.annualRateBasisPoints

    private fun depositForMonth(
        depositMap: Map<YearMonth, MonthlyDepositInput>,
        month: YearMonth,
        openingDate: LocalDate,
    ): Long {
        if (month.isBefore(YearMonth.from(openingDate))) return 0L
        return depositMap[month]?.amountMinor ?: 0L
    }
}
