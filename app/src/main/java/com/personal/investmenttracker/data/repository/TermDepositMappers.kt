package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.domain.calculation.FixedDepositTerms
import com.personal.investmenttracker.domain.calculation.RecurringDepositTerms
import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.YearMonth

fun AccountEntity.toFixedDepositTerms(): FixedDepositTerms? {
    if (type != InvestmentType.FD) return null
    val end = termEndDate ?: return null
    val bps = fixedAnnualRateBasisPoints ?: return null
    return FixedDepositTerms(
        principalMinor = openingBalanceMinor,
        startDate = openingBalanceDate,
        endDate = end,
        annualRateBasisPoints = bps,
    )
}

fun AccountEntity.toRecurringDepositTerms(): RecurringDepositTerms? {
    if (type != InvestmentType.RD) return null
    val bps = fixedAnnualRateBasisPoints ?: return null
    val monthly = rdMonthlyAmountMinor ?: return null
    val startYm = termStartYearMonth() ?: return null
    val endYm = termEndYearMonth() ?: return null
    return RecurringDepositTerms(
        monthlyAmountMinor = monthly,
        startMonth = startYm,
        endMonth = endYm,
        annualRateBasisPoints = bps,
    )
}

private fun AccountEntity.termStartYearMonth(): YearMonth? {
    val y = termStartYear ?: return null
    val m = termStartMonth ?: return null
    return YearMonth.of(y, m)
}

private fun AccountEntity.termEndYearMonth(): YearMonth? {
    val y = termEndYear ?: return null
    val m = termEndMonth ?: return null
    return YearMonth.of(y, m)
}
