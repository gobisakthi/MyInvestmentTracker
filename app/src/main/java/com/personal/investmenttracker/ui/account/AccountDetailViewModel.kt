package com.personal.investmenttracker.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.data.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AccountDetailViewModel(
    accountRepository: AccountRepository,
    accountId: Long,
) : ViewModel() {

    val account: StateFlow<AccountEntity?> =
        accountRepository.observeAccount(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
