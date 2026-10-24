package com.personal.investmenttracker.domain.validation

import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.util.Money
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

data class ValidatedAccountForm(
    val name: String,
    val type: InvestmentType,
    val openingBalanceMinor: Long,
    val openingBalanceDate: LocalDate,
    val openingBalanceIncludesCreditedInterest: Boolean,
    val notes: String?,
    val fixedAnnualRateBasisPoints: Int? = null,
    val termEndDate: LocalDate? = null,
    val rdMonthlyAmountMinor: Long? = null,
    val termStartYear: Int? = null,
    val termStartMonth: Int? = null,
    val termEndYear: Int? = null,
    val termEndMonth: Int? = null,
    val shareQuantity: Double? = null,
    val currentSharePriceMinor: Long? = null,
    val currentValueMinor: Long? = null,
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
        annualRatePercentInput: String,
        termEndDateInput: String = "",
        rdMonthlyAmountInput: String = "",
        rdStartYearMonthInput: String = "",
        rdEndYearMonthInput: String = "",
        shareQuantityInput: String = "",
        sharePriceInput: String = "",
        currentValueInput: String = "",
    ): AccountFormValidationResult {
        val errors = mutableListOf<String>()
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            errors.add("Account name is required.")
        }
        if (type == null) {
            errors.add("Investment type is required.")
            return AccountFormValidationResult.Invalid(errors)
        }

        val rateBps = parseRateBasisPoints(annualRatePercentInput, errors)

        return when (type) {
            InvestmentType.FD -> validateFixedDeposit(
                errors, trimmedName, type, openingBalanceInput, balanceDateInput,
                openingBalanceIncludesCreditedInterest, notes, termEndDateInput, rateBps,
            )
            InvestmentType.RD -> validateRecurringDeposit(
                errors, trimmedName, type, openingBalanceIncludesCreditedInterest, notes,
                rdMonthlyAmountInput, rdStartYearMonthInput, rdEndYearMonthInput, rateBps,
            )
            InvestmentType.PPF, InvestmentType.SSA, InvestmentType.PF -> validateAccrualAccount(
                errors, trimmedName, type, openingBalanceInput, balanceDateInput,
                openingBalanceIncludesCreditedInterest, notes,
            )
            InvestmentType.STOCKS -> validateQuantityPriceHoldings(
                errors, trimmedName, InvestmentType.STOCKS, openingBalanceIncludesCreditedInterest, notes,
                shareQuantityInput, sharePriceInput, "Number of shares", "Share price",
            )
            InvestmentType.METALS -> validateQuantityPriceHoldings(
                errors, trimmedName, InvestmentType.METALS, openingBalanceIncludesCreditedInterest, notes,
                shareQuantityInput, sharePriceInput, "Quantity", "Price per unit",
            )
            InvestmentType.REAL_ESTATE -> validateManualValueHoldings(
                errors, trimmedName, InvestmentType.REAL_ESTATE, openingBalanceIncludesCreditedInterest, notes,
                currentValueInput, "Property value",
            )
            InvestmentType.NPS -> validateManualValueHoldings(
                errors, trimmedName, InvestmentType.NPS, openingBalanceIncludesCreditedInterest, notes,
                currentValueInput, "Current value",
            )
        }
    }

    private fun validateQuantityPriceHoldings(
        errors: MutableList<String>,
        name: String,
        type: InvestmentType,
        includesCredited: Boolean,
        notes: String?,
        quantityInput: String,
        priceInput: String,
        quantityLabel: String,
        priceLabel: String,
    ): AccountFormValidationResult {
        val quantity = parseQuantity(quantityInput, errors, quantityLabel)
        val priceMinor = parseMoney(priceInput, errors, priceLabel)
        if (errors.isNotEmpty()) return AccountFormValidationResult.Invalid(errors)
        val marketValue = BigDecimal.valueOf(quantity!!)
            .multiply(BigDecimal.valueOf(priceMinor!!))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = name,
                type = type,
                openingBalanceMinor = marketValue,
                openingBalanceDate = LocalDate.now(),
                openingBalanceIncludesCreditedInterest = includesCredited,
                notes = cleanNotes(notes),
                shareQuantity = quantity,
                currentSharePriceMinor = priceMinor,
            ),
        )
    }

    private fun validateManualValueHoldings(
        errors: MutableList<String>,
        name: String,
        type: InvestmentType,
        includesCredited: Boolean,
        notes: String?,
        valueInput: String,
        valueLabel: String,
    ): AccountFormValidationResult {
        val valueMinor = parseMoney(valueInput, errors, valueLabel)
        if (errors.isNotEmpty()) return AccountFormValidationResult.Invalid(errors)
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = name,
                type = type,
                openingBalanceMinor = valueMinor!!,
                openingBalanceDate = LocalDate.now(),
                openingBalanceIncludesCreditedInterest = includesCredited,
                notes = cleanNotes(notes),
                currentValueMinor = valueMinor,
            ),
        )
    }

    private fun parseQuantity(input: String, errors: MutableList<String>, label: String): Double? {
        if (input.trim().isEmpty()) {
            errors.add("$label is required.")
            return null
        }
        val value = input.trim().toBigDecimalOrNull()
        if (value == null || value <= BigDecimal.ZERO) {
            errors.add("$label must be a positive number.")
            return null
        }
        return value.toDouble()
    }

    private fun validateAccrualAccount(
        errors: MutableList<String>,
        name: String,
        type: InvestmentType,
        openingBalanceInput: String,
        balanceDateInput: String,
        includesCredited: Boolean,
        notes: String?,
    ): AccountFormValidationResult {
        val money = parseMoney(openingBalanceInput, errors, "Principal")
        val balanceDate = parseDate(balanceDateInput, errors, "Start")
        if (errors.isNotEmpty()) return AccountFormValidationResult.Invalid(errors)
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = name,
                type = type,
                openingBalanceMinor = money!!,
                openingBalanceDate = balanceDate!!,
                openingBalanceIncludesCreditedInterest = includesCredited,
                notes = cleanNotes(notes),
            ),
        )
    }

    private fun validateFixedDeposit(
        errors: MutableList<String>,
        name: String,
        type: InvestmentType,
        principalInput: String,
        startDateInput: String,
        includesCredited: Boolean,
        notes: String?,
        endDateInput: String,
        rateBps: Int?,
    ): AccountFormValidationResult {
        val money = parseMoney(principalInput, errors, "Principal")
        val startDate = parseDate(startDateInput, errors, "Start")
        val endDate = parseDate(endDateInput, errors, "End")
        if (rateBps == null) errors.add("Annual interest rate (%) is required.")
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.add("End date must be on or after start date.")
        }
        if (errors.isNotEmpty()) return AccountFormValidationResult.Invalid(errors)
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = name,
                type = type,
                openingBalanceMinor = money!!,
                openingBalanceDate = startDate!!,
                openingBalanceIncludesCreditedInterest = includesCredited,
                notes = cleanNotes(notes),
                fixedAnnualRateBasisPoints = rateBps,
                termEndDate = endDate,
            ),
        )
    }

    private fun validateRecurringDeposit(
        errors: MutableList<String>,
        name: String,
        type: InvestmentType,
        includesCredited: Boolean,
        notes: String?,
        monthlyInput: String,
        startYmInput: String,
        endYmInput: String,
        rateBps: Int?,
    ): AccountFormValidationResult {
        val monthly = parseMoney(monthlyInput, errors, "Monthly investment")
        val startYm = parseYearMonth(startYmInput, errors, "Start month")
        val endYm = parseYearMonth(endYmInput, errors, "End month")
        if (rateBps == null) errors.add("Annual interest rate (%) is required.")
        if (startYm != null && endYm != null && endYm.isBefore(startYm)) {
            errors.add("End month must be on or after start month.")
        }
        if (errors.isNotEmpty()) return AccountFormValidationResult.Invalid(errors)
        return AccountFormValidationResult.Valid(
            ValidatedAccountForm(
                name = name,
                type = type,
                openingBalanceMinor = 0L,
                openingBalanceDate = startYm!!.atDay(1),
                openingBalanceIncludesCreditedInterest = includesCredited,
                notes = cleanNotes(notes),
                fixedAnnualRateBasisPoints = rateBps,
                rdMonthlyAmountMinor = monthly,
                termStartYear = startYm.year,
                termStartMonth = startYm.monthValue,
                termEndYear = endYm!!.year,
                termEndMonth = endYm.monthValue,
            ),
        )
    }

    private fun parseMoney(input: String, errors: MutableList<String>, label: String): Long? {
        if (input.trim().isEmpty()) {
            errors.add("$label amount is required.")
            return null
        }
        val money = Money.fromRupeesString(input)
        if (money == null) {
            errors.add("$label must be a valid non-negative amount.")
            return null
        }
        return money.amountMinor
    }

    private fun parseDate(input: String, errors: MutableList<String>, label: String): LocalDate? {
        if (input.trim().isEmpty()) {
            errors.add("$label date is required.")
            return null
        }
        return try {
            LocalDate.parse(input.trim())
        } catch (_: DateTimeParseException) {
            errors.add("$label date must be YYYY-MM-DD.")
            null
        }
    }

    private fun parseYearMonth(input: String, errors: MutableList<String>, label: String): YearMonth? {
        if (input.trim().isEmpty()) {
            errors.add("$label is required (YYYY-MM).")
            return null
        }
        return try {
            YearMonth.parse(input.trim())
        } catch (_: DateTimeParseException) {
            errors.add("$label must be YYYY-MM.")
            null
        }
    }

    private fun parseRateBasisPoints(input: String, errors: MutableList<String>): Int? {
        if (input.trim().isEmpty()) return null
        val percent = input.trim().toBigDecimalOrNull()
        if (percent == null || percent < BigDecimal.ZERO) {
            errors.add("Enter a valid annual interest rate (%).")
            return null
        }
        return percent.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact()
    }

    private fun cleanNotes(notes: String?): String? = notes?.trim()?.takeIf { it.isNotEmpty() }
}
