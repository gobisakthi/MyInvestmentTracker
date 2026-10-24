package com.personal.investmenttracker.domain.model

import java.time.LocalDate

/**
 * Read model for dashboard and lists. [recordedBalanceMinor] is the persisted opening balance
 * until the calculation engine produces estimated values in a later milestone.
 */
data class AccountSummary(
    val id: Long,
    val name: String,
    val type: InvestmentType,
    val recordedBalanceMinor: Long,
    val balanceEffectiveDate: LocalDate,
)
