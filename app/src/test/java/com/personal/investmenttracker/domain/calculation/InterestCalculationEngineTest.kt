package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InterestCalculationEngineTest {

    private val openingMinor = 100_000_000L // ₹10,00,000.00
    private val openingDate = LocalDate.of(2026, 3, 31)

    private fun baseInput(
        type: InvestmentType = InvestmentType.PPF,
        deposits: List<MonthlyDepositInput> = emptyList(),
        rates: List<RatePeriodInput> = listOf(
            RatePeriodInput(2026, 4, 710),
        ),
    ) = InterestCalculationInput(
        accountType = type,
        openingBalanceMinor = openingMinor,
        openingBalanceDate = openingDate,
        openingBalanceIncludesCreditedInterest = true,
        monthlyDeposits = deposits,
        ratePeriods = rates,
    )

    @Test
    fun testA_openingBalance_notDoubleCounted() {
        val input = baseInput()
        val onOpening = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 3, 31))
        assertEquals(CalculationStatus.OK, onOpening.status)
        assertEquals(openingMinor, onOpening.estimatedTotalMinor)

        val endApril = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 4, 30))
        assertEquals(CalculationStatus.OK, endApril.status)
        assertTrue(endApril.estimatedTotalMinor > openingMinor)
        assertEquals(openingMinor, endApril.openingBalanceMinor)
    }

    @Test
    fun testB_monthlyDeposit_appliesFromMayOnly() {
        val deposit = MonthlyDepositInput(2026, 5, 1_000_000L) // ₹10,000
        val input = baseInput(deposits = listOf(deposit))

        val aprilEnd = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 4, 30))
        val mayEnd = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 5, 31))

        assertTrue(mayEnd.estimatedTotalMinor > aprilEnd.estimatedTotalMinor)
        assertEquals(0L, aprilEnd.totalDepositsIncludedMinor)
        assertEquals(1_000_000L, mayEnd.totalDepositsIncludedMinor)
    }

    @Test
    fun testC_rateChange_effectiveFromJune() {
        val rates = listOf(
            RatePeriodInput(2026, 4, 710),
            RatePeriodInput(2026, 6, 740),
        )
        val input = baseInput(rates = rates)

        val mayEnd = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 5, 31))
        val juneEnd = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 6, 30))

        assertEquals(CalculationStatus.OK, mayEnd.status)
        assertEquals(CalculationStatus.OK, juneEnd.status)
        assertTrue(juneEnd.uncreditedCompletedMonthsInterestMinor >= mayEnd.uncreditedCompletedMonthsInterestMinor)
    }

    @Test
    fun testD_marchBoundary_creditsInterestOnce() {
        val input = baseInput()
        val beforeCredit = InterestCalculationEngine.calculate(input, LocalDate.of(2027, 3, 30))
        val onCredit = InterestCalculationEngine.calculate(input, LocalDate.of(2027, 3, 31))

        assertTrue(onCredit.creditedInterestMinor > beforeCredit.creditedInterestMinor)
        assertTrue(onCredit.uncreditedCompletedMonthsInterestMinor <= beforeCredit.uncreditedCompletedMonthsInterestMinor)
    }

    @Test
    fun testE_provisionalInterest_elapsedDaysRule() {
        val input = baseInput()
        val oct24 = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 10, 24))
        val oct31 = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 10, 31))

        assertTrue(oct24.provisionalCurrentMonthMinor > 0L)
        assertEquals(0L, oct31.provisionalCurrentMonthMinor)
        assertTrue(oct31.estimatedTotalMinor > oct24.estimatedTotalMinor)
        val ratio = oct24.provisionalCurrentMonthMinor.toDouble() /
            (oct31.uncreditedCompletedMonthsInterestMinor - oct24.uncreditedCompletedMonthsInterestMinor + oct31.provisionalCurrentMonthMinor.coerceAtLeast(1)).toDouble()
        assertTrue(ratio < 1.0)
    }

    @Test
    fun testF_historical_deterministic() {
        val input = baseInput(
            deposits = listOf(MonthlyDepositInput(2026, 5, 1_000_000L)),
            rates = listOf(
                RatePeriodInput(2026, 4, 710),
                RatePeriodInput(2026, 6, 740),
            ),
        )
        val d1 = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 5, 31))
        val d2 = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 5, 31))
        assertEquals(d1.estimatedTotalMinor, d2.estimatedTotalMinor)
        val beforeDepositMonth = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 4, 30))
        assertTrue(d1.estimatedTotalMinor > beforeDepositMonth.estimatedTotalMinor)
    }

    @Test
    fun testG_backdatedDepositChangesLaterResults() {
        val low = baseInput(deposits = listOf(MonthlyDepositInput(2026, 5, 500_000L)))
        val high = baseInput(deposits = listOf(MonthlyDepositInput(2026, 5, 1_000_000L)))
        val juneLow = InterestCalculationEngine.calculate(low, LocalDate.of(2026, 6, 30))
        val juneHigh = InterestCalculationEngine.calculate(high, LocalDate.of(2026, 6, 30))
        assertTrue(juneHigh.estimatedTotalMinor > juneLow.estimatedTotalMinor)
        val april = InterestCalculationEngine.calculate(high, LocalDate.of(2026, 4, 30))
        val aprilFromLow = InterestCalculationEngine.calculate(low, LocalDate.of(2026, 4, 30))
        assertEquals(april.estimatedTotalMinor, aprilFromLow.estimatedTotalMinor)
    }

    @Test
    fun testH_calendarBoundaries() {
        val input = baseInput()
        assertEquals(
            CalculationStatus.OPENING_AFTER_AS_OF,
            InterestCalculationEngine.calculate(input, LocalDate.of(2024, 2, 29)).status,
        )
        assertEquals(CalculationStatus.OK, InterestCalculationEngine.calculate(input, LocalDate.of(2026, 4, 1)).status)
        assertEquals(CalculationStatus.OK, InterestCalculationEngine.calculate(input, LocalDate.of(2026, 3, 31)).status)
        assertEquals(CalculationStatus.OK, InterestCalculationEngine.calculate(input, LocalDate.of(2028, 2, 29)).status)
    }

    @Test
    fun testI_allAccountTypes_useSharedEngine() {
        val types = listOf(InvestmentType.PPF, InvestmentType.SSA, InvestmentType.PF)
        val results = types.map {
            InterestCalculationEngine.calculate(baseInput(type = it), LocalDate.of(2026, 6, 30))
        }
        assertTrue(results.all { it.status == CalculationStatus.OK })
        assertEquals(results.first().estimatedTotalMinor, results[1].estimatedTotalMinor)
        assertEquals(results.first().estimatedTotalMinor, results[2].estimatedTotalMinor)
    }

    @Test
    fun testK_missingRate_returnsStatus() {
        val input = baseInput(rates = emptyList())
        val result = InterestCalculationEngine.calculate(input, LocalDate.of(2026, 4, 30))
        assertEquals(CalculationStatus.MISSING_RATE, result.status)
    }

    @Test
    fun testK_openingAfterAsOf_invalid() {
        val result = InterestCalculationEngine.calculate(baseInput(), LocalDate.of(2026, 3, 1))
        assertEquals(CalculationStatus.OPENING_AFTER_AS_OF, result.status)
    }
}
