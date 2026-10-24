package com.personal.investmenttracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.personal.investmenttracker.ui.account.AccountDetailViewModel
import com.personal.investmenttracker.ui.account.AddEditAccountViewModel
import com.personal.investmenttracker.ui.dashboard.DashboardViewModel
import com.personal.investmenttracker.ui.deposit.MonthlyDepositViewModel
import com.personal.investmenttracker.ui.rates.InterestRateViewModel

class InvestmentViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(container.accountCalculationRepository) as T
            modelClass.isAssignableFrom(AddEditAccountViewModel::class.java) ->
                AddEditAccountViewModel(
                    container.accountRepository,
                    container.interestRateRepository,
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }

    fun createAccountDetailViewModel(accountId: Long): AccountDetailViewModel =
        AccountDetailViewModel(container.accountCalculationRepository, accountId)

    fun createMonthlyDepositViewModel(accountId: Long): MonthlyDepositViewModel =
        MonthlyDepositViewModel(container.monthlyDepositRepository, accountId)

    fun createInterestRateViewModel(accountId: Long): InterestRateViewModel =
        InterestRateViewModel(container.interestRateRepository, accountId)
}
