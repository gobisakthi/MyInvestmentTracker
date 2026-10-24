package com.personal.investmenttracker.domain.validation

import com.personal.investmenttracker.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountFormValidatorTest {

    @Test
    fun validForm_passesValidation() {
        val result = AccountFormValidator.validate(
            name = "My PPF",
            type = InvestmentType.PPF,
            openingBalanceInput = "100000",
            balanceDateInput = "2024-04-01",
            openingBalanceIncludesCreditedInterest = true,
            notes = "  test note  ",
            annualRatePercentInput = "",
        )
        assertTrue(result is AccountFormValidationResult.Valid)
        val form = (result as AccountFormValidationResult.Valid).form
        assertEquals("My PPF", form.name)
        assertEquals(10_000_000L, form.openingBalanceMinor)
        assertEquals("test note", form.notes)
    }

    @Test
    fun emptyName_isInvalid() {
        val result = AccountFormValidator.validate(
            name = "  ",
            type = InvestmentType.SSA,
            openingBalanceInput = "1",
            balanceDateInput = "2024-01-01",
            openingBalanceIncludesCreditedInterest = false,
            notes = null,
            annualRatePercentInput = "",
        )
        assertTrue(result is AccountFormValidationResult.Invalid)
        assertTrue((result as AccountFormValidationResult.Invalid).errors.any { it.contains("name", ignoreCase = true) })
    }

    @Test
    fun invalidDate_isInvalid() {
        val result = AccountFormValidator.validate(
            name = "PF",
            type = InvestmentType.PF,
            openingBalanceInput = "500",
            balanceDateInput = "01-04-2024",
            openingBalanceIncludesCreditedInterest = false,
            notes = null,
            annualRatePercentInput = "",
        )
        assertTrue(result is AccountFormValidationResult.Invalid)
    }

    @Test
    fun negativeBalanceInput_isInvalid() {
        val result = AccountFormValidator.validate(
            name = "PF",
            type = InvestmentType.PF,
            openingBalanceInput = "-1",
            balanceDateInput = "2024-01-01",
            openingBalanceIncludesCreditedInterest = false,
            notes = null,
            annualRatePercentInput = "",
        )
        assertTrue(result is AccountFormValidationResult.Invalid)
    }

    @Test
    fun fixedDeposit_validatesTermFields() {
        val result = AccountFormValidator.validate(
            name = "HDFC FD",
            type = InvestmentType.FD,
            openingBalanceInput = "100000",
            balanceDateInput = "2024-01-01",
            openingBalanceIncludesCreditedInterest = true,
            notes = null,
            annualRatePercentInput = "7.5",
            termEndDateInput = "2026-01-01",
        )
        assertTrue(result is AccountFormValidationResult.Valid)
        val form = (result as AccountFormValidationResult.Valid).form
        assertEquals(750, form.fixedAnnualRateBasisPoints)
    }

    @Test
    fun recurringDeposit_validatesMonthRange() {
        val result = AccountFormValidator.validate(
            name = "Post Office RD",
            type = InvestmentType.RD,
            openingBalanceInput = "",
            balanceDateInput = "",
            openingBalanceIncludesCreditedInterest = true,
            notes = null,
            annualRatePercentInput = "6.5",
            rdMonthlyAmountInput = "5000",
            rdStartYearMonthInput = "2024-01",
            rdEndYearMonthInput = "2025-12",
        )
        assertTrue(result is AccountFormValidationResult.Valid)
        val form = (result as AccountFormValidationResult.Valid).form
        assertEquals(500_000L, form.rdMonthlyAmountMinor)
        assertEquals(2024, form.termStartYear)
    }

    @Test
    fun stocks_validatesQuantityAndPrice() {
        val result = AccountFormValidator.validate(
            name = "Reliance",
            type = InvestmentType.STOCKS,
            openingBalanceInput = "",
            balanceDateInput = "",
            openingBalanceIncludesCreditedInterest = true,
            notes = null,
            annualRatePercentInput = "",
            shareQuantityInput = "10",
            sharePriceInput = "2500.50",
        )
        assertTrue(result is AccountFormValidationResult.Valid)
        val form = (result as AccountFormValidationResult.Valid).form
        assertEquals(10.0, form.shareQuantity!!, 0.001)
        assertEquals(250_050L, form.currentSharePriceMinor)
        assertEquals(2_500_500L, form.openingBalanceMinor)
    }
}
