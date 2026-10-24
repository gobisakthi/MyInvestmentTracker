package com.personal.investmenttracker.di

import com.personal.investmenttracker.data.local.InvestmentDatabase
import com.personal.investmenttracker.data.repository.AccountRepository

class AppContainer(database: InvestmentDatabase) {
    val accountRepository: AccountRepository = AccountRepository(database.accountDao())
}
