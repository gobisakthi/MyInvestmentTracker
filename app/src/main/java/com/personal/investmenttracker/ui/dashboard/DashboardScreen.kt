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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.investmenttracker.domain.model.AccountSummary
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.util.formatInrMinor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    asOfDateViewModel: AsOfDateViewModel,
    onAddAccount: () -> Unit,
    onAccountClick: (Long) -> Unit,
    accounts: List<AccountSummary> = emptyList(),
) {
    val state = dashboardViewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Investment Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccount) {
                Icon(Icons.Default.Add, contentDescription = "Add account")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "As of ${asOfDateViewModel.formattedAsOfDate()}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Portfolio estimated total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatInrMinor(state.portfolioTotalMinor),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recorded balance (placeholder): ${formatInrMinor(state.recordedBalanceTotalMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Estimated accrued interest (placeholder): ${formatInrMinor(state.estimatedAccruedInterestMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Totals will update when accounts and calculations are connected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item {
                CategoryCardRow(
                    ppf = state.ppfTotals,
                    ssa = state.ssaTotals,
                    pf = state.pfTotals,
                )
            }

            item {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (accounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "No accounts yet",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "Add a PPF, SSA, or PF account to start tracking. Values show as zero until you enter balances and the calculation engine is enabled.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            } else {
                items(accounts, key = { it.id }) { account ->
                    AccountListItem(
                        account = account,
                        asOfLabel = asOfDateViewModel.formattedAsOfDate(),
                        onClick = { onAccountClick(account.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCardRow(
    ppf: CategoryTotals,
    ssa: CategoryTotals,
    pf: CategoryTotals,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryCard(
            title = InvestmentType.PPF.name,
            totals = ppf,
        )
        CategoryCard(
            title = InvestmentType.SSA.name,
            totals = ssa,
        )
        CategoryCard(
            title = InvestmentType.PF.name,
            totals = pf,
        )
    }
}

@Composable
private fun CategoryCard(title: String, totals: CategoryTotals) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${totals.accountCount} account(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatInrMinor(totals.estimatedTotalMinor),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun AccountListItem(
    account: AccountSummary,
    asOfLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = account.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = account.type.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatInrMinor(account.estimatedCurrentValueMinor),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "As of $asOfLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
