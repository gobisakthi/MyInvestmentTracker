package com.personal.investmenttracker.ui.deposit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.investmenttracker.util.Money
import com.personal.investmenttracker.util.formatInrMinor
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDepositScreen(
    viewModel: MonthlyDepositViewModel,
    onNavigateBack: () -> Unit,
) {
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    var year by remember { mutableStateOf("2026") }
    var month by remember { mutableStateOf("5") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly deposits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "One deposit per month per account. Applies to PPF, SSA, and PF.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(year, { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(month, { month = it }, label = { Text("Month (1–12)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val y = year.toIntOrNull()
                    val m = month.toIntOrNull()
                    val money = Money.fromRupeesString(amount)
                    if (y == null || m == null || money == null) {
                        error = "Enter valid year, month, and amount."
                        return@Button
                    }
                    viewModel.save(y, m, money.amountMinor, null) {
                        amount = ""
                        error = viewModel.lastError
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save deposit") }
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(deposits, key = { it.id }) { d ->
                    Text(
                        "${Month.of(d.month).name} ${d.year}: ${formatInrMinor(d.amountMinor)}",
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
