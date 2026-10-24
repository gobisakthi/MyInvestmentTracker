package com.personal.investmenttracker.ui.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.local.entity.InterestRatePeriodEntity
import com.personal.investmenttracker.data.repository.InterestRateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InterestRateViewModel(
    private val repository: InterestRateRepository,
    private val accountId: Long,
) : ViewModel() {

    val rates: StateFlow<List<InterestRatePeriodEntity>> =
        repository.observeForAccount(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var lastError: String? = null
        private set

    fun save(
        effectiveYear: Int,
        effectiveMonth: Int,
        annualRateBasisPoints: Int,
        sourceNote: String?,
        existingId: Long?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.upsert(accountId, effectiveYear, effectiveMonth, annualRateBasisPoints, sourceNote, existingId)
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
