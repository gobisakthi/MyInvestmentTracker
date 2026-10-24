package com.personal.investmenttracker.domain.validation

import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.util.Money
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class ValidatedAccountForm(
    val name: String,
    val type: InvestmentType,
    val openingBalanceMinor: Long,
    val openingBalanceDate: LocalDate,
    val openingBalanceIncludesCreditedInterest: Boolean,
    val notes: String?,
)

sealed class AccountFormValidationResult {
    data class Valid(val form: ValidatedAccountForm) : AccountFormValidationResult()
    data class Invalid(val errors: List<String>) : AccountFormValidationResult()
}

object AccountFormValidator {

    fun validate(
        name: String,
        type: InvestmentType?,
        openingBalanceInput: String,
        balanceDateInput: String,
        openingBalanceIncludesCreditedInterest: Boolean,
        notes: String?,
    ): AccountFormValidationResult {
        val errors = mutableListOf<String>()
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            errors.add("Account name is required.")
        }
        if (type == null) {
            errors.add("Investment type is required.")
        }
        val money = Money.fromRupeesString(openingBalanceInput)
        if (openingBalanceInput.trim().isEmpty()) {
            errors.add("Opening balance is required.")
        } else if (money == null) {
            errors.add("Opening balance must be a valid non-negative amount.")
        }
        val balanceDate = parseDate(balanceDateInput)
        if (balanceDateInput.trim().isEmpty()) {
            errors.add("Balance effective date is required.")
        } else if (balanceDate == null) {
            errors.add("Balance effective date must be YYYY-MM-DD.")
        }
        if (errors.isNotEmpty()) {
            return AccountFormValidationResult.Invalid(errors)
        }
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = trimmedName,
                type = type!!,
                openingBalanceMinor = money!!.amountMinor,
                openingBalanceDate = balanceDate!!,
                openingBalanceIncludesCreditedInterest = openingBalanceIncludesCreditedInterest,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    private fun parseDate(input: String): LocalDate? {
        return try {
            LocalDate.parse(input.trim())
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
