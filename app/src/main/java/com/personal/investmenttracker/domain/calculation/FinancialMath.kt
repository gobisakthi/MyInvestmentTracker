package com.personal.investmenttracker.domain.calculation

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

private val CALC_MATH_CONTEXT = RoundingMode.HALF_UP

/** Monthly interest from annual basis points on an eligible balance in paise. */
fun monthlyInterestMinor(
    eligibleBalanceMinor: Long,
    annualRateBasisPoints: Int,
): BigDecimal {
    if (eligibleBalanceMinor == 0L || annualRateBasisPoints == 0) return BigDecimal.ZERO
    val balance = BigDecimal.valueOf(eligibleBalanceMinor)
    val annualRate = BigDecimal.valueOf(annualRateBasisPoints.toLong())
        .divide(BigDecimal.valueOf(10_000L), 12, CALC_MATH_CONTEXT)
    return balance.multiply(annualRate)
        .divide(BigDecimal.valueOf(12L), 12, CALC_MATH_CONTEXT)
}

/** Calendar-day interest for dashboard (avoids month-length jumps at month boundaries). */
fun dailyInterestMinor365(
    eligibleBalanceMinor: Long,
    annualRateBasisPoints: Int,
): Long {
    if (eligibleBalanceMinor == 0L || annualRateBasisPoints == 0) return 0L
    val balance = BigDecimal.valueOf(eligibleBalanceMinor)
    val annualRate = BigDecimal.valueOf(annualRateBasisPoints.toLong())
        .divide(BigDecimal.valueOf(10_000L), 12, CALC_MATH_CONTEXT)
    return balance.multiply(annualRate)
        .divide(BigDecimal.valueOf(365L), 12, CALC_MATH_CONTEXT)
        .toMinorHalfUp()
}

fun BigDecimal.toMinorHalfUp(): Long =
    setScale(0, CALC_MATH_CONTEXT).longValueExact()

fun firstAccrualYearMonth(openingBalanceDate: LocalDate): YearMonth {
    return if (openingBalanceDate.dayOfMonth == 1) {
        YearMonth.from(openingBalanceDate)
    } else {
        YearMonth.from(openingBalanceDate).plusMonths(1)
    }
}

fun isFinancialYearEndMonth(yearMonth: YearMonth): Boolean = yearMonth.monthValue == 3
