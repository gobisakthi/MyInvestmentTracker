package com.personal.investmenttracker.di

import com.personal.investmenttracker.data.local.InvestmentDatabase
import com.personal.investmenttracker.data.repository.AccountCalculationRepository
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.data.repository.InterestRateRepository
import com.personal.investmenttracker.data.repository.MonthlyDepositRepository

class AppContainer(database: InvestmentDatabase) {
    val accountRepository: AccountRepository = AccountRepository(database.accountDao())
    val monthlyDepositRepository: MonthlyDepositRepository =
        MonthlyDepositRepository(database.monthlyDepositDao())
    val interestRateRepository: InterestRateRepository =
        InterestRateRepository(database.interestRatePeriodDao())
    val accountCalculationRepository: AccountCalculationRepository =
        AccountCalculationRepository(
            database.accountDao(),
            database.monthlyDepositDao(),
            database.interestRatePeriodDao(),
        )
}
