package com.personal.investmenttracker.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CategoryTotals(
    val accountCount: Int,
    val estimatedTotalMinor: Long,
)

data class DashboardUiState(
    val portfolioTotalMinor: Long = 0L,
    val recordedBalanceTotalMinor: Long = 0L,
    val estimatedAccruedInterestMinor: Long = 0L,
    val ppfTotals: CategoryTotals = CategoryTotals(0, 0L),
    val ssaTotals: CategoryTotals = CategoryTotals(0, 0L),
    val pfTotals: CategoryTotals = CategoryTotals(0, 0L),
)

/**
 * Placeholder state until Room and the calculation engine are wired in Milestone 3+.
 */
class DashboardViewModel : ViewModel() {
    var uiState by mutableStateOf(DashboardUiState())
        private set
}
