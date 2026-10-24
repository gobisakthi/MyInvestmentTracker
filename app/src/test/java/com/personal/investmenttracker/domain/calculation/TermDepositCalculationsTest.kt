package com.personal.investmenttracker.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TermDepositCalculationsTest {

    @Test
    fun fixedDeposit_dailyProvisional_increasesWithAsOf() {
        val terms = FixedDepositTerms(
            principalMinor = 1_000_000L,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2026, 1, 1),
            annualRateBasisPoints = 700,
        )
        val day5 = TermDepositCalculations.calculateFixedDeposit(terms, LocalDate.of(2024, 10, 5))
        val day6 = TermDepositCalculations.calculateFixedDeposit(terms, LocalDate.of(2024, 10, 6))
        assertEquals(CalculationStatus.OK, day5.status)
        assertEquals(CalculationStatus.OK, day6.status)
        assertTrue(day6.estimatedTotalMinor > day5.estimatedTotalMinor)
        assertTrue(day6.provisionalCurrentMonthMinor >= day5.provisionalCurrentMonthMinor)
    }

    @Test
    fun recurringDeposit_dailyChange_betweenDays() {
        val terms = RecurringDepositTerms(
            monthlyAmountMinor = 500_000L,
            startMonth = java.time.YearMonth.of(2024, 1),
            endMonth = java.time.YearMonth.of(2025, 12),
            annualRateBasisPoints = 650,
        )
        val input = TermDepositCalculations.buildRecurringDepositInput(terms)!!
        val d1 = InterestCalculationEngine.calculate(input, LocalDate.of(2024, 6, 10))
        val d2 = InterestCalculationEngine.calculate(input, LocalDate.of(2024, 6, 11))
        assertEquals(CalculationStatus.OK, d1.status)
        assertTrue(d2.estimatedTotalMinor > d1.estimatedTotalMinor)
    }
}
