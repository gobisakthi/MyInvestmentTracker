package com.personal.investmenttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.repository.AccountCalculationRepository
import com.personal.investmenttracker.data.repository.AccountWithCalculation
import com.personal.investmenttracker.data.repository.countFor
import com.personal.investmenttracker.data.repository.estimatedTotalFor
import com.personal.investmenttracker.data.repository.portfolioAccruedInterestMinor
import com.personal.investmenttracker.data.repository.portfolioEstimatedTotalMinor
import com.personal.investmenttracker.domain.model.providentPlusStocksAllocationTypes
import com.personal.investmenttracker.domain.model.traditionalAllocationTypes
import com.personal.investmenttracker.domain.calculation.CalculationStatus
import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CategoryTotals(
    val accountCount: Int,
    val estimatedTotalMinor: Long,
)

data class DashboardUiState(
    val portfolioEstimatedTotalMinor: Long = 0L,
    val portfolioRecordedOpeningMinor: Long = 0L,
    val portfolioCreditedInterestMinor: Long = 0L,
    val portfolioUncreditedInterestMinor: Long = 0L,
    val portfolioProvisionalMinor: Long = 0L,
    val changeFromYesterdayMinor: Long = 0L,
    val traditionalPortfolioTotalMinor: Long = 0L,
    val traditionalChangeFromYesterdayMinor: Long = 0L,
    val providentPlusStocksPortfolioTotalMinor: Long = 0L,
    val providentPlusStocksChangeFromYesterdayMinor: Long = 0L,
    val ppfTotals: CategoryTotals = CategoryTotals(0, 0L),
    val ssaTotals: CategoryTotals = CategoryTotals(0, 0L),
    val pfTotals: CategoryTotals = CategoryTotals(0, 0L),
    val hasCalculationWarnings: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val accountCalculationRepository: AccountCalculationRepository,
) : ViewModel() {

    private val asOfDate = MutableStateFlow(LocalDate.now())

    fun updateAsOfDate(date: LocalDate) {
        asOfDate.value = date
    }

    val accountsWithCalculations: StateFlow<List<AccountWithCalculation>> =
        asOfDate.flatMapLatest { date ->
            accountCalculationRepository.observeAccountsWithCalculations(flowOf(date))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<DashboardUiState> =
        combine(
            asOfDate.flatMapLatest { date ->
                accountCalculationRepository.observeAccountsWithCalculations(flowOf(date))
            },
            asOfDate.flatMapLatest { date ->
                accountCalculationRepository.observeAccountsWithCalculations(flowOf(date.minusDays(1)))
            },
        ) { todayList, yesterdayList ->
            val todayState = todayList.toDashboardUiState()
            val allToday = todayList.portfolioEstimatedTotalMinor()
            val allYesterday = yesterdayList.portfolioEstimatedTotalMinor()
            val tradToday = todayList.portfolioEstimatedTotalMinor(traditionalAllocationTypes)
            val tradInterestToday = todayList.portfolioAccruedInterestMinor(traditionalAllocationTypes)
            val tradInterestYesterday = yesterdayList.portfolioAccruedInterestMinor(traditionalAllocationTypes)
            val ppsToday = todayList.portfolioEstimatedTotalMinor(providentPlusStocksAllocationTypes)
            val ppsInterestToday = todayList.portfolioAccruedInterestMinor(providentPlusStocksAllocationTypes)
            val ppsInterestYesterday = yesterdayList.portfolioAccruedInterestMinor(providentPlusStocksAllocationTypes)
            todayState.copy(
                portfolioEstimatedTotalMinor = allToday,
                changeFromYesterdayMinor = allToday - allYesterday,
                traditionalPortfolioTotalMinor = tradToday,
                traditionalChangeFromYesterdayMinor = tradInterestToday - tradInterestYesterday,
                providentPlusStocksPortfolioTotalMinor = ppsToday,
                providentPlusStocksChangeFromYesterdayMinor = ppsInterestToday - ppsInterestYesterday,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

private fun List<AccountWithCalculation>.toDashboardUiState(): DashboardUiState {
    val ok = filter { it.calculation.status == CalculationStatus.OK }
    return DashboardUiState(
        portfolioEstimatedTotalMinor = portfolioEstimatedTotalMinor(),
        portfolioRecordedOpeningMinor = sumOf { it.summary.recordedBalanceMinor },
        portfolioCreditedInterestMinor = ok.sumOf { it.calculation.creditedInterestMinor },
        portfolioUncreditedInterestMinor = ok.sumOf { it.calculation.uncreditedCompletedMonthsInterestMinor },
        portfolioProvisionalMinor = ok.sumOf { it.calculation.provisionalCurrentMonthMinor },
        ppfTotals = CategoryTotals(countFor(InvestmentType.PPF), estimatedTotalFor(InvestmentType.PPF)),
        ssaTotals = CategoryTotals(countFor(InvestmentType.SSA), estimatedTotalFor(InvestmentType.SSA)),
        pfTotals = CategoryTotals(countFor(InvestmentType.PF), estimatedTotalFor(InvestmentType.PF)),
        hasCalculationWarnings = any { it.calculation.status != CalculationStatus.OK },
    )
}
