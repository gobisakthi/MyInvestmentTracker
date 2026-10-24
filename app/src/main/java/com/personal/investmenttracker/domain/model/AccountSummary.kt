package com.personal.investmenttracker.domain.model

import java.time.LocalDate

/**
 * Read model for dashboard and lists. Values will be supplied by the calculation layer in a later milestone.
 */
data class AccountSummary(
    val id: Long,
    val name: String,
    val type: InvestmentType,
    val estimatedCurrentValueMinor: Long,
    val asOfDate: LocalDate,
)
