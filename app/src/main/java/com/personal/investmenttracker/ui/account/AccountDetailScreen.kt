package com.personal.investmenttracker.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.util.formatInrMinor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    asOfDateViewModel: AsOfDateViewModel,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onAddTransaction: () -> Unit,
    onInterestRateHistory: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Account #$accountId (placeholder)",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "As of ${asOfDateViewModel.formattedAsOfDate()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailLine("Opening balance (placeholder)", formatInrMinor(0L))
            DetailLine("Contributions / deposits (placeholder)", formatInrMinor(0L))
            DetailLine("Withdrawals (placeholder)", formatInrMinor(0L))
            DetailLine("Officially credited interest (placeholder)", formatInrMinor(0L))
            DetailLine("Estimated accrued interest (placeholder)", formatInrMinor(0L))
            DetailLine("Estimated current value (placeholder)", formatInrMinor(0L))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Interest-rate periods and transaction history will appear here after persistence and calculations are implemented.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddTransaction, modifier = Modifier.fillMaxWidth()) {
                Text("Add transaction")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onInterestRateHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Interest rate history")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onEditAccount, modifier = Modifier.fillMaxWidth()) {
                Text("Edit account")
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}
