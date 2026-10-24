package com.personal.investmenttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.data.repository.accountCountFor
import com.personal.investmenttracker.data.repository.portfolioRecordedTotalMinor
import com.personal.investmenttracker.data.repository.recordedTotalFor
import com.personal.investmenttracker.domain.model.AccountSummary
import com.personal.investmenttracker.domain.model.InvestmentType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CategoryTotals(
    val accountCount: Int,
    val recordedTotalMinor: Long,
)

data class DashboardUiState(
    val portfolioRecordedTotalMinor: Long = 0L,
    val ppfTotals: CategoryTotals = CategoryTotals(0, 0L),
    val ssaTotals: CategoryTotals = CategoryTotals(0, 0L),
    val pfTotals: CategoryTotals = CategoryTotals(0, 0L),
)

class DashboardViewModel(
    accountRepository: AccountRepository,
) : ViewModel() {

    val accounts: StateFlow<List<AccountSummary>> =
        accountRepository.observeAccountSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<DashboardUiState> =
        accounts.map { summaries -> summaries.toDashboardUiState() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

private fun List<AccountSummary>.toDashboardUiState(): DashboardUiState =
    DashboardUiState(
        portfolioRecordedTotalMinor = portfolioRecordedTotalMinor(),
        ppfTotals = CategoryTotals(
            accountCount = accountCountFor(InvestmentType.PPF),
            recordedTotalMinor = recordedTotalFor(InvestmentType.PPF),
        ),
        ssaTotals = CategoryTotals(
            accountCount = accountCountFor(InvestmentType.SSA),
            recordedTotalMinor = recordedTotalFor(InvestmentType.SSA),
        ),
        pfTotals = CategoryTotals(
            accountCount = accountCountFor(InvestmentType.PF),
            recordedTotalMinor = recordedTotalFor(InvestmentType.PF),
        ),
    )
