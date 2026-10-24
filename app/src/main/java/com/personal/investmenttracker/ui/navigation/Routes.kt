package com.personal.investmenttracker.ui.navigation

object Routes {
    const val Dashboard = "dashboard"
    const val AddAccount = "add_account"
    const val EditAccount = "edit_account/{accountId}"
    const val AccountDetail = "account_detail/{accountId}"
    const val AddTransaction = "add_transaction/{accountId}"
    const val InterestRateHistory = "interest_rate_history/{accountId}"

    fun editAccount(accountId: Long) = "edit_account/$accountId"
    fun accountDetail(accountId: Long) = "account_detail/$accountId"
    fun addTransaction(accountId: Long) = "add_transaction/$accountId"
    fun interestRateHistory(accountId: Long) = "interest_rate_history/$accountId"
}
