package com.personal.investmenttracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.investmenttracker.data.repository.AccountDailyInterest
import com.personal.investmenttracker.ui.components.PortfolioPieChart
import com.personal.investmenttracker.ui.components.colorForInvestmentType
import com.personal.investmenttracker.ui.components.pieSlicesForDailyInterest
import com.personal.investmenttracker.util.formatInrMinor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyInterestBreakdownSheet(
    scopeLabel: String,
    asOfLabel: String,
    totalDailyMinor: Long,
    lines: List<AccountDailyInterest>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pieSlices = remember(lines) { pieSlicesForDailyInterest(lines) }
    val totalForPercent = totalDailyMinor.coerceAtLeast(1L).toDouble()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Est. interest per day", style = MaterialTheme.typography.titleLarge)
                Text(
                    scopeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    asOfLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        formatInrMinor(totalDailyMinor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "365-day rate on each account’s eligible balance (PPF, SSA, PF, FD, RD).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (lines.isEmpty()) {
                item {
                    Text(
                        "No interest-bearing accounts in this view.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Share of daily interest",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Tap a slice for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    PortfolioPieChart(slices = pieSlices)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "By account",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(lines, key = { it.account.summary.id }) { line ->
                    val accent = colorForInvestmentType(line.account.summary.type)
                    val percent = line.dailyInterestMinor / totalForPercent * 100.0
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                line.account.summary.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                line.account.summary.type.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatInrMinor(line.dailyInterestMinor),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "${"%.1f".format(Locale.US, percent)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
