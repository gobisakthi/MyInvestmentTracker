package com.personal.investmenttracker.domain.calculation

import com.personal.investmenttracker.domain.model.InvestmentType

/**
 * Scheme-specific policies plugged into the shared monthly pipeline.
 * PPF and SSA use government-style monthly eligible balance (app assumes month/year deposits
 * qualify for that full month). PF uses the same pipeline with an estimate disclaimer.
 */
data class SchemeRules(
    val eligibleBalancePolicy: EligibleBalancePolicy,
    val financialYearCreditMonth: Int = 3, // March — credit accumulated FY interest
)

fun schemeRulesFor(type: InvestmentType): SchemeRules {
    val policy = when (type) {
        InvestmentType.PPF,
        InvestmentType.SSA,
        InvestmentType.PF,
        -> EligibleBalancePolicy.MonthDepositIncludedInEligibleBalance
    }
    return SchemeRules(eligibleBalancePolicy = policy)
}

enum class EligibleBalancePolicy {
    /** Balance at month start plus this month's deposit (no withdrawals in this app). */
    MonthDepositIncludedInEligibleBalance,
}

fun eligibleBalanceMinor(
    policy: EligibleBalancePolicy,
    principalAtMonthStartMinor: Long,
    depositThisMonthMinor: Long,
): Long = when (policy) {
    EligibleBalancePolicy.MonthDepositIncludedInEligibleBalance ->
        principalAtMonthStartMinor + depositThisMonthMinor
}
