package com.personal.investmenttracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.investmenttracker.data.repository.AccountWithCalculation
import com.personal.investmenttracker.data.repository.AccountDailyInterest
import com.personal.investmenttracker.data.repository.dailyInterestBreakdown
import com.personal.investmenttracker.data.repository.filteredForAllocationTypes
import com.personal.investmenttracker.data.repository.sortedByEstimatedTotalDescending
import com.personal.investmenttracker.domain.calculation.CalculationStatus
import com.personal.investmenttracker.domain.model.DASHBOARD_ALLOCATION_PAGE_COUNT
import com.personal.investmenttracker.domain.model.DashboardPortfolioScope
import com.personal.investmenttracker.domain.model.dashboardAllocationPage
import com.personal.investmenttracker.domain.model.dashboardAllocationSubtitle
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.ui.components.AllocationPieChartsPager
import com.personal.investmenttracker.ui.components.PremiumGlassCard
import com.personal.investmenttracker.ui.components.PremiumHeroCard
import com.personal.investmenttracker.ui.components.colorForInvestmentType
import com.personal.investmenttracker.ui.theme.GainGreen
import com.personal.investmenttracker.ui.theme.GoldAccent
import com.personal.investmenttracker.ui.theme.LossRed
import com.personal.investmenttracker.util.formatInrMinor
import com.personal.investmenttracker.util.formatSignedChangeMinor
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    asOfDateViewModel: AsOfDateViewModel,
    onAddAccount: () -> Unit,
    onAccountClick: (Long) -> Unit,
) {
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val accounts by dashboardViewModel.accountsWithCalculations.collectAsStateWithLifecycle()
    val allocationPagerState = rememberPagerState(pageCount = { DASHBOARD_ALLOCATION_PAGE_COUNT })
    val allocationPage = allocationPagerState.currentPage
    val allocationIncludeTypes = dashboardAllocationPage(allocationPage).includeTypes
    val visibleAccounts = remember(accounts, allocationPage) {
        accounts.filteredForAllocationTypes(allocationIncludeTypes).sortedByEstimatedTotalDescending()
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var dailyInterestSheet by remember { mutableStateOf<DailyInterestSheetArgs?>(null) }

    dailyInterestSheet?.let { args ->
        DailyInterestBreakdownSheet(
            scopeLabel = args.scopeLabel,
            asOfLabel = args.asOfLabel,
            totalDailyMinor = args.totalDailyMinor,
            lines = args.lines,
            onDismiss = { dailyInterestSheet = null },
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            asOfDateViewModel.setAsOfDate(date)
                        }
                        showDatePicker = false
                    },
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Portfolio", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "As-of date")
                    }
                    TextButton(onClick = { asOfDateViewModel.resetToToday() }) {
                        Text("Today")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccount,
                containerColor = GoldAccent,
                contentColor = Color(0xFF422006),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add account")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                val page = allocationPage
                val portfolioScope = dashboardAllocationPage(page).portfolioScope
                val portfolioTotal = when (portfolioScope) {
                    DashboardPortfolioScope.TRADITIONAL -> state.traditionalPortfolioTotalMinor
                    DashboardPortfolioScope.PROVIDENT_PLUS_STOCKS -> state.providentPlusStocksPortfolioTotalMinor
                    DashboardPortfolioScope.ALL_HOLDINGS -> state.portfolioEstimatedTotalMinor
                }
                val portfolioChange = when (portfolioScope) {
                    DashboardPortfolioScope.TRADITIONAL -> state.traditionalChangeFromYesterdayMinor
                    DashboardPortfolioScope.PROVIDENT_PLUS_STOCKS ->
                        state.providentPlusStocksChangeFromYesterdayMinor
                    DashboardPortfolioScope.ALL_HOLDINGS -> state.changeFromYesterdayMinor
                }
                val changeCaption = "est. interest per day"
                val interestIncludeTypes = dashboardAllocationPage(page).includeTypes
                val interestLines = accounts.dailyInterestBreakdown(interestIncludeTypes)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumHeroCard {
                        Text(
                            asOfDateViewModel.formattedAsOfDate(),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            dashboardAllocationSubtitle(page),
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            formatInrMinor(portfolioTotal),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Column(
                            modifier = Modifier
                                .clickable {
                                    dailyInterestSheet = DailyInterestSheetArgs(
                                        scopeLabel = dashboardAllocationSubtitle(page),
                                        asOfLabel = asOfDateViewModel.formattedAsOfDate(),
                                        totalDailyMinor = portfolioChange,
                                        lines = interestLines,
                                    )
                                }
                                .padding(top = 4.dp),
                        ) {
                            Text(
                                text = formatSignedChangeMinor(portfolioChange) + " $changeCaption",
                                color = when {
                                    portfolioChange > 0L -> GainGreen.copy(alpha = 0.95f)
                                    portfolioChange < 0L -> LossRed.copy(alpha = 0.95f)
                                    else -> Color.White.copy(alpha = 0.7f)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "Tap for breakdown by account",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    PremiumGlassCard {
                        AllocationPieChartsPager(
                            accounts = accounts,
                            pagerState = allocationPagerState,
                        )
                    }
                }
            }

            if (state.hasCalculationWarnings) {
                item {
                    Text(
                        "Some accounts need rate history to calculate.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                Text("Accounts in this view", style = MaterialTheme.typography.titleLarge)
                Text(
                    dashboardAllocationSubtitle(allocationPage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (accounts.isEmpty()) {
                item {
                    PremiumGlassCard {
                        Text("No accounts yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap + to add an investment or stock holding.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else if (visibleAccounts.isEmpty()) {
                item {
                    PremiumGlassCard {
                        Text("No accounts in this slice", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Swipe the chart or add a holding that matches this view.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(visibleAccounts, key = { it.summary.id }) { row ->
                    PremiumAccountRow(row = row, onClick = { onAccountClick(row.summary.id) })
                }
            }
        }
    }
}

private data class DailyInterestSheetArgs(
    val scopeLabel: String,
    val asOfLabel: String,
    val totalDailyMinor: Long,
    val lines: List<AccountDailyInterest>,
)

@Composable
private fun PremiumAccountRow(
    row: AccountWithCalculation,
    onClick: () -> Unit,
) {
    val accent = colorForInvestmentType(row.summary.type)
    PremiumGlassCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.summary.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    row.summary.type.name,
                    color = accent,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (row.calculation.status == CalculationStatus.OK) {
                    Text(
                        formatInrMinor(row.calculation.estimatedTotalMinor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text("—", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
