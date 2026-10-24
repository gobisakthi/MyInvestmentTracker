package com.personal.investmenttracker.ui.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.local.entity.MonthlyDepositEntity
import com.personal.investmenttracker.data.repository.MonthlyDepositRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonthlyDepositViewModel(
    private val repository: MonthlyDepositRepository,
    private val accountId: Long,
) : ViewModel() {

    val deposits: StateFlow<List<MonthlyDepositEntity>> =
        repository.observeForAccount(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var lastError: String? = null
        private set

    fun save(year: Int, month: Int, amountMinor: Long, existingId: Long?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.upsert(accountId, year, month, amountMinor, existingId)
                .onSuccess { onSuccess() }
                .onFailure { lastError = it.message }
        }
    }

    fun delete(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id).onSuccess { onSuccess() }.onFailure { lastError = it.message }
        }
    }
}
