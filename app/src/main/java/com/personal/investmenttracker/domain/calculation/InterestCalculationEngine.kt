package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

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

        val effectiveAsOf = input.accrualEndDate?.let { end -> minOf(asOfDate, end) } ?: asOfDate
        val firstMonth = when (input.accountType) {
            InvestmentType.FD,
            InvestmentType.RD,
            -> YearMonth.from(input.openingBalanceDate)
            InvestmentType.PPF,
            InvestmentType.SSA,
            InvestmentType.PF,
            InvestmentType.STOCKS,
            InvestmentType.METALS,
            InvestmentType.REAL_ESTATE,
            InvestmentType.NPS,
            -> firstAccrualYearMonth(input.openingBalanceDate)
        }
        val asOfMonth = YearMonth.from(effectiveAsOf)
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
                (month == asOfMonth && isMonthAccrualComplete(month, effectiveAsOf, input.accrualEndDate))

            if (month == asOfMonth && !monthFullyComplete) {
                val elapsedDays = provisionalElapsedDays(
                    month = month,
                    asOf = effectiveAsOf,
                    openingDate = input.openingBalanceDate,
                    accrualEnd = input.accrualEndDate,
                )
                val daysInMonth = accrualDaysInMonth(
                    month = month,
                    openingDate = input.openingBalanceDate,
                    accrualEnd = input.accrualEndDate,
                )
                provisionalCurrentMonthMinor = if (elapsedDays <= 0L || daysInMonth <= 0L) {
                    0L
                } else {
                    fullMonthInterest
                        .multiply(BigDecimal.valueOf(elapsedDays))
                        .divide(BigDecimal.valueOf(daysInMonth), 12, RoundingMode.HALF_UP)
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
            if (input.accountType == InvestmentType.FD || input.accountType == InvestmentType.RD) {
                add("FD/RD estimate uses monthly rate with daily proration in the current month (like PPF).")
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

    private fun isMonthAccrualComplete(
        month: YearMonth,
        asOf: LocalDate,
        accrualEnd: LocalDate?,
    ): Boolean {
        if (asOf.dayOfMonth == month.lengthOfMonth()) return true
        if (accrualEnd != null && YearMonth.from(accrualEnd) == month && !asOf.isBefore(accrualEnd)) {
            return true
        }
        return false
    }

    private fun provisionalElapsedDays(
        month: YearMonth,
        asOf: LocalDate,
        openingDate: LocalDate,
        accrualEnd: LocalDate?,
    ): Long {
        val accrualStart = accrualPeriodStartInMonth(month, openingDate)
        val accrualEndInMonth = accrualPeriodEndInMonth(month, accrualEnd)
        val effectiveEnd = minOf(asOf, accrualEndInMonth)
        if (effectiveEnd.isBefore(accrualStart)) return 0L
        return ChronoUnit.DAYS.between(accrualStart, effectiveEnd).coerceAtLeast(0L)
    }

    private fun accrualDaysInMonth(
        month: YearMonth,
        openingDate: LocalDate,
        accrualEnd: LocalDate?,
    ): Long {
        val accrualStart = accrualPeriodStartInMonth(month, openingDate)
        val accrualEndInMonth = accrualPeriodEndInMonth(month, accrualEnd)
        if (accrualEndInMonth.isBefore(accrualStart)) return 0L
        return ChronoUnit.DAYS.between(accrualStart, accrualEndInMonth) + 1L
    }

    private fun accrualPeriodStartInMonth(month: YearMonth, openingDate: LocalDate): LocalDate {
        val monthStart = month.atDay(1)
        return if (YearMonth.from(openingDate) == month && openingDate.isAfter(monthStart)) {
            openingDate
        } else {
            monthStart
        }
    }

    private fun accrualPeriodEndInMonth(month: YearMonth, accrualEnd: LocalDate?): LocalDate {
        val monthEnd = month.atEndOfMonth()
        if (accrualEnd == null || YearMonth.from(accrualEnd) != month) return monthEnd
        return minOf(monthEnd, accrualEnd)
    }
}
