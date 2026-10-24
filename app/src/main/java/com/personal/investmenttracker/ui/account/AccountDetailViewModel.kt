package com.personal.investmenttracker.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.repository.AccountCalculationRepository
import com.personal.investmenttracker.data.repository.AccountWithCalculation
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class AccountDetailViewModel(
    private val accountCalculationRepository: AccountCalculationRepository,
    private val accountId: Long,
) : ViewModel() {

    private val asOfDate = MutableStateFlow(LocalDate.now())

    fun updateAsOfDate(date: LocalDate) {
        asOfDate.value = date
    }

    val evaluation: StateFlow<AccountWithCalculation?> =
        asOfDate.flatMapLatest { date ->
            accountCalculationRepository.observeAccountCalculation(accountId, flowOf(date))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
