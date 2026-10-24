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
        )
        assertTrue(result is AccountFormValidationResult.Invalid)
    }
}
