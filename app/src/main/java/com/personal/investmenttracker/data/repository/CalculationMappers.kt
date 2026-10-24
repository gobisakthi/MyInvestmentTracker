package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.data.local.entity.InterestRatePeriodEntity
import com.personal.investmenttracker.data.local.entity.MonthlyDepositEntity
import com.personal.investmenttracker.domain.calculation.InterestCalculationInput
import com.personal.investmenttracker.domain.calculation.MonthlyDepositInput
import com.personal.investmenttracker.domain.calculation.RatePeriodInput
import java.time.YearMonth

fun AccountEntity.toCalculationInput(
    deposits: List<MonthlyDepositEntity>,
    rates: List<InterestRatePeriodEntity>,
): InterestCalculationInput =
    InterestCalculationInput(
        accountType = type,
        openingBalanceMinor = openingBalanceMinor,
        openingBalanceDate = openingBalanceDate,
        openingBalanceIncludesCreditedInterest = openingBalanceIncludesCreditedInterest,
        monthlyDeposits = deposits.map {
            MonthlyDepositInput(year = it.year, month = it.month, amountMinor = it.amountMinor)
        },
        ratePeriods = rates.map {
            val ym = YearMonth.from(it.effectiveFrom)
            RatePeriodInput(
                effectiveYear = ym.year,
                effectiveMonth = ym.monthValue,
                annualRateBasisPoints = it.annualRateBasisPoints,
            )
        },
    )
